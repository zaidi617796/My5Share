package com.myfiveshare.app;

import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Process;
import android.provider.Settings;

/**
 * NetworkStatsManager se is phone ki ASAL mobile data usage parhna.
 * Sirf mobile data (WiFi nahi). Usage Access ki ijazat lazmi hai —
 * baghair us ke Android zero deta hai.
 */
public class UsageReader {

    /** Usage Access mila hua hai ya nahi */
    public static boolean hasUsageAccess(Context ctx) {
        try {
            AppOpsManager aom = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
            if (aom == null) return false;
            int mode = aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), ctx.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }

    /** Usage Access wali Settings screen kholo */
    public static void openUsageSettings(Context ctx) {
        try {
            Intent i = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception ignored) {}
    }

    /**
     * startMs se endMs tak kul MOBILE data (bytes, Rx+Tx).
     * Khata khali ho ya error aaye to -1.
     */
    public static long mobileUsageBytes(Context ctx, long startMs, long endMs) {
        try {
            NetworkStatsManager nsm =
                    (NetworkStatsManager) ctx.getSystemService(Context.NETWORK_STATS_SERVICE);
            if (nsm == null) return -1;
            NetworkStats.Bucket bucket =
                    nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startMs, endMs);
            if (bucket == null) return -1;
            return bucket.getRxBytes() + bucket.getTxBytes();
        } catch (Exception e) {
            return -1;
        }
    }

    /** Ek runtime permission mili hui hai ya nahi */
    public static boolean hasPerm(Context ctx, String perm) {
        try {
            return ctx.checkSelfPermission(perm)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * startMs se ab tak ki kul call minutes (CallLog se — incoming + outgoing).
     * READ_CALL_LOG ki ijazat na ho ya error aaye to -1.
     */
    public static int callMinutesUsed(Context ctx, long startMs) {
        if (!hasPerm(ctx, android.Manifest.permission.READ_CALL_LOG)) return -1;
        android.database.Cursor c = null;
        try {
            c = ctx.getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    new String[]{android.provider.CallLog.Calls.DURATION},
                    android.provider.CallLog.Calls.DATE + " > ?",
                    new String[]{String.valueOf(startMs)}, null);
            if (c == null) return -1;
            long secs = 0;
            int di = c.getColumnIndex(android.provider.CallLog.Calls.DURATION);
            while (c.moveToNext()) secs += Math.max(0, c.getLong(di));
            return (int) Math.round(secs / 60.0);
        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }

    /**
     * Kisi Pakistani mobile number ka operator, sirf prefix se.
     * Jazz: 0300-0309 + Warid wale 0321-0324 (ab Jazz) | Zong: 0310-0319 |
     * Ufone: 0330-0339 | Telenor: 0340-0349. Mobile na ho to null.
     * Note: number portability (MNP) wale number par prefix ghalat bhi ho sakta hai —
     * phone ke paas asal network jaanne ka koi tareeqa nahi hota.
     */
    public static String operatorOfNumber(String raw) {
        if (raw == null) return null;
        String n = raw.replaceAll("[^0-9+]", "");
        if (n.startsWith("+92")) n = "0" + n.substring(3);
        else if (n.startsWith("0092")) n = "0" + n.substring(4);
        else if (n.startsWith("92") && n.length() == 12) n = "0" + n.substring(2);
        if (!n.startsWith("03") || n.length() < 4) return null;
        int code;
        try { code = Integer.parseInt(n.substring(0, 4)); } catch (Exception e) { return null; }
        if (code >= 300 && code <= 309) return "Jazz";
        if (code >= 321 && code <= 324) return "Jazz";
        if (code >= 310 && code <= 319) return "Zong";
        if (code >= 330 && code <= 339) return "Ufone";
        if (code >= 340 && code <= 349) return "Telenor";
        return "Other";
    }

    /**
     * Call minutes ka on-net / off-net split: jis number par call hui, us ke
     * operator ko group ke operator se milao. Same network = on-net; doosra
     * mobile network, landline ya unknown = off-net.
     * Wapas: int[]{onNet, offNet}; ijazat na ho to {-1, -1}.
     */
    public static int[] callMinutesSplit(Context ctx, long startMs, String groupOperator) {
        if (!hasPerm(ctx, android.Manifest.permission.READ_CALL_LOG)) return new int[]{-1, -1};
        android.database.Cursor c = null;
        try {
            c = ctx.getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    new String[]{android.provider.CallLog.Calls.DURATION,
                            android.provider.CallLog.Calls.NUMBER},
                    android.provider.CallLog.Calls.DATE + " > ?",
                    new String[]{String.valueOf(startMs)}, null);
            if (c == null) return new int[]{-1, -1};
            long onSecs = 0, offSecs = 0;
            int di = c.getColumnIndex(android.provider.CallLog.Calls.DURATION);
            int ni = c.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
            while (c.moveToNext()) {
                long d = Math.max(0, c.getLong(di));
                String op = ni >= 0 ? operatorOfNumber(c.getString(ni)) : null;
                if (op != null && groupOperator != null && op.equalsIgnoreCase(groupOperator)) onSecs += d;
                else offSecs += d;
            }
            return new int[]{(int) Math.round(onSecs / 60.0), (int) Math.round(offSecs / 60.0)};
        } catch (Exception e) {
            return new int[]{-1, -1};
        } finally {
            if (c != null) c.close();
        }
    }

    /**
     * startMs se ab tak bheje gaye SMS ki tadaad (content://sms/sent).
     * READ_SMS ki ijazat na ho ya error aaye to -1.
     */
    public static int smsSentCount(Context ctx, long startMs) {
        if (!hasPerm(ctx, android.Manifest.permission.READ_SMS)) return -1;
        android.database.Cursor c = null;
        try {
            c = ctx.getContentResolver().query(
                    android.net.Uri.parse("content://sms/sent"),
                    new String[]{"_id"},
                    "date > ?",
                    new String[]{String.valueOf(startMs)}, null);
            if (c == null) return -1;
            return c.getCount();
        } catch (Exception e) {
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }

    /** Bytes ko insaani shakl me: "1.25 GB" / "850 MB" */
    public static String humanGb(long bytes) {
        if (bytes < 0) return "--";
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1) return String.format(java.util.Locale.US, "%.2f GB", gb);
        double mb = bytes / (1024.0 * 1024.0);
        return String.format(java.util.Locale.US, "%.0f MB", mb);
    }

    /** Bytes sirf GB number me (hisab ke liye) */
    public static double toGb(long bytes) {
        return bytes < 0 ? -1 : bytes / (1024.0 * 1024.0 * 1024.0);
    }
}

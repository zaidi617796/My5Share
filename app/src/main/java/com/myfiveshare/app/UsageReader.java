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

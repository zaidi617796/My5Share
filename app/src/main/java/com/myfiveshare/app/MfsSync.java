package com.myfiveshare.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MyFiveShare ka auto-sync — Supabase REST (Signal Map wala hi project, alag tables mfs_*).
 * Group code hi shared secret hai. Har call background thread par, jawab main thread par.
 * Tables ka SQL: myfiveshare-supabase.sql (ek dafa dashboard se chalana hota hai).
 */
public class MfsSync {

    private static final String SB_URL = "https://sogugyourgaacustjmjw.supabase.co";
    private static final String SB_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNvZ3VneW91cmdhYWN1c3RqbWp3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE4MTYyNjUsImV4cCI6MjEwNzM5MjI2NX0.9i--Lreqv2Pa5BUTo5AhEHja-a3uf0uJfuwawwuKFf4";

    private static final ExecutorService EX = Executors.newSingleThreadExecutor();
    private static final android.os.Handler MAIN = new android.os.Handler(android.os.Looper.getMainLooper());

    public interface Cb<T> {
        void ok(T v);
        void fail(String msg);
    }

    public static class GroupInfo {
        public String code, operator;
        public double totalGb;
        public int totalMins, totalOnnetMins, totalOffnetMins, totalSms, membersCount, validityDays;
        public long cycleStartMs;
    }

    public static class MemberRow {
        public String name, deviceId;
        public long usedBytes;
        public int usedMins, usedOnnetMins, usedOffnetMins, usedSms;
        public long updatedAtMs;
    }

    // ---------- public operations ----------

    /** Owner: group server par banao, phir khud ko member joro */
    public static void createGroup(MfsStore st, long usedBytes, int usedMins,
                                   int usedOnnet, int usedOffnet, int usedSms, Cb<Boolean> cb) {
        EX.execute(() -> {
            try {
                JSONObject g = new JSONObject();
                g.put("code", st.joinCode);
                g.put("operator", st.operator);
                g.put("total_gb", st.totalGb);
                g.put("total_mins", st.totalMins);
                g.put("total_onnet_mins", st.totalOnnetMins);
                g.put("total_offnet_mins", st.totalOffnetMins);
                g.put("total_sms", st.totalSms);
                g.put("members_count", st.members);
                g.put("cycle_start", iso(st.cycleStartMs));
                g.put("validity_days", st.validityDays);
                request("POST", "/rest/v1/mfs_groups", g.toString(), null);
                upsertMemberSync(st, usedBytes, usedMins, usedOnnet, usedOffnet, usedSms);
                post(() -> cb.ok(true));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    /** Member: sirf code se group ki tafseel lao (auto — haath se kuch nahi dalna) */
    public static void findGroup(String code, Cb<GroupInfo> cb) {
        EX.execute(() -> {
            try {
                String body = request("GET",
                        "/rest/v1/mfs_groups?code=eq." + enc(code) + "&select=*", null, null);
                JSONArray arr = new JSONArray(body);
                if (arr.length() == 0) {
                    post(() -> cb.ok(null));
                    return;
                }
                JSONObject g = arr.getJSONObject(0);
                GroupInfo gi = new GroupInfo();
                gi.code = g.getString("code");
                gi.operator = g.getString("operator");
                gi.totalGb = g.getDouble("total_gb");
                gi.totalMins = g.optInt("total_mins", 0);
                gi.totalOnnetMins = g.optInt("total_onnet_mins", 0);
                gi.totalOffnetMins = g.optInt("total_offnet_mins", 0);
                // Purana (v2) group: split khaali ho to kul minutes on-net samjho
                if (gi.totalOnnetMins == 0 && gi.totalOffnetMins == 0 && gi.totalMins > 0) {
                    gi.totalOnnetMins = gi.totalMins;
                }
                gi.totalSms = g.optInt("total_sms", 0);
                gi.membersCount = g.getInt("members_count");
                gi.validityDays = g.getInt("validity_days");
                gi.cycleStartMs = parseIso(g.getString("cycle_start"));
                post(() -> cb.ok(gi));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    /** Meri usage server par bhejo (insert ya update — device_id par upsert) */
    public static void upsertMember(MfsStore st, long usedBytes, int usedMins,
                                    int usedOnnet, int usedOffnet, int usedSms, Cb<Boolean> cb) {
        EX.execute(() -> {
            try {
                upsertMemberSync(st, usedBytes, usedMins, usedOnnet, usedOffnet, usedSms);
                post(() -> cb.ok(true));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    /** Poore group ke members ki usage lao (bara istemal pehle) */
    public static void fetchMembers(String code, Cb<List<MemberRow>> cb) {
        EX.execute(() -> {
            try {
                String body = request("GET",
                        "/rest/v1/mfs_members?group_code=eq." + enc(code)
                                + "&select=name,device_id,used_bytes,used_mins,used_onnet_mins,used_offnet_mins,used_sms,updated_at"
                                + "&order=used_bytes.desc", null, null);
                JSONArray arr = new JSONArray(body);
                List<MemberRow> out = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject m = arr.getJSONObject(i);
                    MemberRow r = new MemberRow();
                    r.name = m.getString("name");
                    r.deviceId = m.getString("device_id");
                    r.usedBytes = m.getLong("used_bytes");
                    r.usedMins = m.optInt("used_mins", 0);
                    r.usedOnnetMins = m.optInt("used_onnet_mins", 0);
                    r.usedOffnetMins = m.optInt("used_offnet_mins", 0);
                    // Purani (v2) row: split khaali ho to kul minutes on-net samjho
                    if (r.usedOnnetMins == 0 && r.usedOffnetMins == 0 && r.usedMins > 0) {
                        r.usedOnnetMins = r.usedMins;
                    }
                    r.usedSms = m.optInt("used_sms", 0);
                    r.updatedAtMs = parseIso(m.getString("updated_at"));
                    out.add(r);
                }
                post(() -> cb.ok(out));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    /** Admin: naya cycle — server par cycle_start aaj ki karo */
    public static void updateCycleStart(MfsStore st, Cb<Boolean> cb) {
        EX.execute(() -> {
            try {
                JSONObject g = new JSONObject();
                g.put("cycle_start", iso(st.cycleStartMs));
                request("PATCH", "/rest/v1/mfs_groups?code=eq." + enc(st.joinCode), g.toString(), null);
                post(() -> cb.ok(true));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    /** Admin: group khatam (members cascade se khud hat jayenge) */
    public static void deleteGroup(String code, Cb<Boolean> cb) {
        EX.execute(() -> {
            try {
                request("DELETE", "/rest/v1/mfs_groups?code=eq." + enc(code), null, null);
                post(() -> cb.ok(true));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    /** Member: sirf apna record group se nikalo */
    public static void deleteMyMember(MfsStore st, Cb<Boolean> cb) {
        EX.execute(() -> {
            try {
                request("DELETE", "/rest/v1/mfs_members?group_code=eq." + enc(st.joinCode)
                        + "&device_id=eq." + enc(st.deviceId), null, null);
                post(() -> cb.ok(true));
            } catch (Exception e) {
                post(() -> cb.fail(String.valueOf(e.getMessage())));
            }
        });
    }

    // ---------- internals ----------

    private static void upsertMemberSync(MfsStore st, long usedBytes, int usedMins,
                                         int usedOnnet, int usedOffnet, int usedSms) throws Exception {
        JSONObject m = new JSONObject();
        m.put("group_code", st.joinCode);
        m.put("device_id", st.deviceId);
        m.put("name", st.myName);
        m.put("used_bytes", usedBytes);
        m.put("used_mins", usedMins);
        m.put("used_onnet_mins", usedOnnet);
        m.put("used_offnet_mins", usedOffnet);
        m.put("used_sms", usedSms);
        m.put("updated_at", iso(System.currentTimeMillis()));
        request("POST", "/rest/v1/mfs_members?on_conflict=group_code,device_id",
                m.toString(), "resolution=merge-duplicates");
    }

    private static String request(String method, String path, String jsonBody, String preferExtra) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(SB_URL + path).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(12000);
        c.setReadTimeout(12000);
        c.setRequestProperty("apikey", SB_KEY);
        c.setRequestProperty("Authorization", "Bearer " + SB_KEY);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Prefer", "return=minimal" + (preferExtra != null ? "," + preferExtra : ""));
        if (jsonBody != null) {
            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.close();
        }
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
        }
        c.disconnect();
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code + ": " + sb);
        return sb.toString();
    }

    private static void post(Runnable r) {
        MAIN.post(r);
    }

    private static String enc(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8");
    }

    private static String iso(long ms) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(ms));
    }

    private static long parseIso(String s) {
        try {
            String t = s.trim();
            // PostgREST ki shaklein: "2026-10-28T04:00:00.123456+00:00" waghera
            t = t.replaceAll("\\.(\\d{3})\\d+", ".$1");          // millis tak rakho
            t = t.replaceAll("([+-]\\d{2}):(\\d{2})$", "$1$2");  // +00:00 -> +0000
            t = t.replace("Z", "+0000");
            String[] fmts = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ"};
            for (String p : fmts) {
                try {
                    return new SimpleDateFormat(p, Locale.US).parse(t).getTime();
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return 0;
    }
}

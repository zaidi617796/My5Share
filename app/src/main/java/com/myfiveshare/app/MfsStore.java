package com.myfiveshare.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * MyFiveShare ka local data (SharedPreferences).
 * Group/package ki settings + mera naam. Doosre members ka data
 * backend sync (agle version) se aayega — abhi har phone apna hisaab rakhta hai.
 */
public class MfsStore {

    private static final String PREFS = "MyFiveShare";

    public boolean setupDone;
    public boolean owner;
    public String myName = "";
    public String operator = "Jazz";
    public double totalGb = 100;
    public int members = 5;
    public long cycleStartMs;
    public int validityDays = 30;
    public String joinCode = "";

    private final SharedPreferences sp;

    public MfsStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        setupDone = sp.getBoolean("setup_done", false);
        owner = sp.getBoolean("owner", true);
        myName = sp.getString("my_name", "");
        operator = sp.getString("operator", "Jazz");
        totalGb = sp.getFloat("total_gb", 100f);
        members = sp.getInt("members", 5);
        cycleStartMs = sp.getLong("cycle_start", System.currentTimeMillis());
        validityDays = sp.getInt("validity_days", 30);
        joinCode = sp.getString("join_code", "");
    }

    public void save() {
        sp.edit()
                .putBoolean("setup_done", setupDone)
                .putBoolean("owner", owner)
                .putString("my_name", myName)
                .putString("operator", operator)
                .putFloat("total_gb", (float) totalGb)
                .putInt("members", members)
                .putLong("cycle_start", cycleStartMs)
                .putInt("validity_days", validityDays)
                .putString("join_code", joinCode)
                .apply();
    }

    public void clearAll() {
        sp.edit().clear().apply();
        setupDone = false;
        joinCode = "";
    }

    /** Mera barabar hissa (GB) */
    public double fairShareGb() {
        return members > 0 ? totalGb / members : totalGb;
    }

    public long cycleEndMs() {
        return cycleStartMs + validityDays * 24L * 60L * 60L * 1000L;
    }

    /** Cycle me kitne din baqi (0 = khatam) */
    public int daysLeft() {
        long diff = cycleEndMs() - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) Math.ceil(diff / (24.0 * 60 * 60 * 1000));
    }

    /** Cycle ka kitna hissa guzar gaya (0..1) */
    public double cycleProgress() {
        long now = System.currentTimeMillis();
        if (now <= cycleStartMs) return 0;
        if (now >= cycleEndMs()) return 1;
        return (double) (now - cycleStartMs) / (double) (cycleEndMs() - cycleStartMs);
    }

    /** Naya join code: 6 harf/number, uljhan wale harf nahi (0/O, 1/I nahi) */
    public static String newJoinCode() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        java.util.Random r = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}

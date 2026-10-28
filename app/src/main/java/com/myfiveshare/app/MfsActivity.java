package com.myfiveshare.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * MyFiveShare — sharing package ka hisaab.
 * v1: Usage Access + asal usage reading + package setup + fair-share dashboard (single phone).
 * Group sync (sab members ek dashboard par) backend ke saath agle version me.
 * UI poori programmatic hai (koi layout XML nahi) — UdhaarWapas/NoFap wali pattern.
 */
public class MfsActivity extends AppCompatActivity {

    private static final int BG = Color.parseColor("#0F172A");
    private static final int CARD = Color.parseColor("#1B2A4A");
    private static final int CARD2 = Color.parseColor("#16233F");
    private static final int CYAN = Color.parseColor("#00E5FF");
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.parseColor("#8A9ABF");
    private static final int GREEN = Color.parseColor("#22C55E");
    private static final int AMBER = Color.parseColor("#F59E0B");
    private static final int RED = Color.parseColor("#EF4444");
    private static final int DARK_TEXT = Color.parseColor("#0F172A");

    private static final int SCREEN_SETUP = 0, SCREEN_HOME = 1, SCREEN_MEMBERS = 2;
    private int screen = SCREEN_SETUP;

    private MfsStore store;
    private LinearLayout root;
    private long selStartMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new MfsStore(this);
        selStartMs = startOfToday();

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.setFillViewport(true);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(24));
        sv.addView(root);
        setContentView(sv);

        if (store.setupDone) showHome(); else showSetup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup form ke darmiyan wapas aane par form na ude; baaki screens refresh
        if (screen == SCREEN_HOME) showHome();
        else if (screen == SCREEN_MEMBERS) showMembers();
    }

    // ================= helpers =================

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(roundRect(CARD, 14));
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private Button button(String label, int bg, int fg) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(fg);
        b.setTextSize(15);
        b.setTypeface(null, android.graphics.Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(roundRect(bg, 24));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(10);
        b.setLayoutParams(lp);
        return b;
    }

    private EditText input(String hint, boolean number, boolean decimal) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(WHITE);
        e.setTextSize(15);
        e.setBackground(roundRect(CARD2, 10));
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        if (number) {
            e.setInputType(decimal
                    ? android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    : android.text.InputType.TYPE_CLASS_NUMBER);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    private Spinner operatorSpinner() {
        Spinner s = new Spinner(this);
        String[] ops = {"Jazz", "Zong", "Telenor", "Ufone"};
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ops) {
            @Override public View getView(int pos, View cv, ViewGroup parent) {
                TextView t = (TextView) super.getView(pos, cv, parent);
                t.setTextColor(WHITE);
                t.setTextSize(15);
                return t;
            }
            @Override public View getDropDownView(int pos, View cv, ViewGroup parent) {
                TextView t = (TextView) super.getDropDownView(pos, cv, parent);
                t.setTextColor(WHITE);
                t.setBackgroundColor(CARD);
                t.setTextSize(15);
                t.setPadding(dp(12), dp(10), dp(12), dp(10));
                return t;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(ad);
        s.setBackground(roundRect(CARD2, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(8);
        s.setLayoutParams(lp);
        return s;
    }

    /** Progress bar: track ke andar rang-daar hissa (0..1 se zyada bhi ho to full) */
    private View progressBar(double frac, int color) {
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setBackground(roundRect(CARD2, 8));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(16));
        tlp.topMargin = dp(10);
        track.setLayoutParams(tlp);
        double f = Math.max(0, Math.min(1, frac));
        View fill = new View(this);
        fill.setBackground(roundRect(color, 8));
        track.addView(fill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) f));
        View rest = new View(this);
        track.addView(rest, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) (1 - f)));
        return track;
    }

    private long startOfToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private String fmtDate(long ms) {
        return new SimpleDateFormat("d MMM yyyy", Locale.US).format(new Date(ms));
    }

    private void pickDate(TextView label) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selStartMs);
        new DatePickerDialog(this, (v, y, m, d) -> {
            Calendar s = Calendar.getInstance();
            s.set(y, m, d, 0, 0, 0);
            s.set(Calendar.MILLISECOND, 0);
            selStartMs = s.getTimeInMillis();
            label.setText("Shuru ki date: " + fmtDate(selStartMs));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void header() {
        TextView t = text("MyFiveShare", 26, WHITE, true);
        root.addView(t);
        TextView sub = text("Sharing package ka saaf hisaab — kis ne kitna use kiya, sab ko nazar aaye.",
                13, MUTED, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        sub.setLayoutParams(lp);
        root.addView(sub);
    }

    private void accessCardIfNeeded(LinearLayout parent) {
        if (UsageReader.hasUsageAccess(this)) return;
        LinearLayout c = card();
        c.setBackground(roundRect(Color.parseColor("#3A2A10"), 14));
        c.addView(text("⚠ Usage Access chahiye", 16, AMBER, true));
        TextView d = text("App tumhari asal data usage tabhi parh sakti hai jab tum ek dafa "
                + "Settings me ja kar MyFiveShare ko Usage Access do. Baghair is ke hisaab nahi ban sakta.",
                13, WHITE, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        d.setLayoutParams(lp);
        c.addView(d);
        Button b = button("Usage Access do", AMBER, DARK_TEXT);
        b.setOnClickListener(x -> UsageReader.openUsageSettings(this));
        c.addView(b);
        parent.addView(c);
    }

    // ================= SETUP =================

    private void showSetup() {
        screen = SCREEN_SETUP;
        root.removeAllViews();
        header();
        accessCardIfNeeded(root);

        // ---- Owner: package banao ----
        LinearLayout oc = card();
        oc.addView(text("Package banao — Owner", 17, CYAN, true));
        final EditText etName = input("Apna naam", false, false);
        final Spinner spOp = operatorSpinner();
        final EditText etGb = input("Kul data (GB) — masalan 100", true, true);
        final EditText etMembers = input("Kitne log share kar rahe hain — masalan 5", true, false);
        final EditText etDays = input("Package kitne din ka hai — masalan 30", true, false);
        etDays.setText("30");
        final TextView tvDate = text("Shuru ki date: " + fmtDate(selStartMs), 14, WHITE, false);
        Button btnDate = button("Shuru ki date chuno", CARD2, CYAN);
        btnDate.setOnClickListener(x -> pickDate(tvDate));
        Button btnCreate = button("Group banao", CYAN, DARK_TEXT);
        oc.addView(etName); oc.addView(spOp); oc.addView(etGb); oc.addView(etMembers); oc.addView(etDays);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.topMargin = dp(10);
        tvDate.setLayoutParams(dlp);
        oc.addView(tvDate); oc.addView(btnDate); oc.addView(btnCreate);
        root.addView(oc);

        btnCreate.setOnClickListener(x -> {
            String name = etName.getText().toString().trim();
            Double gb = parseDouble(etGb.getText().toString());
            Integer mem = parseInt(etMembers.getText().toString());
            Integer days = parseInt(etDays.getText().toString());
            if (name.isEmpty()) { toast("Pehle apna naam likho"); return; }
            if (gb == null || gb <= 0) { toast("Kul data (GB) sahi likho"); return; }
            if (mem == null || mem < 2) { toast("Members kam az kam 2 hon"); return; }
            if (days == null || days < 1) { toast("Package ke din sahi likho"); return; }
            store.owner = true;
            store.myName = name;
            store.operator = String.valueOf(spOp.getSelectedItem());
            store.totalGb = gb;
            store.members = mem;
            store.validityDays = days;
            store.cycleStartMs = selStartMs;
            store.joinCode = MfsStore.newJoinCode();
            store.setupDone = true;
            store.save();
            toast("Group ban gaya! Code: " + store.joinCode);
            showHome();
        });

        // ---- Member: join karo ----
        LinearLayout jc = card();
        jc.addView(text("Group join karo — Member", 17, CYAN, true));
        final EditText etJName = input("Apna naam", false, false);
        final EditText etJCode = input("Owner wala join code — masalan K7PX2A", false, false);
        final Spinner spJOp = operatorSpinner();
        final EditText etJGb = input("Kul data (GB) — owner se pooch kar", true, true);
        final EditText etJMembers = input("Kitne members hain — owner se pooch kar", true, false);
        final EditText etJDays = input("Package kitne din ka hai", true, false);
        etJDays.setText("30");
        final TextView tvJDate = text("Shuru ki date: " + fmtDate(selStartMs), 14, WHITE, false);
        Button btnJDate = button("Shuru ki date chuno", CARD2, CYAN);
        btnJDate.setOnClickListener(x -> pickDate(tvJDate));
        TextView note = text("Note: abhi ke version me package ki details owner se pooch kar dalni hain — "
                + "auto-sync agle version me aa raha hai, phir code dalte hi sab khud aa jayega.", 12, MUTED, false);
        Button btnJoin = button("Join karo", CYAN, DARK_TEXT);
        jc.addView(etJName); jc.addView(etJCode); jc.addView(spJOp); jc.addView(etJGb);
        jc.addView(etJMembers); jc.addView(etJDays);
        tvJDate.setLayoutParams(dlp);
        jc.addView(tvJDate); jc.addView(btnJDate);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = dp(10);
        note.setLayoutParams(nlp);
        jc.addView(note); jc.addView(btnJoin);
        root.addView(jc);

        btnJoin.setOnClickListener(x -> {
            String name = etJName.getText().toString().trim();
            String code = etJCode.getText().toString().trim().toUpperCase(Locale.US);
            Double gb = parseDouble(etJGb.getText().toString());
            Integer mem = parseInt(etJMembers.getText().toString());
            Integer days = parseInt(etJDays.getText().toString());
            if (name.isEmpty()) { toast("Pehle apna naam likho"); return; }
            if (code.length() < 4) { toast("Join code sahi likho"); return; }
            if (gb == null || gb <= 0) { toast("Kul data (GB) sahi likho"); return; }
            if (mem == null || mem < 2) { toast("Members kam az kam 2 hon"); return; }
            if (days == null || days < 1) { toast("Package ke din sahi likho"); return; }
            store.owner = false;
            store.myName = name;
            store.operator = String.valueOf(spJOp.getSelectedItem());
            store.totalGb = gb;
            store.members = mem;
            store.validityDays = days;
            store.cycleStartMs = selStartMs;
            store.joinCode = code;
            store.setupDone = true;
            store.save();
            toast("Join ho gaya!");
            showHome();
        });

        TextView foot = text("Imandari: ye app kisi ka data rok nahi sakti — sirf hisaab dikhati hai. "
                + "Har member ko apne phone par app lagani hogi, tabhi us ka istemal gina jayega.",
                12, MUTED, false);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.topMargin = dp(14);
        foot.setLayoutParams(flp);
        root.addView(foot);
    }

    // ================= HOME =================

    private void showHome() {
        screen = SCREEN_HOME;
        store = new MfsStore(this);
        root.removeAllViews();
        header();
        accessCardIfNeeded(root);

        boolean access = UsageReader.hasUsageAccess(this);
        long usedBytes = access
                ? UsageReader.mobileUsageBytes(this, store.cycleStartMs, System.currentTimeMillis())
                : -1;
        double usedGb = UsageReader.toGb(usedBytes);
        double fair = store.fairShareGb();
        double pct = (usedGb >= 0 && fair > 0) ? usedGb / fair : -1;

        // Group card
        LinearLayout gc = card();
        gc.addView(text(store.operator + " Sharing Package • " + (store.owner ? "Owner" : "Member"),
                15, CYAN, true));
        TextView code = text("Join code: " + store.joinCode, 20, WHITE, true);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = dp(8);
        code.setLayoutParams(clp);
        gc.addView(code);
        gc.addView(text(String.format(Locale.US,
                "Kul package: %.0f GB • Members: %d • Ek ka hissa: %.1f GB",
                store.totalGb, store.members, fair), 13, MUTED, false));
        gc.addView(progressBar(store.cycleProgress(), CYAN));
        int left = store.daysLeft();
        gc.addView(text(left > 0
                        ? "Cycle: " + fmtDate(store.cycleStartMs) + " se • " + left + " din baqi"
                        : "Cycle khatam ho gaya — naya package laga kar setup dobara karo",
                13, left > 0 ? MUTED : RED, false));
        Button btnCode = button("Join code share karo", CARD2, CYAN);
        btnCode.setOnClickListener(x -> shareText("MyFiveShare group code: " + store.joinCode
                + "\nApp laga kar ye code dalo, hum sab ka sharing hisaab ek jagah dikhega."));
        gc.addView(btnCode);
        root.addView(gc);

        // Mera istemal card
        LinearLayout uc = card();
        uc.addView(text("Mera istemal (" + store.myName + ")", 17, WHITE, true));
        if (!access) {
            uc.addView(text("Usage Access ke baghair istemal nahi parha ja sakta — upar wala button dabao.",
                    13, AMBER, false));
        } else if (usedGb < 0) {
            uc.addView(text("Abhi usage parhne me masla hai — thodi dair baad Refresh karo.", 13, AMBER, false));
        } else {
            TextView big = text(UsageReader.humanGb(usedBytes), 34, WHITE, true);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.topMargin = dp(6);
            big.setLayoutParams(blp);
            uc.addView(big);
            uc.addView(text(String.format(Locale.US, "Mera hissa: %.1f GB • Use: %.0f%%",
                    fair, Math.max(0, pct * 100)), 13, MUTED, false));
            int barColor = pct >= 1 ? RED : (pct >= 0.8 ? AMBER : GREEN);
            uc.addView(progressBar(pct, barColor));
            if (pct >= 1) {
                TextView w = text("⚠ Tum apne hisse se ZYADA use kar chuke ho!", 15, RED, true);
                w.setLayoutParams(blp);
                uc.addView(w);
            } else if (pct >= 0.8) {
                uc.addView(text("Hissa khatam hone wala hai — sambhal kar use karo.", 13, AMBER, false));
            }
            if (usedBytes == 0) {
                uc.addView(text("Abhi 0 aya hai. Agar data use kiya hai to kuch dair baad Refresh karo — "
                        + "kuch phones par stats dair se update hote hain.", 12, MUTED, false));
            }
        }
        root.addView(uc);

        // Actions
        Button btnReport = button("Report share karo (WhatsApp)", GREEN, DARK_TEXT);
        btnReport.setOnClickListener(x -> shareText(reportText(usedBytes, usedGb, pct)));
        root.addView(btnReport);
        Button btnMembers = button("Members dekho", CARD, CYAN);
        btnMembers.setOnClickListener(x -> showMembers());
        root.addView(btnMembers);
        Button btnRefresh = button("Refresh", CARD, CYAN);
        btnRefresh.setOnClickListener(x -> showHome());
        root.addView(btnRefresh);
        Button btnReset = button("Naya group / Setup badlo", CARD, RED);
        btnReset.setOnClickListener(x -> new AlertDialog.Builder(this)
                .setTitle("Setup badlo?")
                .setMessage("Purana group aur hisaab is phone se hat jayega. Aage barhun?")
                .setPositiveButton("Haan, badlo", (d, w) -> {
                    store.clearAll();
                    showSetup();
                })
                .setNegativeButton("Cancel", null)
                .show());
        root.addView(btnReset);

        TextView note = text("Note: doosre members ka istemal un ke phones se sync hone par ek saath dikhega — "
                + "ye suvidha agle version me aa rahi hai. Abhi har phone apna hisaab dikhata hai.",
                12, MUTED, false);
        LinearLayout.LayoutParams nlp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp2.topMargin = dp(14);
        note.setLayoutParams(nlp2);
        root.addView(note);
    }

    // ================= MEMBERS =================

    private void showMembers() {
        screen = SCREEN_MEMBERS;
        root.removeAllViews();
        header();
        Button back = button("← Wapas", CARD, CYAN);
        back.setOnClickListener(x -> showHome());
        root.addView(back);

        boolean access = UsageReader.hasUsageAccess(this);
        long usedBytes = access
                ? UsageReader.mobileUsageBytes(this, store.cycleStartMs, System.currentTimeMillis())
                : -1;

        LinearLayout mc = card();
        mc.addView(text("Members (" + store.members + ")", 17, CYAN, true));
        TextView me = text("• " + store.myName + " (tum) — "
                        + (usedBytes >= 0 ? UsageReader.humanGb(usedBytes) : "data baqi"),
                15, WHITE, false);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = dp(8);
        me.setLayoutParams(mlp);
        mc.addView(me);
        for (int i = 2; i <= store.members; i++) {
            TextView r = text("• Member " + i + " — data un ke phone se sync hone par aayega",
                    14, MUTED, false);
            r.setLayoutParams(mlp);
            mc.addView(r);
        }
        root.addView(mc);
        TextView n = text("Jo member app hi na lagaye ya sync na kare, us ka istemal nazar nahi aayega — "
                + "ye khaali line khud ek ishara hai.", 12, MUTED, false);
        n.setLayoutParams(mlp);
        root.addView(n);
    }

    // ================= share / report =================

    private String reportText(long usedBytes, double usedGb, double pct) {
        StringBuilder sb = new StringBuilder();
        sb.append("MyFiveShare Report\n");
        sb.append(store.operator).append(" package: ")
                .append(String.format(Locale.US, "%.0f", store.totalGb)).append(" GB • Members: ")
                .append(store.members).append("\n");
        sb.append("Mera hissa: ").append(String.format(Locale.US, "%.1f", store.fairShareGb())).append(" GB\n");
        if (usedGb >= 0) {
            sb.append("Maine use kiya: ").append(UsageReader.humanGb(usedBytes))
                    .append(" (").append(String.format(Locale.US, "%.0f", pct * 100)).append("%)");
            if (pct >= 1) sb.append(" — hisse se ZYADA");
            sb.append("\n");
        } else {
            sb.append("Mera istemal abhi parha nahi ja saka.\n");
        }
        int left = store.daysLeft();
        sb.append(left > 0 ? ("Cycle me " + left + " din baqi.") : "Cycle khatam.");
        sb.append("\n— MyFiveShare app se");
        return sb.toString();
    }

    private void shareText(String s) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, s);
            startActivity(Intent.createChooser(i, "Share karo"));
        } catch (Exception ignored) {}
    }

    private Double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
}

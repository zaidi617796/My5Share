package com.myfiveshare.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MyFiveShare v2 — auto-sync group hisaab.
 * Tabs: Home (mera data/minutes/SMS vs hissa) | Members (sab ka synced hisaab) | Admin/Member.
 * Settings (gear): zubaan English / Urdu. Theme: SIMOSA (Jazz World) jaisi halki + laal/bengni.
 * Group sync Supabase REST se (MfsSync). UI poori programmatic.
 */
public class MfsActivity extends AppCompatActivity {

    // SIMOSA jaisi halki theme: safed cards, halka pas-manzar, Jazz laal + bengni
    private static final int BG = Color.parseColor("#F4F5FA");
    private static final int CARD = Color.WHITE;
    private static final int STROKE = Color.parseColor("#E2E5F1");
    private static final int TEXT = Color.parseColor("#191932");
    private static final int MUTED = Color.parseColor("#6E7191");
    private static final int RED = Color.parseColor("#E4002B");
    private static final int VIOLET = Color.parseColor("#7B2FF7");
    private static final int INPUT_BG = Color.parseColor("#EEF0F7");
    private static final int TRACK = Color.parseColor("#E8EAF3");
    private static final int GREEN = Color.parseColor("#1FA84A");
    private static final int AMBER = Color.parseColor("#F59E0B");
    private static final int OVER = Color.parseColor("#D90429");
    private static final int AMBER_BG = Color.parseColor("#FFF3D6");
    private static final int TAB_TINT = Color.parseColor("#FDECEF");

    private static final int REQ_CALL = 41, REQ_SMS = 42;

    private MfsStore store;
    private LinearLayout content;
    private LinearLayout tabBar;
    private final TextView[] tabViews = new TextView[3];
    private int tab = 0;
    private boolean inSettings = false;
    private int renderGen = 0;
    private long selStartMs;
    private List<MfsSync.MemberRow> lastMembers;

    private String L(String k) { return MfsLang.s(k); }

    private String F(String k, Object... a) { return String.format(Locale.US, L(k), a); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new MfsStore(this);
        MfsLang.lang = store.lang;
        selStartMs = startOfToday();

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(BG);

        // Upar wali patti: gradient (laal -> bengni), naam + gear
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable grad = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{RED, VIOLET});
        top.setBackground(grad);
        top.setPadding(dp(16), dp(12), dp(10), dp(12));
        TextView title = new TextView(this);
        title.setText("MyFiveShare");
        title.setTextColor(Color.WHITE);
        title.setTextSize(21);
        title.setTypeface(null, Typeface.BOLD);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView gear = new TextView(this);
        gear.setText("⚙");
        gear.setTextColor(Color.WHITE);
        gear.setTextSize(24);
        gear.setPadding(dp(10), dp(2), dp(10), dp(2));
        gear.setOnClickListener(v -> showSettings());
        top.addView(gear);
        shell.addView(top);

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(24));
        sv.addView(content);
        shell.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // Neeche tabs: Home | Members | Admin/Member
        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Color.WHITE);
        tabBar.setPadding(dp(6), dp(6), dp(6), dp(8));
        String[] names = {"tab_home", "tab_members", "tab_admin"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = new TextView(this);
            t.setGravity(Gravity.CENTER);
            t.setTextSize(14);
            t.setPadding(dp(4), dp(10), dp(4), dp(10));
            t.setOnClickListener(v -> showTab(idx));
            tabViews[i] = t;
            tabBar.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        shell.addView(tabBar);
        setContentView(shell);

        if (store.setupDone) showTab(0); else showSetup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ijazat day kar wapas aane par screen taza ho jaye (setup form na ude)
        if (store != null && store.setupDone && !inSettings) showTab(tab);
    }

    // ================= helpers =================

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundRect(int color, int radiusDp, boolean stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        if (stroke) d.setStroke(dp(1), STROKE);
        return d;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private TextView spaced(TextView t, int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(topDp);
        t.setLayoutParams(lp);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(roundRect(CARD, 14, true));
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    private Button button(String label, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        if (primary) {
            b.setTextColor(Color.WHITE);
            b.setBackground(roundRect(RED, 24, false));
        } else {
            b.setTextColor(TEXT);
            b.setBackground(roundRect(Color.WHITE, 24, true));
        }
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
        e.setTextColor(TEXT);
        e.setTextSize(15);
        e.setBackground(roundRect(INPUT_BG, 10, false));
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
                t.setTextColor(TEXT);
                t.setTextSize(15);
                return t;
            }
            @Override public View getDropDownView(int pos, View cv, ViewGroup parent) {
                TextView t = (TextView) super.getDropDownView(pos, cv, parent);
                t.setTextColor(TEXT);
                t.setBackgroundColor(Color.WHITE);
                t.setTextSize(15);
                t.setPadding(dp(12), dp(10), dp(12), dp(10));
                return t;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(ad);
        s.setBackground(roundRect(INPUT_BG, 10, false));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(8);
        s.setLayoutParams(lp);
        return s;
    }

    private View progressBar(double frac, int color) {
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setBackground(roundRect(TRACK, 8, false));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(14));
        tlp.topMargin = dp(10);
        track.setLayoutParams(tlp);
        double f = Math.max(0, Math.min(1, frac));
        View fill = new View(this);
        fill.setBackground(roundRect(color, 8, false));
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
            label.setText(L("start_date") + fmtDate(selStartMs));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void accessCardIfNeeded(LinearLayout parent) {
        if (UsageReader.hasUsageAccess(this)) return;
        LinearLayout c = card();
        c.setBackground(roundRect(AMBER_BG, 14, false));
        c.addView(text(L("access_title"), 16, TEXT, true));
        c.addView(spaced(text(L("access_body"), 13, TEXT, false), 6));
        Button b = new Button(this);
        b.setText(L("access_btn"));
        b.setTextColor(TEXT);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(roundRect(AMBER, 24, false));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        lp.topMargin = dp(10);
        b.setLayoutParams(lp);
        b.setOnClickListener(x -> UsageReader.openUsageSettings(this));
        c.addView(b);
        parent.addView(c);
    }

    private Double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private int parseIntOr0(String s) {
        Integer v = parseInt(s);
        return v == null ? 0 : Math.max(0, v);
    }

    /** Abhi ki meri usage teeno cheezon me — ijazat na ho to us hissa me 0/-1 ka saaf farq */
    private long myDataBytes() {
        if (!UsageReader.hasUsageAccess(this)) return -1;
        return UsageReader.mobileUsageBytes(this, store.cycleStartMs, System.currentTimeMillis());
    }

    /** Mere minutes ka on-net/off-net split (ijazat na ho to {-1,-1}) */
    private int[] myMinsSplit() {
        return UsageReader.callMinutesSplit(this, store.cycleStartMs, store.operator);
    }

    private int myMins() {
        int[] sp = myMinsSplit();
        return sp[0] < 0 ? -1 : sp[0] + sp[1];
    }

    private int mySms() {
        return UsageReader.smsSentCount(this, store.cycleStartMs);
    }

    private void shareText(String s) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, s);
            startActivity(Intent.createChooser(i, "Share"));
        } catch (Exception ignored) {}
    }

    // ================= SETUP =================

    private void showSetup() {
        inSettings = false;
        renderGen++;
        tabBar.setVisibility(View.GONE);
        content.removeAllViews();
        content.addView(text(L("tagline"), 13, MUTED, false));
        accessCardIfNeeded(content);

        // ---- Admin: package banao ----
        LinearLayout oc = card();
        oc.addView(text(L("create_title"), 17, RED, true));
        final EditText etName = input(L("your_name"), false, false);
        final Spinner spOp = operatorSpinner();
        final EditText etGb = input(L("total_gb_hint"), true, true);
        final EditText etOnnet = input(L("total_onnet_hint"), true, false);
        final EditText etOffnet = input(L("total_offnet_hint"), true, false);
        final EditText etSms = input(L("total_sms_hint"), true, false);
        final EditText etMembers = input(L("members_hint"), true, false);
        final EditText etDays = input(L("days_hint"), true, false);
        etDays.setText("30");
        final TextView tvDate = text(L("start_date") + fmtDate(selStartMs), 14, TEXT, false);
        Button btnDate = button(L("pick_date"), false);
        btnDate.setOnClickListener(x -> pickDate(tvDate));
        final Button btnCreate = button(L("create_btn"), true);
        oc.addView(etName); oc.addView(spOp); oc.addView(etGb); oc.addView(etOnnet); oc.addView(etOffnet); oc.addView(etSms);
        oc.addView(etMembers); oc.addView(etDays);
        oc.addView(spaced(tvDate, 10));
        oc.addView(btnDate); oc.addView(btnCreate);
        content.addView(oc);

        btnCreate.setOnClickListener(x -> {
            String name = etName.getText().toString().trim();
            Double gb = parseDouble(etGb.getText().toString());
            Integer mem = parseInt(etMembers.getText().toString());
            Integer days = parseInt(etDays.getText().toString());
            if (name.isEmpty()) { toast(L("err_name")); return; }
            if (gb == null || gb <= 0) { toast(L("err_gb")); return; }
            if (mem == null || mem < 2) { toast(L("err_members")); return; }
            if (days == null || days < 1) { toast(L("err_days")); return; }
            store.owner = true;
            store.myName = name;
            store.operator = String.valueOf(spOp.getSelectedItem());
            store.totalGb = gb;
            store.totalOnnetMins = parseIntOr0(etOnnet.getText().toString());
            store.totalOffnetMins = parseIntOr0(etOffnet.getText().toString());
            store.totalMins = store.totalOnnetMins + store.totalOffnetMins;
            store.totalSms = parseIntOr0(etSms.getText().toString());
            store.members = mem;
            store.validityDays = days;
            store.cycleStartMs = selStartMs;
            store.joinCode = MfsStore.newJoinCode();
            btnCreate.setEnabled(false);
            btnCreate.setText(L("creating"));
            long db = myDataBytes();
            int[] sp0 = myMinsSplit();
            MfsSync.createGroup(store, Math.max(0, db), Math.max(0, myMins()),
                    Math.max(0, sp0[0]), Math.max(0, sp0[1]), Math.max(0, mySms()),
                    new MfsSync.Cb<Boolean>() {
                        @Override public void ok(Boolean v) {
                            store.setupDone = true;
                            store.save();
                            toast(L("created_toast") + store.joinCode);
                            showTab(0);
                        }
                        @Override public void fail(String msg) {
                            btnCreate.setEnabled(true);
                            btnCreate.setText(L("create_btn"));
                            toast(L("err_net"));
                        }
                    });
        });

        // ---- Member: sirf code se join (sab tafseel khud aayegi) ----
        LinearLayout jc = card();
        jc.addView(text(L("join_title"), 17, RED, true));
        final EditText etJName = input(L("your_name"), false, false);
        final EditText etJCode = input(L("join_code_hint"), false, false);
        final Button btnJoin = button(L("join_btn"), true);
        jc.addView(etJName); jc.addView(etJCode);
        jc.addView(spaced(text(L("join_auto_note"), 12, MUTED, false), 10));
        jc.addView(btnJoin);
        content.addView(jc);

        btnJoin.setOnClickListener(x -> {
            String name = etJName.getText().toString().trim();
            String code = etJCode.getText().toString().trim().toUpperCase(Locale.US);
            if (name.isEmpty()) { toast(L("err_name")); return; }
            if (code.length() < 4) { toast(L("err_code")); return; }
            btnJoin.setEnabled(false);
            btnJoin.setText(L("joining"));
            MfsSync.findGroup(code, new MfsSync.Cb<MfsSync.GroupInfo>() {
                @Override public void ok(MfsSync.GroupInfo gi) {
                    if (gi == null) {
                        btnJoin.setEnabled(true);
                        btnJoin.setText(L("join_btn"));
                        toast(L("err_notfound"));
                        return;
                    }
                    store.owner = false;
                    store.myName = name;
                    store.operator = gi.operator;
                    store.totalGb = gi.totalGb;
                    store.totalOnnetMins = gi.totalOnnetMins;
                    store.totalOffnetMins = gi.totalOffnetMins;
                    store.totalMins = gi.totalOnnetMins + gi.totalOffnetMins;
                    store.totalSms = gi.totalSms;
                    store.members = gi.membersCount;
                    store.validityDays = gi.validityDays;
                    store.cycleStartMs = gi.cycleStartMs;
                    store.joinCode = gi.code;
                    long db = myDataBytes();
                    int[] spj = myMinsSplit();
                    MfsSync.upsertMember(store, Math.max(0, db), Math.max(0, myMins()),
                            Math.max(0, spj[0]), Math.max(0, spj[1]), Math.max(0, mySms()),
                            new MfsSync.Cb<Boolean>() {
                                @Override public void ok(Boolean v) {
                                    store.setupDone = true;
                                    store.save();
                                    toast(L("joined_toast"));
                                    showTab(0);
                                }
                                @Override public void fail(String msg) {
                                    btnJoin.setEnabled(true);
                                    btnJoin.setText(L("join_btn"));
                                    toast(L("err_net"));
                                }
                            });
                }
                @Override public void fail(String msg) {
                    btnJoin.setEnabled(true);
                    btnJoin.setText(L("join_btn"));
                    toast(L("err_net"));
                }
            });
        });

        // Note: Zahid ke hukm par "imandari wali baat" (disclaimer) app se hata di gayi hai (2026-10-28)
    }

    // ================= TABS =================

    private void showTab(int i) {
        tab = i;
        inSettings = false;
        store = new MfsStore(this);
        MfsLang.lang = store.lang;
        tabBar.setVisibility(View.VISIBLE);
        String[] names = {"tab_home", "tab_members", store.owner ? "tab_admin" : "tab_member"};
        for (int k = 0; k < 3; k++) {
            tabViews[k].setText(L(names[k]));
            boolean sel = k == i;
            tabViews[k].setTextColor(sel ? RED : MUTED);
            tabViews[k].setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            tabViews[k].setBackground(sel ? roundRect(TAB_TINT, 18, false) : null);
        }
        if (i == 0) showDashboard();
        else if (i == 1) showMembers();
        else showRole();
    }

    // ---------- Dashboard ----------

    private void showDashboard() {
        renderGen++;
        final int gen = renderGen;
        content.removeAllViews();
        accessCardIfNeeded(content);

        final long dataBytes = myDataBytes();
        final int[] minsSplit = myMinsSplit();
        final int mins = minsSplit[0] < 0 ? -1 : minsSplit[0] + minsSplit[1];
        final int sms = mySms();
        final double fairGb = store.fairShareGb();

        // Group card
        LinearLayout gc = card();
        gc.addView(text(store.operator + L("pkg_suffix") + " • " + L(store.owner ? "role_admin" : "role_member"),
                15, VIOLET, true));
        gc.addView(spaced(text(L("join_code_label") + store.joinCode, 20, TEXT, true), 8));
        gc.addView(text(F("pkg_line", store.totalGb, store.members, fairGb), 13, MUTED, false));
        if (store.totalOnnetMins > 0 || store.totalOffnetMins > 0) {
            gc.addView(text(L("onnet_u") + ": " + store.totalOnnetMins
                    + " • " + L("offnet_u") + ": " + store.totalOffnetMins, 13, MUTED, false));
        }
        if (store.totalSms > 0) {
            gc.addView(text(L("sms_u") + ": " + store.totalSms, 13, MUTED, false));
        }
        gc.addView(progressBar(store.cycleProgress(), VIOLET));
        int left = store.daysLeft();
        gc.addView(text(left > 0 ? F("cycle_left", fmtDate(store.cycleStartMs), left) : L("cycle_ended"),
                13, left > 0 ? MUTED : OVER, false));
        final TextView syncLine = spaced(text(L("syncing"), 12, MUTED, false), 8);
        gc.addView(syncLine);
        Button btnCode = button(L("share_code"), false);
        btnCode.setOnClickListener(x -> shareText(F("share_code_msg", store.joinCode)));
        gc.addView(btnCode);
        content.addView(gc);

        // Data card
        content.addView(usageCard(L("data_u"),
                dataBytes < 0 ? null : UsageReader.humanGb(dataBytes),
                dataBytes < 0 ? -1 : UsageReader.toGb(dataBytes), fairGb, true, dataBytes == 0));

        // Minutes cards — on-net aur off-net ALAG (Zahid, 2026-10-28)
        if (mins < 0) {
            content.addView(permCard(L("mins_u"), L("perm_call"), REQ_CALL,
                    android.Manifest.permission.READ_CALL_LOG));
        } else {
            if (store.totalOnnetMins > 0 || minsSplit[0] > 0) {
                content.addView(usageCard(L("onnet_u"), minsSplit[0] + " " + L("mins_u"),
                        minsSplit[0], store.fairShareOnnet(), false, false));
            }
            if (store.totalOffnetMins > 0 || minsSplit[1] > 0) {
                content.addView(usageCard(L("offnet_u"), minsSplit[1] + " " + L("mins_u"),
                        minsSplit[1], store.fairShareOffnet(), false, false));
            }
            if (store.totalOnnetMins == 0 && store.totalOffnetMins == 0
                    && minsSplit[0] == 0 && minsSplit[1] == 0) {
                content.addView(usageCard(L("mins_u"), "0 " + L("mins_u"), 0, 0, false, false));
            }
        }

        // SMS card
        if (store.totalSms > 0 || sms >= 0) {
            if (sms < 0) content.addView(permCard(L("sms_u"), L("perm_sms"), REQ_SMS,
                    android.Manifest.permission.READ_SMS));
            else content.addView(usageCard(L("sms_u"), sms + " " + L("sms_u"),
                    sms, store.fairShareSms(), false, false));
        } else {
            content.addView(permCard(L("sms_u"), L("perm_sms"), REQ_SMS,
                    android.Manifest.permission.READ_SMS));
        }

        Button btnReport = button(L("report_btn"), true);
        btnReport.setOnClickListener(x -> shareText(reportText(dataBytes, minsSplit, sms)));
        content.addView(btnReport);
        Button btnRefresh = button(L("refresh"), false);
        btnRefresh.setOnClickListener(x -> showDashboard());
        content.addView(btnRefresh);

        // Sync: pehle meri usage bhejo, phir poora group lao
        MfsSync.upsertMember(store, Math.max(0, dataBytes), Math.max(0, mins),
                Math.max(0, minsSplit[0]), Math.max(0, minsSplit[1]), Math.max(0, sms),
                new MfsSync.Cb<Boolean>() {
                    @Override public void ok(Boolean v) { fetchGroup(gen, syncLine); }
                    @Override public void fail(String msg) { fetchGroup(gen, syncLine); }
                });
    }

    private void fetchGroup(final int gen, final TextView syncLine) {
        MfsSync.fetchMembers(store.joinCode, new MfsSync.Cb<List<MfsSync.MemberRow>>() {
            @Override public void ok(List<MfsSync.MemberRow> rows) {
                lastMembers = rows;
                cacheMembers(rows);
                if (gen != renderGen || tab != 0) return;
                long sum = 0;
                for (MfsSync.MemberRow r : rows) sum += r.usedBytes;
                syncLine.setText(F("group_used", UsageReader.humanGb(sum), store.totalGb));
            }
            @Override public void fail(String msg) {
                if (gen != renderGen || tab != 0) return;
                syncLine.setText(L("sync_fail"));
            }
        });
    }

    /** Ek istemal wala card: bara number, hissa, rang-daar bar, warning */
    private LinearLayout usageCard(String title, String bigText, double used, double fair,
                                   boolean isData, boolean zeroData) {
        LinearLayout c = card();
        c.addView(text(title, 16, TEXT, true));
        if (bigText == null) {
            c.addView(spaced(text(isData ? L("access_short") : L("usage_fail"), 13, AMBER, false), 6));
            return c;
        }
        c.addView(spaced(text(bigText, 32, TEXT, true), 4));
        if (fair > 0) {
            double pct = used / fair;
            c.addView(text(F("share_line2", fair, Math.max(0, pct * 100)), 13, MUTED, false));
            int color = pct >= 1 ? OVER : (pct >= 0.8 ? AMBER : GREEN);
            c.addView(progressBar(pct, color));
            if (pct >= 1) c.addView(spaced(text(L("over_warn"), 14, OVER, true), 8));
            else if (pct >= 0.8) c.addView(spaced(text(L("almost_warn"), 13, AMBER, false), 8));
        }
        if (zeroData) c.addView(spaced(text(L("zero_note"), 12, MUTED, false), 8));
        return c;
    }

    /** Ijazat mangne wala card (minutes / SMS) */
    private LinearLayout permCard(String title, String body, final int reqCode, final String perm) {
        LinearLayout c = card();
        c.addView(text(title, 16, TEXT, true));
        c.addView(spaced(text(body, 13, MUTED, false), 6));
        Button b = button(L("grant"), false);
        b.setOnClickListener(x -> requestPermissions(new String[]{perm}, reqCode));
        c.addView(b);
        return c;
    }

    // ---------- Members ----------

    private void showMembers() {
        renderGen++;
        final int gen = renderGen;
        content.removeAllViews();
        if (lastMembers == null) lastMembers = parseCache();
        renderMembers(gen);
        MfsSync.fetchMembers(store.joinCode, new MfsSync.Cb<List<MfsSync.MemberRow>>() {
            @Override public void ok(List<MfsSync.MemberRow> rows) {
                lastMembers = rows;
                cacheMembers(rows);
                if (gen == renderGen && tab == 1) renderMembers(gen);
            }
            @Override public void fail(String msg) {
                if (gen == renderGen && tab == 1) renderMembers(gen);
            }
        });
    }

    private void renderMembers(int gen) {
        content.removeAllViews();
        content.addView(text(F("members_title", store.members), 17, VIOLET, true));
        List<MfsSync.MemberRow> rows = lastMembers;
        if (rows == null) {
            LinearLayout c = card();
            c.addView(text(L("syncing"), 13, MUTED, false));
            content.addView(c);
        } else {
            for (MfsSync.MemberRow r : rows) content.addView(memberCard(r));
            for (int i = rows.size() + 1; i <= store.members; i++) {
                LinearLayout c = card();
                c.addView(text(L("tab_member") + " " + i + " — " + L("not_joined"), 15, MUTED, true));
                c.addView(spaced(text(L("member_data_note"), 12, MUTED, false), 4));
                content.addView(c);
            }
        }
        Button btnRefresh = button(L("refresh"), false);
        btnRefresh.setOnClickListener(x -> showMembers());
        content.addView(btnRefresh);
    }

    /** SIMOSA-style member card: naam + data %, phir har cheez ki apni line/bar */
    private LinearLayout memberCard(MfsSync.MemberRow r) {
        LinearLayout c = card();
        boolean me = r.deviceId.equals(store.deviceId);
        double fairGb = store.fairShareGb();
        double gb = UsageReader.toGb(r.usedBytes);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(text(r.name + (me ? L("you_suffix") : ""), 16, TEXT, true),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (fairGb > 0 && gb >= 0) {
            double pct = gb / fairGb;
            int col = pct >= 1 ? OVER : (pct >= 0.8 ? AMBER : GREEN);
            head.addView(text(String.format(Locale.US, "%.0f%%", pct * 100), 18, col, true));
        }
        c.addView(head);

        long shareBytes = (long) (fairGb * 1073741824.0);
        statRow(c, L("data_u"), UsageReader.humanGb(r.usedBytes), gb, fairGb,
                UsageReader.humanGb(Math.max(0, shareBytes - Math.max(0, r.usedBytes))),
                String.format(Locale.US, "%.1f GB", fairGb));
        statRow(c, L("onnet_u"), r.usedOnnetMins + " " + L("mins_u"),
                r.usedOnnetMins, store.fairShareOnnet(),
                Math.max(0, (int) Math.round(store.fairShareOnnet() - r.usedOnnetMins)) + " " + L("mins_u"),
                String.format(Locale.US, "%.0f ", store.fairShareOnnet()) + L("mins_u"));
        statRow(c, L("offnet_u"), r.usedOffnetMins + " " + L("mins_u"),
                r.usedOffnetMins, store.fairShareOffnet(),
                Math.max(0, (int) Math.round(store.fairShareOffnet() - r.usedOffnetMins)) + " " + L("mins_u"),
                String.format(Locale.US, "%.0f ", store.fairShareOffnet()) + L("mins_u"));
        statRow(c, L("sms_u"), r.usedSms + " " + L("sms_u"),
                r.usedSms, store.fairShareSms(),
                Math.max(0, (int) Math.round(store.fairShareSms() - r.usedSms)) + " " + L("sms_u"),
                String.format(Locale.US, "%.0f ", store.fairShareSms()) + L("sms_u"));
        c.addView(spaced(text(F("last_sync", agoText(r.updatedAtMs)), 11, MUTED, false), 8));
        return c;
    }

    /** Ek istemal wali line: label, bara used + %, patli bar, "X left of Y" */
    private void statRow(LinearLayout parent, String label, String usedText,
                         double used, double fair, String leftText, String shareText) {
        parent.addView(spaced(text(label, 12, MUTED, true), 10));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(text(usedText, 17, TEXT, true),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (fair > 0) {
            double pct = used / fair;
            int col = pct >= 1 ? OVER : (pct >= 0.8 ? AMBER : GREEN);
            row.addView(text(String.format(Locale.US, "%.0f%%", pct * 100), 13, col, true));
        }
        parent.addView(row);
        if (fair > 0) {
            double pct = used / fair;
            int col = pct >= 1 ? OVER : (pct >= 0.8 ? AMBER : GREEN);
            parent.addView(progressBarSlim(pct, col));
            parent.addView(text(leftText + " " + L("left_of") + " " + shareText, 11, MUTED, false));
        }
    }

    private View progressBarSlim(double frac, int color) {
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setBackground(roundRect(TRACK, 6, false));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        tlp.topMargin = dp(4);
        track.setLayoutParams(tlp);
        double f = Math.max(0, Math.min(1, frac));
        View fill = new View(this);
        fill.setBackground(roundRect(color, 6, false));
        track.addView(fill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) f));
        View rest = new View(this);
        track.addView(rest, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) (1 - f)));
        return track;
    }

    private String agoText(long updatedAtMs) {
        if (updatedAtMs <= 0) return L("just_now");
        long minsAgo = (System.currentTimeMillis() - updatedAtMs) / 60000;
        if (minsAgo < 1) return L("just_now");
        if (minsAgo < 60) return minsAgo + L("mins");
        if (minsAgo < 1440) return (minsAgo / 60) + L("hours");
        return (minsAgo / 1440) + L("days_u");
    }

    private void cacheMembers(List<MfsSync.MemberRow> rows) {
        try {
            JSONArray arr = new JSONArray();
            for (MfsSync.MemberRow r : rows) {
                JSONObject o = new JSONObject();
                o.put("n", r.name);
                o.put("d", r.deviceId);
                o.put("b", r.usedBytes);
                o.put("m", r.usedMins);
                o.put("on", r.usedOnnetMins);
                o.put("of", r.usedOffnetMins);
                o.put("s", r.usedSms);
                o.put("u", r.updatedAtMs);
                arr.put(o);
            }
            store.membersCacheJson = arr.toString();
            store.membersCacheAt = System.currentTimeMillis();
            store.save();
        } catch (Exception ignored) {}
    }

    private List<MfsSync.MemberRow> parseCache() {
        try {
            if (store.membersCacheJson == null || store.membersCacheJson.isEmpty()) return null;
            JSONArray arr = new JSONArray(store.membersCacheJson);
            List<MfsSync.MemberRow> out = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                MfsSync.MemberRow r = new MfsSync.MemberRow();
                r.name = o.getString("n");
                r.deviceId = o.getString("d");
                r.usedBytes = o.getLong("b");
                r.usedMins = o.optInt("m", 0);
                r.usedOnnetMins = o.optInt("on", 0);
                r.usedOffnetMins = o.optInt("of", 0);
                if (r.usedOnnetMins == 0 && r.usedOffnetMins == 0 && r.usedMins > 0) {
                    r.usedOnnetMins = r.usedMins;
                }
                r.usedSms = o.optInt("s", 0);
                r.updatedAtMs = o.optLong("u", 0);
                out.add(r);
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- Admin / Member tab ----------

    private void showRole() {
        content.removeAllViews();
        LinearLayout c = card();
        c.addView(text(L(store.owner ? "tab_admin" : "tab_member"), 17, VIOLET, true));
        c.addView(spaced(text(store.operator + L("pkg_suffix"), 14, TEXT, false), 8));
        c.addView(text(L("join_code_label") + store.joinCode, 15, TEXT, true));
        c.addView(text(F("pkg_line", store.totalGb, store.members, store.fairShareGb()), 13, MUTED, false));
        content.addView(c);

        if (store.owner) {
            Button btnCycle = button(L("new_cycle"), true);
            btnCycle.setOnClickListener(x -> new AlertDialog.Builder(this)
                    .setTitle(L("confirm_cycle_title"))
                    .setMessage(L("confirm_cycle_msg"))
                    .setPositiveButton(L("yes_go"), (d, w) -> {
                        store.cycleStartMs = startOfToday();
                        store.save();
                        MfsSync.updateCycleStart(store, new MfsSync.Cb<Boolean>() {
                            @Override public void ok(Boolean v) { toast(L("new_cycle_done")); showTab(0); }
                            @Override public void fail(String msg) { toast(L("err_net")); }
                        });
                    })
                    .setNegativeButton(L("cancel"), null)
                    .show());
            content.addView(btnCycle);

            Button btnDel = button(L("delete_group"), false);
            btnDel.setTextColor(OVER);
            btnDel.setOnClickListener(x -> new AlertDialog.Builder(this)
                    .setTitle(L("confirm_delete_title"))
                    .setMessage(L("confirm_delete_msg"))
                    .setPositiveButton(L("yes_go"), (d, w) -> MfsSync.deleteGroup(store.joinCode,
                            new MfsSync.Cb<Boolean>() {
                                @Override public void ok(Boolean v) { store.clearAll(); lastMembers = null; showSetup(); }
                                @Override public void fail(String msg) { toast(L("err_net")); }
                            }))
                    .setNegativeButton(L("cancel"), null)
                    .show());
            content.addView(btnDel);
        } else {
            content.addView(spaced(text(L("member_info"), 13, MUTED, false), 12));
            Button btnLeave = button(L("leave_group"), false);
            btnLeave.setTextColor(OVER);
            btnLeave.setOnClickListener(x -> new AlertDialog.Builder(this)
                    .setTitle(L("confirm_leave_title"))
                    .setMessage(L("confirm_leave_msg"))
                    .setPositiveButton(L("yes_go"), (d, w) -> MfsSync.deleteMyMember(store,
                            new MfsSync.Cb<Boolean>() {
                                @Override public void ok(Boolean v) { store.clearAll(); lastMembers = null; showSetup(); }
                                @Override public void fail(String msg) { toast(L("err_net")); }
                            }))
                    .setNegativeButton(L("cancel"), null)
                    .show());
            content.addView(btnLeave);
        }
    }

    // ================= SETTINGS =================

    private void showSettings() {
        inSettings = true;
        renderGen++;
        tabBar.setVisibility(View.GONE);
        content.removeAllViews();
        Button back = button(L("back"), false);
        back.setOnClickListener(x -> {
            if (store.setupDone) showTab(tab); else showSetup();
        });
        content.addView(back);
        LinearLayout c = card();
        c.addView(text(L("settings_title"), 17, VIOLET, true));
        c.addView(spaced(text(L("language"), 14, MUTED, false), 10));
        Button bEn = button("English", "en".equals(store.lang));
        bEn.setOnClickListener(x -> setLang("en"));
        c.addView(bEn);
        Button bUr = button("اردو", !"en".equals(store.lang));
        bUr.setOnClickListener(x -> setLang("ur"));
        c.addView(bUr);
        content.addView(c);
    }

    private void setLang(String lang) {
        store.lang = lang;
        store.save();
        MfsLang.lang = lang;
        showSettings();
    }

    // ================= REPORT =================

    private String reportText(long dataBytes, int[] minsSplit, int sms) {
        StringBuilder sb = new StringBuilder();
        sb.append(L("r_title")).append("\n");
        sb.append(F("r_pkg", store.operator, store.totalGb, store.members)).append("\n");
        sb.append(F("r_share", store.fairShareGb())).append("\n");
        if (dataBytes >= 0) {
            double gb = UsageReader.toGb(dataBytes);
            double pct = store.fairShareGb() > 0 ? gb / store.fairShareGb() : 0;
            sb.append(F("r_used", UsageReader.humanGb(dataBytes), pct * 100));
            if (pct >= 1) sb.append(L("r_over"));
            sb.append("\n");
        } else {
            sb.append(L("r_nodata")).append("\n");
        }
        if (minsSplit != null && minsSplit[0] >= 0) {
            if (store.totalOnnetMins > 0) {
                sb.append(F("r_onnet", minsSplit[0], store.fairShareOnnet())).append("\n");
            }
            if (store.totalOffnetMins > 0) {
                sb.append(F("r_offnet", minsSplit[1], store.fairShareOffnet())).append("\n");
            }
        }
        if (store.totalSms > 0 && sms >= 0) {
            sb.append(F("r_sms", sms, store.fairShareSms())).append("\n");
        }
        int left = store.daysLeft();
        sb.append(left > 0 ? F("r_days", left) : L("r_ended")).append("\n");
        sb.append(L("r_from"));
        return sb.toString();
    }
}

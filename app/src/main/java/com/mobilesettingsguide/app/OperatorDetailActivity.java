package com.mobilesettingsguide.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilesettingsguide.app.data.OperatorData;

/** Ek operator: APN guide + codes. Koi permission nahi — sab intent se. */
public class OperatorDetailActivity extends AppCompatActivity {

    private OperatorData op;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        op = OperatorData.byId(getIntent().getStringExtra("op_id"));
        if (op == null) { finish(); return; }

        boolean urdu = getResources().getConfiguration().getLocales().get(0)
                .getLanguage().equals("ur");
        setTitle(urdu ? op.nameUr : op.nameEn);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad * 2);
        scroll.addView(root);

        // ===== Internet Settings section =====
        root.addView(sectionTitle(getString(R.string.internet_settings)));

        root.addView(stepText("1. " + getString(R.string.step1)));
        root.addView(stepText("2. " + getString(R.string.step2)));
        root.addView(stepText("3. " + getString(R.string.step3)));
        root.addView(stepText("4. " + getString(R.string.step4)));
        root.addView(stepText("5. " + getString(R.string.step5)));

        for (OperatorData.ApnField f : op.apn) {
            root.addView(apnRow(f));
        }

        // APN screen kholne ka button
        Button btnApn = bigButton(getString(R.string.open_apn_settings),
                Color.parseColor(op.brandColor));
        btnApn.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APN_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, R.string.no_apn_screen, Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(withMargin(btnApn, 12, 4));

        // SMS se settings (jahan verified ho)
        if (op.smsNumber != null) {
            Button btnSms = bigButton(
                    getString(R.string.send_sms_settings, op.smsText, op.smsNumber),
                    Color.parseColor("#0F172A"));
            btnSms.setOnClickListener(v -> {
                try {
                    Intent i = new Intent(Intent.ACTION_SENDTO,
                            Uri.parse("smsto:" + op.smsNumber));
                    i.putExtra("sms_body", op.smsText);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(this, R.string.no_sms_app, Toast.LENGTH_SHORT).show();
                }
            });
            root.addView(withMargin(btnSms, 4, 2));
            TextView note = new TextView(this);
            note.setText(op.smsNote);
            note.setTextSize(12);
            note.setTextColor(Color.parseColor("#64748B"));
            root.addView(withMargin(note, 0, 8));
        }

        // ===== Codes section =====
        root.addView(sectionTitle(getString(R.string.codes_title)));
        for (OperatorData.Code c : op.codes) {
            root.addView(codeRow(c));
        }

        TextView foot = new TextView(this);
        foot.setText(getString(R.string.last_updated));
        foot.setTextSize(11);
        foot.setTextColor(Color.parseColor("#94A3B8"));
        foot.setGravity(Gravity.CENTER);
        root.addView(withMargin(foot, 20, 0));

        setContentView(scroll);
    }

    private TextView sectionTitle(String t) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(19);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.parseColor(op.brandColor));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        lp.bottomMargin = dp(8);
        tv.setLayoutParams(lp);
        return tv;
    }

    private TextView stepText(String t) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#334155"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(4);
        tv.setLayoutParams(lp);
        return tv;
    }

    /** APN field: label + value + Copy button */
    private LinearLayout apnRow(OperatorData.ApnField f) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F1F5F9"));
        bg.setCornerRadius(dp(10));
        row.setBackground(bg);
        int p = dp(10);
        row.setPadding(p, p, p, p);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        texts.setLayoutParams(tlp);

        TextView label = new TextView(this);
        label.setText(f.label);
        label.setTextSize(12);
        label.setTextColor(Color.parseColor("#64748B"));
        texts.addView(label);

        TextView value = new TextView(this);
        value.setText(f.value);
        value.setTextSize(15);
        value.setTypeface(null, Typeface.BOLD);
        value.setTextColor(Color.parseColor("#0F172A"));
        value.setTextIsSelectable(true);
        texts.addView(value);
        row.addView(texts);

        Button copy = new Button(this);
        copy.setText(getString(R.string.copy));
        copy.setAllCaps(false);
        copy.setTextSize(13);
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("apn", f.value));
                Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
            }
        });
        row.addView(copy);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);
        return row;
    }

    /** Code row: label + code + Dial button */
    private LinearLayout codeRow(OperatorData.Code c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), Color.parseColor("#E2E8F0"));
        row.setBackground(bg);
        int p = dp(12);
        row.setPadding(p, p, p, p);

        int labelRes;
        switch (c.labelKey) {
            case "balance": labelRes = R.string.balance; break;
            case "mbs": labelRes = R.string.remaining_mbs; break;
            default: labelRes = R.string.helpline;
        }
        TextView label = new TextView(this);
        label.setText(getString(labelRes));
        label.setTextSize(15);
        label.setTextColor(Color.parseColor("#0F172A"));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(llp);
        row.addView(label);

        TextView code = new TextView(this);
        code.setText(c.code);
        code.setTextSize(16);
        code.setTypeface(null, Typeface.BOLD);
        code.setTextColor(Color.parseColor(op.brandColor));
        row.addView(code);

        Button dial = new Button(this);
        dial.setText(getString(R.string.dial));
        dial.setAllCaps(false);
        dial.setTextSize(13);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.leftMargin = dp(8);
        dlp.rightMargin = dp(8);
        dial.setLayoutParams(dlp);
        dial.setOnClickListener(v -> {
            try {
                // ACTION_DIAL: koi permission nahi chahiye, dialer khulta hai
                Intent i = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + Uri.encode(c.code)));
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, R.string.no_dialer, Toast.LENGTH_SHORT).show();
            }
        });
        row.addView(dial);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);
        return row;
    }

    private Button bigButton(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(12));
        b.setBackground(bg);
        b.setPadding(dp(8), dp(14), dp(8), dp(14));
        return b;
    }

    private android.view.View withMargin(android.view.View v, int top, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(top);
        lp.bottomMargin = dp(bottom);
        v.setLayoutParams(lp);
        return v;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

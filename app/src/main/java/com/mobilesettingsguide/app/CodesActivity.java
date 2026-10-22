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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilesettingsguide.app.data.CodeData;

/** Khufia codes: internet/network ki hidden screens. Koi permission nahi. */
public class CodesActivity extends AppCompatActivity {

    private boolean urdu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urdu = getResources().getConfiguration().getLocales().get(0)
                .getLanguage().equals("ur");
        setTitle(getString(R.string.codes_screen_title));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad * 2);
        scroll.addView(root);

        // Safety note
        TextView safety = new TextView(this);
        safety.setText(getString(R.string.codes_safety));
        safety.setTextSize(13);
        safety.setTextColor(Color.parseColor("#92400E"));
        GradientDrawable sbg = new GradientDrawable();
        sbg.setColor(Color.parseColor("#FEF3C7"));
        sbg.setCornerRadius(dp(10));
        safety.setBackground(sbg);
        safety.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.bottomMargin = dp(16);
        safety.setLayoutParams(slp);
        root.addView(safety);

        for (CodeData.BrandGroup g : CodeData.all()) {
            TextView title = new TextView(this);
            title.setText(urdu ? g.nameUr : g.nameEn);
            title.setTextSize(19);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(Color.parseColor(g.color));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.topMargin = dp(10);
            tlp.bottomMargin = dp(8);
            title.setLayoutParams(tlp);
            root.addView(title);

            for (CodeData.SecretCode c : g.codes) {
                root.addView(codeCard(c, g.color));
            }
        }

        TextView foot = new TextView(this);
        foot.setText(getString(R.string.last_updated));
        foot.setTextSize(11);
        foot.setTextColor(Color.parseColor("#94A3B8"));
        foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.topMargin = dp(20);
        foot.setLayoutParams(flp);
        root.addView(foot);

        setContentView(scroll);
    }

    /** Ek code ka card: title + bara code + description + Copy/Dial */
    private LinearLayout codeCard(CodeData.SecretCode c, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F1F5F9"));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);
        int p = dp(12);
        card.setPadding(p, p, p, p);

        TextView title = new TextView(this);
        title.setText(urdu ? c.titleUr : c.titleEn);
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#0F172A"));
        card.addView(title);

        TextView code = new TextView(this);
        code.setText(c.code);
        code.setTextSize(22);
        code.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        code.setTextColor(Color.parseColor(color));
        code.setTextIsSelectable(true);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = dp(6);
        code.setLayoutParams(clp);
        card.addView(code);

        TextView desc = new TextView(this);
        desc.setText(urdu ? c.descUr : c.descEn);
        desc.setTextSize(13);
        desc.setTextColor(Color.parseColor("#334155"));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.topMargin = dp(6);
        desc.setLayoutParams(dlp);
        card.addView(desc);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.topMargin = dp(10);
        btnRow.setLayoutParams(blp);

        Button copy = new Button(this);
        copy.setText(getString(R.string.copy));
        copy.setAllCaps(false);
        copy.setTextSize(13);
        copy.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("code", c.code));
                Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(copy);

        Button dial = new Button(this);
        dial.setText(getString(R.string.dial));
        dial.setAllCaps(false);
        dial.setTextSize(13);
        LinearLayout.LayoutParams dlpBtn = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dlpBtn.leftMargin = dp(8);
        dial.setLayoutParams(dlpBtn);
        dial.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + Uri.encode(c.code)));
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, R.string.no_dialer, Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(dial);
        card.addView(btnRow);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        return card;
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

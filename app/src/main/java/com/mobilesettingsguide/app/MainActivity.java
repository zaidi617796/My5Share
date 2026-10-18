package com.mobilesettingsguide.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mobilesettingsguide.app.data.OperatorData;

import java.util.List;

/** Home: 4 operators ke cards. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextSize(24);
        title.setTextColor(Color.parseColor("#0F172A"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.tagline));
        sub.setTextSize(14);
        sub.setTextColor(Color.parseColor("#64748B"));
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = dp(4);
        slp.bottomMargin = dp(16);
        root.addView(sub, slp);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(true);
        root.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        List<OperatorData> ops = OperatorData.all();
        for (OperatorData op : ops) {
            Button b = new Button(this);
            // Urdu locale me Urdu naam, warna English
            boolean urdu = getResources().getConfiguration().getLocales().get(0)
                    .getLanguage().equals("ur");
            b.setText(urdu ? op.nameUr : op.nameEn);
            b.setTextSize(20);
            b.setTextColor(Color.WHITE);
            b.setAllCaps(false);
            int brand = Color.parseColor(op.brandColor);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(brand);
            bg.setCornerRadius(dp(16));
            b.setBackground(bg);
            int padB = dp(8);
            b.setPadding(padB, dp(28), padB, dp(28));
            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
            glp.width = 0;
            glp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            glp.setMargins(dp(6), dp(6), dp(6), dp(6));
            b.setOnClickListener(v -> {
                Intent i = new Intent(this, OperatorDetailActivity.class);
                i.putExtra("op_id", op.id);
                startActivity(i);
            });
            grid.addView(b, glp);
        }

        TextView foot = new TextView(this);
        foot.setText(getString(R.string.disclaimer));
        foot.setTextSize(11);
        foot.setTextColor(Color.parseColor("#94A3B8"));
        foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.topMargin = dp(24);
        root.addView(foot, flp);

        setContentView(scroll);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}

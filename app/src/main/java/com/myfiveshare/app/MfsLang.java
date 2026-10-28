package com.myfiveshare.app;

import java.util.HashMap;
import java.util.Map;

/**
 * MyFiveShare ki dono zubanein: English + Urdu (script).
 * Settings (gear) se badli jati hai; MfsStore me "lang" save rehti hai.
 */
public class MfsLang {

    public static String lang = "ur"; // "en" ya "ur"

    private static final Map<String, String[]> T = new HashMap<>();

    private static void p(String key, String en, String ur) {
        T.put(key, new String[]{en, ur});
    }

    public static String s(String key) {
        String[] v = T.get(key);
        if (v == null) return key;
        return "en".equals(lang) ? v[0] : v[1];
    }

    static {
        p("tagline", "Fair sharing, clear account — who used how much, visible to everyone.",
                "برابر کی تقسیم، صاف حساب — کس نے کتنا استعمال کیا، سب کو نظر آئے۔");
        p("access_title", "⚠ Usage Access needed", "⚠ استعمال کی اجازت درکار ہے");
        p("access_body", "The app can read your real data usage only after you allow Usage Access once in Settings. Without it, no account can be made.",
                "ایپ آپ کا اصل ڈیٹا استعمال تبھی پڑھ سکتی ہے جب آپ ایک دفعہ سیٹنگز میں استعمال کی اجازت دیں۔ اس کے بغیر حساب نہیں بن سکتا۔");
        p("access_btn", "Give Usage Access", "اجازت دیں");
        p("access_short", "Without Usage Access, usage cannot be read — tap the button above.",
                "استعمال کی اجازت کے بغیر استعمال نہیں پڑھا جا سکتا — اوپر والا بٹن دبائیں۔");

        p("create_title", "Create Package — Admin", "پیکیج بنائیں — ایڈمن");
        p("join_title", "Join Group — Member", "گروپ میں شامل ہوں — ممبر");
        p("your_name", "Your name", "آپ کا نام");
        p("total_gb_hint", "Total data (GB) — e.g. 100", "کل ڈیٹا (GB) — مثلاً 100");
        p("members_hint", "How many people are sharing — e.g. 5", "کتنے لوگ شیئر کر رہے ہیں — مثلاً 5");
        p("days_hint", "Package validity (days) — e.g. 30", "پیکیج کتنے دن کا ہے — مثلاً 30");
        p("start_date", "Start date: ", "شروع کی تاریخ: ");
        p("pick_date", "Choose start date", "شروع کی تاریخ چنیں");
        p("create_btn", "Create Group", "گروپ بنائیں");
        p("creating", "Creating group…", "گروپ بن رہا ہے…");
        p("join_code_hint", "Join code from admin — e.g. K7PX2A", "ایڈمن والا جوائن کوڈ — مثلاً K7PX2A");
        p("join_btn", "Join", "شامل ہوں");
        p("joining", "Joining…", "شامل ہو رہے ہیں…");
        p("join_auto_note", "Just the code is enough — all package details will arrive automatically.",
                "صرف کوڈ کافی ہے — پیکیج کی ساری تفصیل خود بخود آ جائے گی۔");
        p("honesty", "Honesty: this app cannot stop anyone's data — it only shows the account. Every member must install the app on their own phone for their usage to be counted.",
                "ایمانداری: یہ ایپ کسی کا ڈیٹا روک نہیں سکتی — صرف حساب دکھاتی ہے۔ ہر ممبر کو اپنے فون پر ایپ لگانی ہوگی، تبھی اس کا استعمال گنا جائے گا۔");

        p("err_name", "Write your name first", "پہلے اپنا نام لکھیں");
        p("err_gb", "Write total data (GB) correctly", "کل ڈیٹا (GB) درست لکھیں");
        p("err_members", "Members must be at least 2", "ممبر کم از کم 2 ہوں");
        p("err_days", "Write validity days correctly", "پیکیج کے دن درست لکھیں");
        p("err_code", "Write the join code correctly", "جوائن کوڈ درست لکھیں");
        p("err_notfound", "Group not found — check the code", "گروپ نہیں ملا — کوڈ چیک کریں");
        p("err_net", "Internet problem — try again", "انٹرنیٹ کا مسئلہ ہے — دوبارہ کوشش کریں");
        p("created_toast", "Group created! Code: ", "گروپ بن گیا! کوڈ: ");
        p("joined_toast", "Joined!", "شامل ہو گئے!");

        p("tab_home", "Home", "ہوم");
        p("tab_members", "Members", "ممبرز");
        p("tab_admin", "Admin", "ایڈمن");
        p("tab_member", "Member", "ممبر");
        p("role_admin", "Admin", "ایڈمن");
        p("role_member", "Member", "ممبر");
        p("pkg_suffix", " Sharing Package", " شیئرنگ پیکیج");
        p("join_code_label", "Join code: ", "جوائن کوڈ: ");
        p("share_code", "Share join code", "جوائن کوڈ شیئر کریں");
        p("pkg_line", "Total: %.0f GB • Members: %d • One share: %.1f GB",
                "کل: %.0f GB • ممبر: %d • ایک کا حصہ: %.1f GB");
        p("cycle_left", "Cycle: from %s • %d days left", "سائیکل: %s سے • %d دن باقی");
        p("cycle_ended", "Cycle ended — start a new one from the Admin tab",
                "سائیکل ختم — ایڈمن ٹیب سے نیا سائیکل شروع کریں");
        p("group_used", "Group total used: %s of %.0f GB", "گروپ کا کل استعمال: %s از %.0f GB");
        p("syncing", "Syncing…", "تازہ ہو رہا ہے…");
        p("sync_fail", "Could not sync — showing last saved data.", "تازہ کاری نہیں ہو سکی — آخری محفوظ ڈیٹا دکھایا جا رہا ہے۔");

        p("my_usage", "My usage", "میرا استعمال");
        p("you_suffix", " (you)", " (آپ)");
        p("my_share_line", "My share: %.1f GB • Used: %.0f%%", "میرا حصہ: %.1f GB • استعمال: %.0f%%");
        p("over_warn", "⚠ You have used MORE than your share!", "⚠ آپ اپنے حصے سے زیادہ استعمال کر چکے ہیں!");
        p("almost_warn", "Your share is almost finished — use carefully.",
                "حصہ ختم ہونے والا ہے — احتیاط سے استعمال کریں۔");
        p("zero_note", "Still 0. If you have used data, Refresh after a while — some phones update the count late.",
                "ابھی 0 آیا ہے۔ اگر ڈیٹا استعمال کیا ہے تو کچھ دیر بعد تازہ کریں — کچھ فونز پر گنتی دیر سے تازہ ہوتی ہے۔");
        p("usage_fail", "Could not read usage right now — try Refresh in a bit.",
                "ابھی استعمال نہیں پڑھا جا سکا — تھوڑی دیر بعد تازہ کریں۔");
        p("refresh", "Refresh", "تازہ کریں");
        p("report_btn", "Share report (WhatsApp)", "رپورٹ شیئر کریں (WhatsApp)");

        p("members_title", "Members (%d)", "ممبرز (%d)");
        p("not_joined", "Not joined yet", "ابھی شامل نہیں ہوا");
        p("member_data_note", "Their data will come from their phone", "ان کا ڈیٹا ان کے فون سے آئے گا");
        p("last_sync", "synced %s ago", "%s پہلے تازہ ہوا");
        p("just_now", "just now", "ابھی");
        p("mins", " min", " منٹ");
        p("hours", " h", " گھنٹے");
        p("days_u", " d", " دن");

        p("member_info", "You are a member of this group. The package is managed by the admin.",
                "آپ اس گروپ کے ممبر ہیں۔ پیکیج کا انتظام ایڈمن کے پاس ہے۔");
        p("new_cycle", "Start new cycle", "نیا سائیکل شروع کریں");
        p("new_cycle_done", "New cycle started", "نیا سائیکل شروع ہو گیا");
        p("delete_group", "End group", "گروپ ختم کریں");
        p("leave_group", "Leave group", "گروپ چھوڑیں");
        p("confirm_cycle_title", "New cycle?", "نیا سائیکل؟");
        p("confirm_cycle_msg", "The cycle will start from today and everyone's count restarts. Continue?",
                "سائیکل آج سے شروع ہو جائے گا اور سب کی گنتی نئے سرے سے شروع ہو گی۔ آگے بڑھیں؟");
        p("confirm_delete_title", "End the group?", "گروپ ختم کریں؟");
        p("confirm_delete_msg", "The group and its whole account will be deleted for everyone. Continue?",
                "گروپ اور اس کا سارا حساب سب کے لیے ختم ہو جائے گا۔ آگے بڑھیں؟");
        p("confirm_leave_title", "Leave the group?", "گروپ چھوڑیں؟");
        p("confirm_leave_msg", "Your record will be removed from this group. Continue?",
                "آپ کا ریکارڈ اس گروپ سے نکل جائے گا۔ آگے بڑھیں؟");
        p("yes_go", "Yes, continue", "ہاں، آگے بڑھیں");
        p("cancel", "Cancel", "منسوخ");

        p("settings_title", "Settings", "سیٹنگز");
        p("language", "Language", "زبان");
        p("back", "← Back", "← واپس");

        p("data_u", "Data", "ڈیٹا");
        p("mins_u", "Minutes", "منٹ");
        p("sms_u", "SMS", "پیغامات");
        p("total_mins_hint", "Total minutes — e.g. 1000 (empty = not counted)", "کل منٹ — مثلاً 1000 (خالی = گنتی نہیں)");
        p("onnet_u", "On-Net Minutes", "آن نیٹ منٹ");
        p("offnet_u", "Off-Net Minutes", "آف نیٹ منٹ");
        p("total_onnet_hint", "Total on-net minutes (own network) — e.g. 80000 (empty = not counted)",
                "کل آن نیٹ منٹ (اپنے نیٹ ورک پر) — مثلاً 80000 (خالی = گنتی نہیں)");
        p("total_offnet_hint", "Total off-net minutes (other networks) — e.g. 8000 (empty = not counted)",
                "کل آف نیٹ منٹ (دوسرے نیٹ ورکس پر) — مثلاً 8000 (خالی = گنتی نہیں)");
        p("r_onnet", "On-net minutes: I used %d of %.0f share", "آن نیٹ منٹ: میں نے %2$.0f حصے میں سے %1$d استعمال کیے");
        p("r_offnet", "Off-net minutes: I used %d of %.0f share", "آف نیٹ منٹ: میں نے %2$.0f حصے میں سے %1$d استعمال کیے");
        p("left_of", "left of", "باقی از");
        p("total_sms_hint", "Total SMS — e.g. 1000 (empty = not counted)", "کل پیغامات — مثلاً 1000 (خالی = گنتی نہیں)");
        p("used_line", "Used: %s of %.1f", "استعمال: %s از %.1f");
        p("perm_call", "Allow call record access to count your minutes", "منٹ گننے کے لیے کال ریکارڈ کی اجازت دیں");
        p("perm_sms", "Allow SMS access to count your messages", "پیغامات گننے کے لیے SMS کی اجازت دیں");
        p("grant", "Allow", "اجازت دیں");
        p("r_mins", "Minutes: I used %d of %.0f share", "منٹ: میں نے %2$.0f حصے میں سے %1$d استعمال کیے");
        p("r_sms", "SMS: I used %d of %.0f share", "پیغامات: میں نے %2$.0f حصے میں سے %1$d بھیجے");
        p("share_line2", "Share: %.1f • Used: %.0f%%", "حصہ: %.1f • استعمال: %.0f%%");

        p("r_title", "MyFiveShare Report", "MyFiveShare رپورٹ");
        p("r_pkg", "%s package: %.0f GB • Members: %d", "%s پیکیج: %.0f GB • ممبر: %d");
        p("r_share", "My share: %.1f GB", "میرا حصہ: %.1f GB");
        p("r_used", "I used: %s (%.0f%%)", "میں نے استعمال کیا: %s (%.0f%%)");
        p("r_over", " — MORE than my share", " — حصے سے زیادہ");
        p("r_nodata", "My usage could not be read yet.", "میرا استعمال ابھی نہیں پڑھا جا سکا۔");
        p("r_days", "Days left in cycle: %d", "سائیکل میں دن باقی: %d");
        p("r_ended", "Cycle ended.", "سائیکل ختم۔");
        p("r_from", "— from the MyFiveShare app", "— MyFiveShare ایپ سے");
        p("share_code_msg", "MyFiveShare group code: %s\nInstall the app and enter this code — the whole group's sharing account in one place.",
                "MyFiveShare گروپ کوڈ: %s\nایپ لگا کر یہ کوڈ ڈالیں — پورے گروپ کا شیئرنگ حساب ایک جگہ۔");
    }
}

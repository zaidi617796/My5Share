package com.mobilesettingsguide.app.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Khufia (secret) codes jo internet/network ki khufia screens kholte hain.
 * Codes Oct 2026 me 2+ sources se verify kiye gaye.
 * SIRF maloomat/settings wale codes hain — reset wale codes kabhi nahi.
 */
public class CodeData {

    public static class SecretCode {
        public final String code;      // dialable, e.g. "*#*#4636#*#*"
        public final String titleEn;
        public final String titleUr;
        public final String descEn;
        public final String descUr;
        /** true = is menu me setting badalna mumkin hai, ehtiyaat wali note */
        public final boolean caution;
        public SecretCode(String code, String titleEn, String titleUr,
                          String descEn, String descUr, boolean caution) {
            this.code = code;
            this.titleEn = titleEn;
            this.titleUr = titleUr;
            this.descEn = descEn;
            this.descUr = descUr;
            this.caution = caution;
        }
    }

    public static class BrandGroup {
        public final String nameEn;
        public final String nameUr;
        public final String color;
        public final List<SecretCode> codes = new ArrayList<>();
        public BrandGroup(String nameEn, String nameUr, String color) {
            this.nameEn = nameEn;
            this.nameUr = nameUr;
            this.color = color;
        }
    }

    public static List<BrandGroup> all() {
        List<BrandGroup> list = new ArrayList<>();

        // ---- SAB ANDROID ----
        BrandGroup all = new BrandGroup("All Android Phones", "تمام اینڈرائیڈ فونز", "#0F172A");
        all.codes.add(new SecretCode("*#*#4636#*#*",
                "Testing Menu — Phone Info",
                "ٹیسٹنگ مینو — فون کی معلومات",
                "Sab se kaam ka code! Signal strength (dBm), current network, aur 'Set preferred network type' — yahan se LTE Only / 4G Only bhi set hota hai jab signal bar bar girta ho. Battery aur Wi-Fi ki info bhi yahin milti hai.",
                "سب سے کام کا کوڈ! سگنل کی طاقت (dBm)، موجودہ نیٹ ورک، اور 'Set preferred network type' — یہاں سے LTE Only / 4G Only بھی سیٹ ہوتا ہے جب سگنل بار بار گرتا ہو۔ بیٹری اور وائی فائی کی معلومات بھی یہیں ملتی ہے۔",
                true));
        all.codes.add(new SecretCode("*#06#",
                "IMEI Number",
                "IMEI نمبر",
                "Phone ka IMEI screen par aa jata hai. PTA DIRBS par phone check karne ke liye bhi yehi number chahiye hota hai.",
                "فون کا IMEI اسکرین پر آ جاتا ہے۔ PTA DIRBS پر فون چیک کرنے کے لیے بھی یہی نمبر چاہیے ہوتا ہے۔",
                false));
        list.add(all);

        // ---- SAMSUNG ----
        BrandGroup samsung = new BrandGroup("Samsung", "سام سنگ", "#1428A0");
        samsung.codes.add(new SecretCode("*#0011#",
                "Service Mode — Network Info",
                "سروس موڈ — نیٹ ورک معلومات",
                "Samsung ki khufia network screen: kaunsa band chal raha hai, signal ki quality, SIM ki halat — sab live nazar ata hai. Ye sirf dekhne ke liye hai, yahan se kuch change nahi hota.",
                "سام سنگ کی خفیہ نیٹ ورک اسکرین: کونسا بینڈ چل رہا ہے، سگنل کی کوالٹی، سم کی حالت — سب لائیو نظر آتا ہے۔ یہ صرف دیکھنے کے لیے ہے، یہاں سے کچھ تبدیل نہیں ہوتا۔",
                false));
        samsung.codes.add(new SecretCode("*#2263#",
                "Band Selection",
                "بینڈ سلیکشن",
                "Kuch Samsung models par is se network band khud chun sakte ho (maslan sirf LTE band). Har model par nahi chalta — agar menu na khule to phone isay support nahi karta.",
                "کچھ سام سنگ ماڈلز پر اس سے نیٹ ورک بینڈ خود چن سکتے ہو (مثلاً صرف LTE بینڈ)۔ ہر ماڈل پر نہیں چلتا — اگر مینو نہ کھلے تو فون اسے سپورٹ نہیں کرتا۔",
                true));
        list.add(samsung);

        // ---- XIAOMI ----
        BrandGroup xiaomi = new BrandGroup("Xiaomi / Redmi / POCO", "شیاومی / ریڈمی / پوکو", "#FF6900");
        xiaomi.codes.add(new SecretCode("*#*#6484#*#*",
                "Hardware Test (CIT)",
                "ہارڈویئر ٹیسٹ (CIT)",
                "Xiaomi ka khufia test menu: SIM, Wi-Fi, sensors aur hardware ke test. Internet/network ki gehri info ke liye oper wala *#*#4636#*#* code bhi Xiaomi par chalta hai.",
                "شیاومی کا خفیہ ٹیسٹ مینو: سم، وائی فائی، سینسرز اور ہارڈویئر کے ٹیسٹ۔ انٹرنیٹ/نیٹ ورک کی گہری معلومات کے لیے اوپر والا *#*#4636#*#* کوڈ بھی شیاومی پر چلتا ہے۔",
                false));
        list.add(xiaomi);

        // ---- MEDIATEK (INFINIX / TECNO / ITEL) ----
        BrandGroup mtk = new BrandGroup("Infinix / Tecno / Itel (MediaTek)",
                "انفینکس / ٹیکنو / آئیٹیل (میڈیا ٹیک)", "#7C3AED");
        mtk.codes.add(new SecretCode("*#*#3646633#*#*",
                "Engineer Mode",
                "انجینئر موڈ",
                "MediaTek processor wale phones ka khufia menu: band mode, network ki gehri settings aur hardware tests. EHTIYAAT: yahan ghalat setting tabdeel karne se network band ho sakta hai — sirf dekho, samajh na aye to kuch change na karo.",
                "میڈیا ٹیک پروسیسر والے فونز کا خفیہ مینو: بینڈ موڈ، نیٹ ورک کی گہری سیٹنگز اور ہارڈویئر ٹیسٹ۔ احتیاط: یہاں غلط سیٹنگ تبدیل کرنے سے نیٹ ورک بند ہو سکتا ہے — صرف دیکھو، سمجھ نہ آئے تو کچھ تبدیل نہ کرو۔",
                true));
        list.add(mtk);

        return list;
    }
}

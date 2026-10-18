package com.mobilesettingsguide.app.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Charon operators ka verified data (Oct 2026).
 * Koi bhi code add karne se pehle dobara verify karo — operators badal dete hain.
 */
public class OperatorData {

    public static class ApnField {
        public final String label;
        public final String value;
        public ApnField(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class Code {
        public final String labelKey; // "balance" | "mbs" | "helpline"
        public final String code;     // dialable, e.g. "*111#"
        public Code(String labelKey, String code) {
            this.labelKey = labelKey;
            this.code = code;
        }
    }

    public final String id;
    public final String nameEn;
    public final String nameUr;
    public final String brandColor;
    public final List<ApnField> apn = new ArrayList<>();
    public final List<Code> codes = new ArrayList<>();
    /** SMS se settings (null agar na ho): text + number + note */
    public String smsText, smsNumber, smsNote;

    private OperatorData(String id, String nameEn, String nameUr, String brandColor) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameUr = nameUr;
        this.brandColor = brandColor;
    }

    public static List<OperatorData> all() {
        List<OperatorData> list = new ArrayList<>();

        // ---- JAZZ ----
        OperatorData jazz = new OperatorData("jazz", "Jazz", "جاز", "#D32F2F");
        jazz.apn.add(new ApnField("Name", "Jazz Internet"));
        jazz.apn.add(new ApnField("APN (Prepaid)", "jazzconnect.mobilinkworld.com"));
        jazz.apn.add(new ApnField("APN (Postpaid)", "connect.mobilinkworld.com"));
        jazz.apn.add(new ApnField("MCC / MNC", "410 / 01"));
        jazz.apn.add(new ApnField("APN protocol", "IPv4/IPv6"));
        jazz.codes.add(new Code("balance", "*111#"));
        jazz.codes.add(new Code("mbs", "*114*4#"));
        jazz.codes.add(new Code("helpline", "111"));
        list.add(jazz);

        // ---- ZONG ----
        OperatorData zong = new OperatorData("zong", "Zong", "زونگ", "#2E7D32");
        zong.apn.add(new ApnField("Name", "Zong Internet"));
        zong.apn.add(new ApnField("APN", "zonginternet"));
        zong.apn.add(new ApnField("MCC / MNC", "410 / 07"));
        zong.apn.add(new ApnField("APN type", "default"));
        zong.smsText = "all";
        zong.smsNumber = "2161";
        zong.smsNote = "Settings SMS ayega — PIN 1234 mange to dal do, phir Install dabao";
        zong.codes.add(new Code("balance", "*222#"));
        zong.codes.add(new Code("mbs", "*102#"));
        zong.codes.add(new Code("helpline", "310"));
        list.add(zong);

        // ---- TELENOR ----
        OperatorData telenor = new OperatorData("telenor", "Telenor", "ٹیلی نار", "#0288D1");
        telenor.apn.add(new ApnField("Name", "Telenor Internet"));
        telenor.apn.add(new ApnField("APN", "internet"));
        telenor.apn.add(new ApnField("Username", "Telenor"));
        telenor.apn.add(new ApnField("Password", "Telenor"));
        telenor.apn.add(new ApnField("MCC / MNC", "410 / 06"));
        telenor.apn.add(new ApnField("APN type", "default,supl"));
        telenor.smsText = "Internet";
        telenor.smsNumber = "131";
        telenor.smsNote = "Settings SMS ayega — default PIN 0000, phir Save/Install dabao";
        telenor.codes.add(new Code("balance", "*444#"));
        telenor.codes.add(new Code("mbs", "*999#"));
        telenor.codes.add(new Code("helpline", "345"));
        list.add(telenor);

        // ---- UFONE ----
        OperatorData ufone = new OperatorData("ufone", "Ufone", "یوفون", "#EF6C00");
        ufone.apn.add(new ApnField("Name", "Ufone Internet"));
        ufone.apn.add(new ApnField("APN (Prepaid)", "ufone.pinternet"));
        ufone.apn.add(new ApnField("APN (Postpaid)", "ufone.internet"));
        ufone.apn.add(new ApnField("MCC / MNC", "410 / 03"));
        ufone.codes.add(new Code("balance", "*124#"));
        ufone.codes.add(new Code("mbs", "*706#"));
        ufone.codes.add(new Code("helpline", "333"));
        list.add(ufone);

        return list;
    }

    public static OperatorData byId(String id) {
        for (OperatorData o : all()) {
            if (o.id.equals(id)) return o;
        }
        return null;
    }
}

package org.talor.wurmunlimited.mods.structures;

public class ScrollTemplate {

    public String model;
    public String name;
    public String description;
    public int templateID;
    public int skillID;
    public float skillModifier;

    public ScrollTemplate (String n, String d, String m, int sid, float sm) {
        model = m;
        name = n;
        description = d;
        skillID = sid;
        skillModifier = sm;
    }

}
package org.talor.wurmunlimited.mods.structures;

public class EnchantScrollTemplate {

    public String model;
    public String name;
    public String description;
    public int templateID;
    public int enchantID;

    public EnchantScrollTemplate (String n, String d, String m, int eid) {
        model = m;
        name = n;
        description = d;
        enchantID = eid;
    }
}

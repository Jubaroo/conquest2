package org.talor.wurmunlimited.mods.structures;

public class SpawnerTemplate {

    public String sound;
    public String model;
    public String name;
    public int mobType;
    public int maxNum;
    public long timeout;
    public int templateID;

    public SpawnerTemplate(String n, String s, String m, int mt, int mn, long to) {
        sound = s;
        model = m;
        name = n;
        mobType = mt;
        maxNum = mn;
        timeout = to;
    }


}

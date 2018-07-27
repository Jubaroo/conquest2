package org.talor.wurmunlimited.mods.structures;

public class StructureTemplate {

    public String sound;
    public String model;
    public String name;
    public int templateProduce;
    public int templateConsume;
    public int templateSecondaryConsume;
    public int maxNum;
    public int maxitems;
    public long timeout;
    public int templateID;

    public StructureTemplate( String n, String s, String m, int tp, int tc, int tsc, int mn, int mi, long to) {
        sound = s;
        model = m;
        name = n;
        templateProduce = tp;
        templateConsume = tc;
        templateSecondaryConsume = tsc;
        maxNum = mn;
        maxitems = mi;
        timeout = to;
    }


}

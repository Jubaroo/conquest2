package org.talor.wurmunlimited.mods.structures;

public class Location {
    public float x;
    public float y;
    public byte type;

    public Location(float nx, float ny, byte ntype) {
        x = nx;
        y = ny;
        type = ntype;
    }
}
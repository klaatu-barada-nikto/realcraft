package com.realcraft.buildmodel.model;

public class VoxelBlock {
    private final String id;
    private final int x;
    private final int y;
    private final int z;

    public VoxelBlock(String id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getId() {
        return id;
    }
}
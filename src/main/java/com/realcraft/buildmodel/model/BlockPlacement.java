package com.realcraft.buildmodel.model;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockPlacement {
    private final BlockPos pos;
    private final BlockState state;

    public BlockPlacement(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }
}
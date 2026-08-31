package com.realcraft.buildmodel.queue;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class BuildTickHandler {
    public static final int MAX_BLOCKS_PER_TICK = 1000;

    private BuildTickHandler() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server ->
                BuildTaskQueue.getInstance().tick(MAX_BLOCKS_PER_TICK));
    }
}
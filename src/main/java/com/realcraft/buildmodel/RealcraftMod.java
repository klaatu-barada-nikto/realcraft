package com.realcraft.buildmodel;

import com.realcraft.buildmodel.command.BuildModelCommand;
import com.realcraft.buildmodel.queue.BuildTickHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealcraftMod implements ModInitializer {
    public static final String MOD_ID = "realcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BuildModelCommand.register();
        BuildTickHandler.register();
        LOGGER.info("[Realcraft] Voxel Builder plugin initialized.");
    }
}
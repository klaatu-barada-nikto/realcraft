package com.realcraft.buildmodel.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.realcraft.buildmodel.service.ModelBuildService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BuildModelCommand {

    private BuildModelCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("buildmodel")
                        .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    var source = context.getSource();
                                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                                        source.sendError(Text.literal("该指令只能由玩家执行。"));
                                        return 0;
                                    }
                                    String url = StringArgumentType.getString(context, "url");
                                    ModelBuildService.getInstance().startBuild(player, url);
                                    return 1;
                                }))));
    }
}
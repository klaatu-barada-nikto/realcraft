package com.realcraft.buildmodel.service;

import com.realcraft.buildmodel.model.VoxelBlock;
import com.realcraft.buildmodel.queue.BuildTaskQueue;
import com.realcraft.buildmodel.queue.ModelBuildJob;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import java.util.List;

public class ModelBuildService {
    private static final ModelBuildService INSTANCE = new ModelBuildService();

    private final ModelDownloader downloader = new ModelDownloader();

    private ModelBuildService() {
    }

    public static ModelBuildService getInstance() {
        return INSTANCE;
    }

    public void startBuild(ServerPlayerEntity player, String url) {
        player.sendMessage(Text.literal("正在下载模型数据: " + url).withColor(0xFFFFFF55));
        downloader.download(url).whenComplete((blocks, error) -> {
            if (error != null) {
                sendToServerThread(player, () -> sendError(player, "模型数据获取失败: " + rootMessage(error)));
                return;
            }
            sendToServerThread(player, () -> {
                ModelBuildJob job = ModelBuildJob.fromVoxelBlocks(player, blocks);
                BuildTaskQueue.getInstance().enqueue(job);
                player.sendMessage(Text.literal("已获取 " + blocks.size() + " 个方块，开始分批构建...").withColor(0xFFFFFF55));
            });
        });
    }

    private void sendToServerThread(ServerPlayerEntity player, Runnable action) {
        if (player.isDisconnected()) {
            return;
        }
        player.getEntityWorld().getServer().execute(action);
    }

    private void sendError(ServerPlayerEntity player, String message) {
        if (player.isDisconnected()) {
            return;
        }
        player.sendMessage(Text.literal("[buildmodel] " + message).withColor(0xFFFF5555));
    }

    private static String rootMessage(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }
}
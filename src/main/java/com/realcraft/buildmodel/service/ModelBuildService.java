package com.realcraft.buildmodel.service;

import com.realcraft.buildmodel.model.VoxelBlock;
import com.realcraft.buildmodel.queue.BuildTaskQueue;
import com.realcraft.buildmodel.queue.ModelBuildJob;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModelBuildService {
    private static final ModelBuildService INSTANCE = new ModelBuildService();

    private final ModelDownloader downloader = new ModelDownloader();

    private ModelBuildService() {
    }

    public static ModelBuildService getInstance() {
        return INSTANCE;
    }

    public void startBuild(ServerPlayerEntity player, String url) {
        World world = player.getEntityWorld();
        MinecraftServer server = world.getServer();

        CompletableFuture<List<VoxelBlock>> future;
        try {
            future = downloader.download(url);
        } catch (RuntimeException e) {
            // 地址非法等同步异常：终止本次构建（尚未放置任何方块，无需回滚）
            sendError(player, "模型数据获取失败: " + rootMessage(e));
            return;
        }

        sendInfo(player, "正在下载模型数据: " + url);
        future.whenComplete((blocks, error) -> {
            if (error != null) {
                server.execute(() -> sendError(player, "模型数据获取失败: " + rootMessage(error)));
                return;
            }
            // 玩家即使已离线，任务仍需入队并走完队列
            server.execute(() -> {
                ModelBuildJob job = ModelBuildJob.fromVoxelBlocks(player, world, blocks);
                BuildTaskQueue.getInstance().enqueue(job);
                sendInfo(player, "已获取 " + blocks.size() + " 个方块，开始分批构建...");
            });
        });
    }

    private void sendInfo(ServerPlayerEntity player, String message) {
        if (player.isDisconnected()) {
            return;
        }
        player.sendMessage(Text.literal(message).withColor(0xFFFFFF55));
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

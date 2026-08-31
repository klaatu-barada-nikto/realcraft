package com.realcraft.buildmodel.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.realcraft.buildmodel.model.VoxelBlock;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModelDownloader {
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();

    public CompletableFuture<List<VoxelBlock>> download(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(this::toVoxelBlocks);
    }

    private List<VoxelBlock> toVoxelBlocks(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new ModelDownloadException("HTTP 状态码异常: " + response.statusCode());
        }
        try {
            JsonArray rows = JsonParser.parseString(response.body()).getAsJsonArray();
            if (rows.isEmpty()) {
                throw new ModelDownloadException("模型数据为空");
            }
            List<VoxelBlock> blocks = new ArrayList<>(rows.size());
            for (JsonElement rowElement : rows) {
                JsonArray row = rowElement.getAsJsonArray();
                blocks.add(new VoxelBlock(
                        row.get(0).getAsString(),
                        row.get(1).getAsInt(),
                        row.get(2).getAsInt(),
                        row.get(3).getAsInt()));
            }
            return blocks;
        } catch (JsonSyntaxException | IllegalStateException | IndexOutOfBoundsException e) {
            throw new ModelDownloadException("JSON 解析失败: " + e.getMessage(), e);
        }
    }
}
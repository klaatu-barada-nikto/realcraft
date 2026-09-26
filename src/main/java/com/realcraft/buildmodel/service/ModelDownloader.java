package com.realcraft.buildmodel.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.realcraft.buildmodel.model.VoxelBlock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModelDownloader {
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;
    private static final long MAX_JSON_BYTES = 16L * 1024 * 1024;
    private static final int READ_CHUNK_BYTES = 8192;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .build();

    public CompletableFuture<List<VoxelBlock>> download(String url) {
        URI uri = validateUrl(url);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApplyAsync(this::toVoxelBlocks);
    }

    /**
     * 校验模型地址合法性：非空、可解析、协议为 HTTP(S) 且含主机名。
     * 非法时立即抛出 {@link ModelDownloadException}，由上层捕获并终止本次构建。
     */
    private static URI validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ModelDownloadException("模型地址为空");
        }
        String trimmed = url.trim();
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException e) {
            throw new ModelDownloadException("模型地址非法: " + trimmed, e);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new ModelDownloadException("模型地址必须是 HTTP(S) 地址: " + trimmed);
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new ModelDownloadException("模型地址缺少主机名: " + trimmed);
        }
        return uri;
    }

    private List<VoxelBlock> toVoxelBlocks(HttpResponse<InputStream> response) {
        if (response.statusCode() != 200) {
            closeQuietly(response.body());
            throw new ModelDownloadException("HTTP 状态码异常: " + response.statusCode());
        }
        byte[] body;
        try (InputStream in = response.body()) {
            body = readLimited(in);
        } catch (IOException e) {
            throw new ModelDownloadException("模型数据读取失败: " + e.getMessage(), e);
        }
        try {
            JsonArray rows = JsonParser.parseString(new String(body, StandardCharsets.UTF_8)).getAsJsonArray();
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
        } catch (ModelDownloadException e) {
            throw e;
        } catch (JsonSyntaxException | IllegalStateException | IndexOutOfBoundsException
                 | NumberFormatException | UnsupportedOperationException e) {
            throw new ModelDownloadException("JSON 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式读取响应体，超过 {@link #MAX_JSON_BYTES} 立即失败，避免超大响应撑爆内存。
     */
    private static byte[] readLimited(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[READ_CHUNK_BYTES];
        long total = 0;
        int read;
        while ((read = in.read(chunk)) != -1) {
            total += read;
            if (total > MAX_JSON_BYTES) {
                throw new ModelDownloadException("模型数据超过大小限制 " + (MAX_JSON_BYTES / (1024 * 1024)) + "MB");
            }
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private static void closeQuietly(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (IOException ignored) {
            // 关闭失败无需处理
        }
    }
}

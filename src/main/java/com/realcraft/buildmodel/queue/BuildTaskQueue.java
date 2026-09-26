package com.realcraft.buildmodel.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BuildTaskQueue {
    private static final BuildTaskQueue INSTANCE = new BuildTaskQueue();

    private final Queue<ModelBuildJob> queue = new ConcurrentLinkedQueue<>();

    private BuildTaskQueue() {
    }

    public static BuildTaskQueue getInstance() {
        return INSTANCE;
    }

    public void enqueue(ModelBuildJob job) {
        queue.add(job);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int pendingJobs() {
        return queue.size();
    }

    public int tick(int maxBlocksPerTick) {
        int placed = 0;
        while (placed < maxBlocksPerTick && !queue.isEmpty()) {
            ModelBuildJob job = queue.peek();
            try {
                while (placed < maxBlocksPerTick && !job.isDone()) {
                    if (job.placeNext()) {
                        placed++;
                    }
                }
            } catch (RuntimeException e) {
                // 放置异常：终止当前构建（不回滚已放置方块），继续处理后续任务
                queue.poll();
                job.notifyAborted(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                continue;
            }
            if (job.isDone()) {
                queue.poll();
                job.notifyComplete();
            }
        }
        return placed;
    }
}

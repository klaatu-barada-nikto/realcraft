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
            while (placed < maxBlocksPerTick && !job.isDone()) {
                if (job.placeNext()) {
                    placed++;
                }
            }
            if (job.isDone()) {
                queue.poll();
                job.notifyComplete();
            }
        }
        return placed;
    }
}
package com.miphi.custompool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int TASK_COUNT = 100;

    public static void main(String[] args) {
        CustomThreadExecutor executor = new CustomThreadExecutor(
                4,
                8,
                12,
                6,
                TimeUnit.SECONDS,
                4
        );

        int rejected = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= TASK_COUNT; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    logger.info("Задача #{} начата", taskId);
                    try {
                        Thread.sleep(100); // имитация работы
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    logger.info("Задача #{} завершена", taskId);
                });
            } catch (RejectedExecutionException e) {
                logger.warn("Задача #{} отклонена", taskId);
                rejected++;
            }
        }

        long endTime = System.currentTimeMillis();
        executor.shutdown();

        try {
            Thread.sleep(2000); // ждём завершения потоков
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logStats(startTime, endTime, rejected);
    }

    private static void logStats(long startTime, long endTime, int rejectedTasks) {
        long totalTime = endTime - startTime;
        int executedTasks = TASK_COUNT - rejectedTasks;
        double avgTime = executedTasks > 0 ? (double) totalTime / executedTasks : 0.0;
        DecimalFormat df = new DecimalFormat("#.##");

        logger.info("Общее время: {} мс", totalTime);
        logger.info("Выполнено задач: {}", executedTasks);
        logger.info("Отклонено задач: {}", rejectedTasks);
        logger.info("Среднее время на задачу: {} мс", df.format(avgTime));
    }
}


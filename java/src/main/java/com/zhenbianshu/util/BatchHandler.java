package com.zhenbianshu.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * created by zbs on 2018/6/27
 */
public class BatchHandler<E> implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(BatchHandler.class);

    private static volatile Map<Class, BatchHandler> instance = Maps.newConcurrentMap();

    private volatile LinkedBlockingDeque<E> batchContainer = new LinkedBlockingDeque<>();

    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newScheduledThreadPool(1);

    private volatile long lastCleanTime = System.currentTimeMillis();

    private Function<List<E>, Boolean> cleaner;

    private long interval;

    private int threshHold;

    private BatchHandler(Function<List<E>, Boolean> cleaner, int threshHold, long interval) {
        this.cleaner = cleaner;
        this.threshHold = threshHold;
        this.interval = interval;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 使用 schedule 防止 timer 被阻塞
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                if (lastCleanTime - System.currentTimeMillis() > interval) {
                    this.clean();
                }
            } catch (Exception e) {
                logger.error("clean container exception", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void submit(E event) {
        batchContainer.add(event);
        if (batchContainer.size() >= threshHold) {
            clean();
        }
    }

    private void clean() {
        List<E> transferList = Lists.newArrayListWithExpectedSize(threshHold);
        batchContainer.drainTo(transferList, 100);

        if (CollectionUtils.isEmpty(transferList)) {
            return;
        }

        try {
            cleaner.apply(transferList);
        } catch (Exception e) {
            logger.error("batch execute error, transferList:{}", transferList, e);
        }
    }

    public static <E> BatchHandler getInstance(Function<List<E>, Boolean> cleaner, int threshHold, long interval) {

        Class jobClass = cleaner.getClass();
        if (instance.get(jobClass) == null) {
            synchronized (BatchHandler.class) {
                if (instance.get(jobClass) == null) {
                    instance.put(jobClass, new BatchHandler<>(cleaner, threshHold, interval));
                }
            }
        }

        return instance.get(jobClass);
    }

}
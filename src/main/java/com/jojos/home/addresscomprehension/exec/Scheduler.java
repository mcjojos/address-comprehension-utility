/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class that creates and executes a periodic action on a collection of {@link SchedulerTask}s.
 *
 * Created by karanikasg@gmail.com.
 */
public class Scheduler<T extends Runnable> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ScheduledExecutorService executorService;
    private final Optional<LocalTime> schedulerRunTime;
    private final long periodInSeconds;

    /**
     * Constructor of the scheduler
     *
     * @param schedulerRunTime optional {@link LocalTime} defining the time the scheduler will start
     *                         if the time is before {@link LocalTime#now()} add a day to start the next day
     * @param periodInSeconds the period between two successive runs.
     * @param threadCount the threads this scheduler will use
     */
    public Scheduler(Optional<LocalTime> schedulerRunTime, long periodInSeconds, int threadCount) {
        this.schedulerRunTime = schedulerRunTime;
        executorService = Executors.newScheduledThreadPool(threadCount);
        this.periodInSeconds = periodInSeconds;
    }

    /**
     * The submit action takes a collection of {@link Runnable}s to execute periodically
     * @param tasks a collection of {@link SchedulerTask}s.
     */
    public void submit(Collection<T> tasks) {

        long initialDelay = 0L;

        if (schedulerRunTime.isPresent()) {
            LocalDateTime dateTimeNow = LocalDateTime.now();
            LocalDateTime nextDateTime = dateTimeNow.
                    withHour(schedulerRunTime.get().getHour()).
                    withMinute(schedulerRunTime.get().getMinute()).
                    withSecond(schedulerRunTime.get().getSecond());
            if (dateTimeNow.compareTo(nextDateTime) > 0) {
                nextDateTime = nextDateTime.plusDays(1);
            }
            Duration duration = Duration.between(dateTimeNow, nextDateTime);
            initialDelay = duration.getSeconds();
        }

        for (Runnable task : tasks) {
            executorService.scheduleAtFixedRate(task, initialDelay, periodInSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * stop all actively executing tasks.
     */
    public void stop() {
        final List<Runnable> rejected = executorService.shutdownNow();
        log.debug("Ongoing rejected tasks: {}", rejected.size());

    }
}

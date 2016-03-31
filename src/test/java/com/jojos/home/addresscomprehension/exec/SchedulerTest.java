/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.exec;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Test for thread safety, scheduling etc
 * <p>
 * Created by karanikasg@gmail.com.
 */
public class SchedulerTest {
    private static final int MAX = 1_000_000;
    private static final CountDownLatch latch = new CountDownLatch(MAX);
    private static final LongAdder longAdder = new LongAdder();

    private static Set<Runnable> tasks;

    @BeforeClass
    public static void setUp() {
        tasks = new HashSet<>();
        for (int i = 0; i < MAX; i++) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    longAdder.increment();
                    latch.countDown();
                }
            };
            tasks.add(task);
        }
    }

    @Test
    public void testTreadSafety() {
        // run the runnables once per dat starting now. dispatch them among 4 threads.
        Scheduler<Runnable> scheduler = new Scheduler<>(Optional.empty(), (24 * 60 * 60), 4);
        scheduler.submit(tasks);
        try {
            latch.await(90, TimeUnit.SECONDS);
            scheduler.stop();
            Assert.assertEquals(0L, latch.getCount());
            Assert.assertEquals(MAX, longAdder.intValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail("Thread interrupted while waiting");
        }
    }

    @Test
    public void testPeriod() {
        Set<Runnable> set = new HashSet<>();
        List<Integer> numbers = new ArrayList<>();
        set.add(() -> numbers.add(1));
        long period = 2000L;
        long sleepPeriod = 7000L;
        // run the task every one second starting from now.
        Scheduler<Runnable> scheduler = new Scheduler<>(Optional.empty(), period / 1000, 1);

        scheduler.submit(set);

        try {
            Thread.sleep(sleepPeriod);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail("Interrupted at a point where it wasn't supposed to");
        }
        scheduler.stop();

        long expected = sleepPeriod / period + 1;   // +1 because we count one more starting now
        Assert.assertEquals(expected, numbers.size());
    }
}

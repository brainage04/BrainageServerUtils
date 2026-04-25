package com.github.brainage04.brainageserverutils.util;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RunnableScheduler {
    public static final int MINIMUM_TICK_DELAY = 2;

    private static class ScheduledTask {
        public final Runnable runnable;
        public int ticksPassed;
        public final int delay;

        public ScheduledTask(Runnable runnable, int delay) {
            this.runnable = runnable;
            this.delay = delay;
        }
    }

    private static final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (scheduledTasks.isEmpty()) return;

            Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                task.ticksPassed++;

                if (task.ticksPassed >= task.delay) {
                    server.execute(task.runnable);
                    iterator.remove();
                }
            }
        });
    }

    public static void scheduleTask(Runnable runnable, int extraTickDelay) {
        if (MINIMUM_TICK_DELAY + extraTickDelay < 1) {
            BrainageServerUtils.LOGGER.error("Runnable scheduled for less than 1 tick in the future - cancelling!");
            return;
        }

        scheduledTasks.add(new ScheduledTask(runnable, MINIMUM_TICK_DELAY + extraTickDelay));
    }

    public static void scheduleTask(Runnable runnable) {
        scheduledTasks.add(new ScheduledTask(runnable, MINIMUM_TICK_DELAY));
    }
}

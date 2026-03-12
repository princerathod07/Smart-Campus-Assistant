package com.campus.utils;

import com.campus.services.AnnouncementService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * NotificationThread — background thread for campus notifications.
 * Demonstrates: Multithreading (Thread, Runnable, synchronized, sleep)
 *
 * Uses:
 * - Implements Runnable
 * - Thread sleep for periodic polling
 * - Synchronized access to shared notification queue
 * - ScheduledExecutorService for scheduled tasks
 */
public class NotificationThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(NotificationThread.class.getName());

    // Shared notification queue — synchronized access
    private static final Queue<Map<String, String>> notificationQueue
        = new LinkedList<>();

    private static final Object LOCK = new Object();

    private final AnnouncementService announcementService;
    private volatile boolean running = true;   // volatile for thread visibility
    private final int pollIntervalSeconds;

    // Scheduled executor for periodic checks
    private ScheduledExecutorService scheduler;

    public NotificationThread(AnnouncementService announcementService, int pollIntervalSeconds) {
        this.announcementService  = announcementService;
        this.pollIntervalSeconds  = pollIntervalSeconds;
    }

    @Override
    public void run() {
        LOGGER.info("[NotificationThread] Started — polling every "
                    + pollIntervalSeconds + "s");

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "campus-notification-poller");
            t.setDaemon(true);   // daemon thread — won't block JVM shutdown
            return t;
        });

        // Schedule a periodic check
        scheduler.scheduleAtFixedRate(this::checkAndEnqueueNotifications,
            0, pollIntervalSeconds, TimeUnit.SECONDS);
    }

    /** Periodic task: check for high-priority announcements and enqueue notifications */
    private void checkAndEnqueueNotifications() {
        try {
            List<Map<String, String>> highs = announcementService.getHighPriorityAnnouncements();

            synchronized (LOCK) {                         // synchronized block
                for (Map<String, String> ann : highs) {
                    Map<String, String> notif = new HashMap<>();
                    notif.put("type",      "ANNOUNCEMENT");
                    notif.put("id",        ann.get("id"));
                    notif.put("title",     ann.get("title"));
                    notif.put("message",   "📢 " + ann.get("content"));
                    notif.put("timestamp", LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")));
                    notif.put("priority",  "HIGH");

                    // Add only if not already queued
                    boolean alreadyQueued = notificationQueue.stream()
                        .anyMatch(n -> ann.get("id").equals(n.get("id")));

                    if (!alreadyQueued) {
                        notificationQueue.offer(notif);
                        LOGGER.info("[NotificationThread] Enqueued notification: "
                                    + ann.get("title"));
                    }
                }

                // Cap queue size
                while (notificationQueue.size() > 50) notificationQueue.poll();
            }
        } catch (Exception e) {
            LOGGER.warning("[NotificationThread] Error during check: " + e.getMessage());
        }
    }

    /** Add an ad-hoc notification (thread-safe) */
    public static void pushNotification(String type, String title, String message) {
        synchronized (LOCK) {
            Map<String, String> notif = new HashMap<>();
            notif.put("type",      type);
            notif.put("title",     title);
            notif.put("message",   message);
            notif.put("id",        "N-" + System.currentTimeMillis());
            notif.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")));
            notif.put("priority",  "MEDIUM");
            notificationQueue.offer(notif);
        }
    }

    /** Drain all pending notifications (thread-safe) */
    public static List<Map<String, String>> drainNotifications() {
        List<Map<String, String>> result = new ArrayList<>();
        synchronized (LOCK) {
            while (!notificationQueue.isEmpty()) {
                result.add(notificationQueue.poll());
            }
        }
        return result;
    }

    /** Peek at notifications without removing */
    public static List<Map<String, String>> peekNotifications(int max) {
        List<Map<String, String>> result = new ArrayList<>();
        synchronized (LOCK) {
            int i = 0;
            for (Map<String, String> n : notificationQueue) {
                if (i++ >= max) break;
                result.add(new HashMap<>(n));
            }
        }
        return result;
    }

    /** Graceful shutdown */
    public void stop() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            LOGGER.info("[NotificationThread] Stopped.");
        }
    }

    public boolean isRunning() {
        return running && scheduler != null && !scheduler.isShutdown();
    }
}

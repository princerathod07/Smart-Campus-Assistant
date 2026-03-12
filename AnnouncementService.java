package com.campus.services;

import com.campus.exceptions.FileStorageException;
import com.campus.utils.FileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnnouncementService — manages campus announcements.
 * Demonstrates: Interface implementation, File Handling, Multithreading integration
 */
public class AnnouncementService implements CampusService {

    private static final String SERVICE_NAME = "AnnouncementService";
    private List<Map<String, String>> announcements = new ArrayList<>();
    private FileManager fileManager;
    private boolean healthy = false;
    private List<String> recentActivity = new ArrayList<>();

    public AnnouncementService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void initialize() throws FileStorageException {
        try {
            List<String> lines = fileManager.readLines("announcements.txt");
            announcements.clear();
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    Map<String, String> a = new LinkedHashMap<>();
                    a.put("id",        parts[0].trim());
                    a.put("title",     parts[1].trim());
                    a.put("content",   parts[2].trim());
                    a.put("author",    parts[3].trim());
                    a.put("priority",  parts[4].trim());  // HIGH, MEDIUM, LOW
                    a.put("timestamp", parts.length > 5 ? parts[5].trim() : "");
                    announcements.add(a);
                }
            }
            healthy = true;
            logActivity("AnnouncementService initialized with " + announcements.size() + " items.");
        } catch (Exception e) {
            healthy = false;
            throw new FileStorageException("Failed to initialize AnnouncementService: " + e.getMessage(), e);
        }
    }

    /** Post a new announcement */
    public Map<String, String> postAnnouncement(String title, String content,
                                                 String author, String priority)
            throws FileStorageException {

        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Announcement title cannot be empty.");

        String id = "ANN-" + System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, String> ann = new LinkedHashMap<>();
        ann.put("id",        id);
        ann.put("title",     title.replace("|", ";"));
        ann.put("content",   content.replace("|", ";"));
        ann.put("author",    author);
        ann.put("priority",  priority != null ? priority : "MEDIUM");
        ann.put("timestamp", timestamp);

        announcements.add(0, ann);
        saveToFile();
        logActivity("Announcement posted: " + title + " by " + author);
        return ann;
    }

    /** Get all announcements sorted by newest first */
    public List<Map<String, String>> getAllAnnouncements() {
        return new ArrayList<>(announcements);
    }

    /** Get high-priority announcements */
    public List<Map<String, String>> getHighPriorityAnnouncements() {
        return announcements.stream()
            .filter(a -> "HIGH".equalsIgnoreCase(a.get("priority")))
            .collect(Collectors.toList());
    }

    /** Get latest N announcements */
    public List<Map<String, String>> getLatest(int n) {
        return announcements.stream().limit(n).collect(Collectors.toList());
    }

    private void saveToFile() throws FileStorageException {
        List<String> lines = announcements.stream()
            .map(a -> a.get("id") + "|" + a.get("title") + "|" + a.get("content")
                    + "|" + a.get("author") + "|" + a.get("priority") + "|" + a.get("timestamp"))
            .collect(Collectors.toList());
        fileManager.writeLines("announcements.txt", lines);
    }

    private void logActivity(String msg) {
        recentActivity.add(0, msg);
        if (recentActivity.size() > 20) recentActivity.remove(recentActivity.size() - 1);
    }

    // --- CampusService interface ---
    @Override public String getServiceName() { return SERVICE_NAME; }

    @Override
    public Map<String, String> getServiceStatus() {
        Map<String, String> s = new LinkedHashMap<>();
        s.put("service",              SERVICE_NAME);
        s.put("status",               healthy ? "UP" : "DOWN");
        s.put("totalAnnouncements",   String.valueOf(announcements.size()));
        long high = announcements.stream().filter(a -> "HIGH".equalsIgnoreCase(a.get("priority"))).count();
        s.put("highPriority",         String.valueOf(high));
        return s;
    }

    @Override public boolean isHealthy()  { return healthy; }
    @Override public void shutdown()      { announcements.clear(); healthy = false; }

    @Override
    public List<String> getRecentActivity(int limit) {
        return recentActivity.stream().limit(limit).collect(Collectors.toList());
    }
}

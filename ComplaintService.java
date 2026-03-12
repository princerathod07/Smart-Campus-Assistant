package com.campus.services;

import com.campus.exceptions.FileStorageException;
import com.campus.utils.FileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ComplaintService — handles student complaint submission and management.
 * Demonstrates: Interface implementation, File Handling, Exception Handling
 */
public class ComplaintService implements CampusService {

    private static final String SERVICE_NAME = "ComplaintService";
    private List<Map<String, String>> complaints = new ArrayList<>();
    private FileManager fileManager;
    private boolean healthy = false;
    private List<String> recentActivity = new ArrayList<>();

    public ComplaintService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void initialize() throws FileStorageException {
        try {
            List<String> lines = fileManager.readLines("complaints.txt");
            complaints.clear();
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Map<String, String> c = new LinkedHashMap<>();
                    c.put("id",          parts[0].trim());
                    c.put("studentId",   parts[1].trim());
                    c.put("studentName", parts[2].trim());
                    c.put("category",    parts[3].trim());
                    c.put("description", parts[4].trim());
                    c.put("status",      parts[5].trim());
                    c.put("timestamp",   parts.length > 6 ? parts[6].trim() : "");
                    complaints.add(c);
                }
            }
            healthy = true;
            logActivity("ComplaintService initialized with " + complaints.size() + " records.");
        } catch (Exception e) {
            healthy = false;
            throw new FileStorageException("Failed to initialize ComplaintService: " + e.getMessage(), e);
        }
    }

    /** Submit a new complaint */
    public Map<String, String> submitComplaint(String studentId, String studentName,
                                                String category, String description)
            throws FileStorageException {

        if (description == null || description.trim().length() < 10) {
            throw new IllegalArgumentException("Complaint description must be at least 10 characters.");
        }

        String id = "CMP-" + System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, String> complaint = new LinkedHashMap<>();
        complaint.put("id",          id);
        complaint.put("studentId",   studentId);
        complaint.put("studentName", studentName);
        complaint.put("category",    category);
        complaint.put("description", description.replace("|", ";"));
        complaint.put("status",      "PENDING");
        complaint.put("timestamp",   timestamp);

        complaints.add(0, complaint);
        saveComplaintsToFile();
        logActivity("Complaint submitted by " + studentName + " [" + category + "]");
        return complaint;
    }

    /** Get complaints by student ID */
    public List<Map<String, String>> getComplaintsByStudent(String studentId) {
        return complaints.stream()
            .filter(c -> studentId.equals(c.get("studentId")))
            .collect(Collectors.toList());
    }

    /** Get all complaints (admin) */
    public List<Map<String, String>> getAllComplaints() {
        return new ArrayList<>(complaints);
    }

    /** Update complaint status */
    public boolean updateStatus(String complaintId, String newStatus) throws FileStorageException {
        Optional<Map<String, String>> opt = complaints.stream()
            .filter(c -> complaintId.equals(c.get("id")))
            .findFirst();
        if (opt.isPresent()) {
            opt.get().put("status", newStatus);
            saveComplaintsToFile();
            logActivity("Complaint " + complaintId + " updated to " + newStatus);
            return true;
        }
        return false;
    }

    private void saveComplaintsToFile() throws FileStorageException {
        List<String> lines = complaints.stream()
            .map(c -> c.get("id") + "|" + c.get("studentId") + "|" + c.get("studentName")
                    + "|" + c.get("category") + "|" + c.get("description")
                    + "|" + c.get("status") + "|" + c.get("timestamp"))
            .collect(Collectors.toList());
        fileManager.writeLines("complaints.txt", lines);
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
        s.put("service",          SERVICE_NAME);
        s.put("status",           healthy ? "UP" : "DOWN");
        s.put("totalComplaints",  String.valueOf(complaints.size()));
        long pending = complaints.stream().filter(c -> "PENDING".equals(c.get("status"))).count();
        s.put("pendingComplaints",String.valueOf(pending));
        return s;
    }

    @Override public boolean isHealthy()  { return healthy; }
    @Override public void shutdown()      { complaints.clear(); healthy = false; }

    @Override
    public List<String> getRecentActivity(int limit) {
        return recentActivity.stream().limit(limit).collect(Collectors.toList());
    }
}

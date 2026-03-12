package com.campus.services;

import com.campus.exceptions.FileStorageException;
import com.campus.utils.FileManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LibraryService — manages campus library books.
 * Demonstrates: Interface implementation, File Handling, Exception Handling
 */
public class LibraryService implements CampusService {

    private static final String SERVICE_NAME = "LibraryService";
    private List<Map<String, String>> books = new ArrayList<>();
    private FileManager fileManager;
    private boolean healthy = false;
    private List<String> recentActivity = new ArrayList<>();

    public LibraryService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void initialize() throws FileStorageException {
        try {
            List<String> lines = fileManager.readLines("books.txt");
            books.clear();
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Map<String, String> book = new LinkedHashMap<>();
                    book.put("id",        parts[0].trim());
                    book.put("title",     parts[1].trim());
                    book.put("author",    parts[2].trim());
                    book.put("isbn",      parts[3].trim());
                    book.put("category",  parts[4].trim());
                    book.put("available", parts[5].trim());
                    book.put("copies",    parts.length > 6 ? parts[6].trim() : "1");
                    books.add(book);
                }
            }
            healthy = true;
            logActivity("Library initialized with " + books.size() + " books.");
        } catch (Exception e) {
            healthy = false;
            throw new FileStorageException("Failed to initialize LibraryService: " + e.getMessage(), e);
        }
    }

    /** Search books by title, author, or category */
    public List<Map<String, String>> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>(books);
        String q = query.toLowerCase().trim();
        return books.stream()
            .filter(b -> b.getOrDefault("title","").toLowerCase().contains(q)
                      || b.getOrDefault("author","").toLowerCase().contains(q)
                      || b.getOrDefault("category","").toLowerCase().contains(q)
                      || b.getOrDefault("isbn","").toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    /** Get all books */
    public List<Map<String, String>> getAllBooks() {
        return new ArrayList<>(books);
    }

    /** Get only available books */
    public List<Map<String, String>> getAvailableBooks() {
        return books.stream()
            .filter(b -> "true".equalsIgnoreCase(b.getOrDefault("available", "true")))
            .collect(Collectors.toList());
    }

    /** Get book by ID */
    public Optional<Map<String, String>> getBookById(String id) {
        return books.stream()
            .filter(b -> id.equals(b.get("id")))
            .findFirst();
    }

    /** Add a new book */
    public void addBook(Map<String, String> book) throws FileStorageException {
        books.add(book);
        saveBooksToFile();
        logActivity("Book added: " + book.get("title"));
    }

    private void saveBooksToFile() throws FileStorageException {
        List<String> lines = books.stream()
            .map(b -> b.get("id") + "|" + b.get("title") + "|" + b.get("author")
                    + "|" + b.get("isbn") + "|" + b.get("category")
                    + "|" + b.get("available") + "|" + b.get("copies"))
            .collect(Collectors.toList());
        fileManager.writeLines("books.txt", lines);
    }

    private void logActivity(String msg) {
        recentActivity.add(0, msg);
        if (recentActivity.size() > 20) recentActivity.remove(recentActivity.size() - 1);
    }

    // --- CampusService interface methods ---

    @Override public String getServiceName() { return SERVICE_NAME; }

    @Override
    public Map<String, String> getServiceStatus() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("service",       SERVICE_NAME);
        status.put("status",        healthy ? "UP" : "DOWN");
        status.put("totalBooks",    String.valueOf(books.size()));
        status.put("availableBooks",String.valueOf(getAvailableBooks().size()));
        return status;
    }

    @Override public boolean isHealthy() { return healthy; }
    @Override public void shutdown()     { books.clear(); healthy = false; }

    @Override
    public List<String> getRecentActivity(int limit) {
        return recentActivity.stream().limit(limit).collect(Collectors.toList());
    }
}

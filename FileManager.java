package com.campus.utils;

import com.campus.exceptions.FileStorageException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * FileManager — handles all file I/O for persistent storage.
 * Demonstrates: File Handling (core Java concept), Singleton pattern
 */
public class FileManager {

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());
    private final String dataDirectory;

    // Singleton instance
    private static FileManager instance;

    private FileManager(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        ensureDirectoryExists();
    }

    /** Singleton accessor */
    public static synchronized FileManager getInstance(String dataDir) {
        if (instance == null) {
            instance = new FileManager(dataDir);
        }
        return instance;
    }

    /** Ensure the data directory exists */
    private void ensureDirectoryExists() {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            LOGGER.info("Data directory created: " + dataDirectory + " = " + created);
        }
    }

    /** Read all non-empty lines from a file */
    public List<String> readLines(String filename) throws FileStorageException {
        File file = new File(dataDirectory, filename);
        List<String> lines = new ArrayList<>();

        if (!file.exists()) {
            // Create empty file if not found
            try {
                file.createNewFile();
                LOGGER.info("Created new data file: " + filename);
            } catch (IOException e) {
                throw new FileStorageException(filename, "CREATE",
                    "Could not create data file", e);
            }
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            throw new FileStorageException(filename, "READ",
                "I/O error reading file", e);
        }
        return lines;
    }

    /** Write lines to a file (overwrites) */
    public void writeLines(String filename, List<String> lines) throws FileStorageException {
        File file = new File(dataDirectory, filename);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            throw new FileStorageException(filename, "WRITE",
                "I/O error writing file", e);
        }
    }

    /** Append a single line to a file */
    public void appendLine(String filename, String line) throws FileStorageException {
        File file = new File(dataDirectory, filename);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new FileStorageException(filename, "APPEND",
                "I/O error appending to file", e);
        }
    }

    /** Check if file exists */
    public boolean fileExists(String filename) {
        return new File(dataDirectory, filename).exists();
    }

    /** Delete a file */
    public boolean deleteFile(String filename) {
        return new File(dataDirectory, filename).delete();
    }

    /** Get path to a data file */
    public String getFilePath(String filename) {
        return dataDirectory + File.separator + filename;
    }

    public String getDataDirectory() { return dataDirectory; }
}

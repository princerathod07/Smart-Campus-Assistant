package com.campus.services;

import java.util.List;
import java.util.Map;

/**
 * CampusService Interface — defines the contract for all campus services.
 * Demonstrates: Java Interfaces
 *
 * All service classes (LibraryService, ComplaintService, AnnouncementService)
 * must implement this interface, demonstrating polymorphism through interfaces.
 */
public interface CampusService {

    /**
     * Initialise the service (load data from files, etc.)
     */
    void initialize() throws Exception;

    /**
     * Get service name identifier
     */
    String getServiceName();

    /**
     * Get service status info
     */
    Map<String, String> getServiceStatus();

    /**
     * Health check
     */
    boolean isHealthy();

    /**
     * Shutdown / cleanup resources
     */
    void shutdown();

    /**
     * Get recent activity for the service
     */
    List<String> getRecentActivity(int limit);
}

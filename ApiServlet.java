package com.campus.servlets;

import com.campus.exceptions.FileStorageException;
import com.campus.services.*;
import com.campus.utils.FileManager;
import com.campus.utils.NotificationThread;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * ApiServlet — unified REST API servlet.
 * Handles: /api/library/*, /api/complaints/*, /api/announcements/*, /api/chat/*, /api/notifications
 * Demonstrates: Polymorphism through CampusService interface, Exception Handling
 */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    // Polymorphic service references through interface
    private CampusService libraryService;
    private CampusService complaintService;
    private CampusService announcementService;

    private NotificationThread notificationThread;
    private Thread notifThread;

    @Override
    public void init() throws ServletException {
        String dataPath = getServletContext().getRealPath("/data");
        FileManager fm = FileManager.getInstance(dataPath);

        // Dynamic Method Dispatch — actual types resolved at runtime
        libraryService      = new LibraryService(fm);
        complaintService    = new ComplaintService(fm);
        announcementService = new AnnouncementService(fm);

        try {
            libraryService.initialize();
            complaintService.initialize();
            announcementService.initialize();

            // Seed data if empty
            seedDataIfNeeded(fm);

        } catch (Exception e) {
            throw new ServletException("Service initialization failed: " + e.getMessage(), e);
        }

        // Start background notification thread (Multithreading)
        notificationThread = new NotificationThread(
            (AnnouncementService) announcementService, 30);
        notifThread = new Thread(notificationThread, "campus-notif-main");
        notifThread.setDaemon(true);
        notifThread.start();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");

        String path = req.getPathInfo();
        if (path == null) { sendError(res, 400, "No path"); return; }

        try {
            if (path.startsWith("/library")) {
                handleLibraryGet(req, res);
            } else if (path.startsWith("/complaints")) {
                handleComplaintsGet(req, res);
            } else if (path.startsWith("/announcements")) {
                handleAnnouncementsGet(req, res);
            } else if (path.startsWith("/notifications")) {
                handleNotifications(req, res);
            } else if (path.startsWith("/timetable")) {
                handleTimetable(req, res);
            } else if (path.startsWith("/status")) {
                handleStatus(res);
            } else {
                sendError(res, 404, "Unknown API endpoint: " + path);
            }
        } catch (FileStorageException e) {
            sendError(res, 500, "Storage error: " + e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");

        String path = req.getPathInfo();

        try {
            if (path.startsWith("/complaints/submit")) {
                handleComplaintSubmit(req, res);
            } else if (path.startsWith("/announcements/post")) {
                handleAnnouncementPost(req, res);
            } else if (path.startsWith("/chat")) {
                handleChat(req, res);
            } else {
                sendError(res, 404, "Unknown POST endpoint: " + path);
            }
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (FileStorageException e) {
            sendError(res, 500, "Storage error: " + e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Server error: " + e.getMessage());
        }
    }

    // ---- GET Handlers ----

    private void handleLibraryGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String query = req.getParameter("q");
        LibraryService lib = (LibraryService) libraryService;
        List<Map<String, String>> books = query != null && !query.isEmpty()
            ? lib.searchBooks(query) : lib.getAllBooks();
        sendJson(res, 200, books);
    }

    private void handleComplaintsGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false);
        ComplaintService cs = (ComplaintService) complaintService;
        List<Map<String, String>> complaints;

        if (session != null) {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            if (user != null && "ADMIN".equals(user.get("role"))) {
                complaints = cs.getAllComplaints();
            } else if (user != null) {
                complaints = cs.getComplaintsByStudent((String) user.get("userId"));
            } else {
                complaints = new ArrayList<>();
            }
        } else {
            complaints = new ArrayList<>();
        }
        sendJson(res, 200, complaints);
    }

    private void handleAnnouncementsGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        AnnouncementService as = (AnnouncementService) announcementService;
        String limitStr = req.getParameter("limit");
        List<Map<String, String>> anns;
        if (limitStr != null) {
            anns = as.getLatest(Integer.parseInt(limitStr));
        } else {
            anns = as.getAllAnnouncements();
        }
        sendJson(res, 200, anns);
    }

    private void handleNotifications(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        List<Map<String, String>> notifs = NotificationThread.peekNotifications(10);
        sendJson(res, 200, notifs);
    }

    private void handleTimetable(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // Static timetable (in production would be from file/DB)
        List<Map<String, String>> schedule = getTimetableData();
        sendJson(res, 200, schedule);
    }

    private void handleStatus(HttpServletResponse res) throws IOException {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("libraryService",      libraryService.getServiceStatus());
        status.put("complaintService",    complaintService.getServiceStatus());
        status.put("announcementService", announcementService.getServiceStatus());
        status.put("notificationThread",  notifThread.isAlive() ? "RUNNING" : "STOPPED");
        sendJson(res, 200, status);
    }

    // ---- POST Handlers ----

    private void handleComplaintSubmit(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpSession session = req.getSession(false);
        String studentId   = "GUEST";
        String studentName = "Guest";

        if (session != null && session.getAttribute("user") != null) {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            studentId   = (String) user.get("userId");
            studentName = (String) user.get("fullName");
        }

        String category    = req.getParameter("category");
        String description = req.getParameter("description");

        ComplaintService cs = (ComplaintService) complaintService;
        Map<String, String> complaint = cs.submitComplaint(
            studentId, studentName, category, description);

        NotificationThread.pushNotification("COMPLAINT",
            "New Complaint", studentName + " submitted a " + category + " complaint.");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",   true);
        response.put("message",   "Complaint submitted successfully!");
        response.put("complaint", complaint);
        sendJson(res, 200, response);
    }

    private void handleAnnouncementPost(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        String title    = req.getParameter("title");
        String content  = req.getParameter("content");
        String priority = req.getParameter("priority");

        HttpSession session = req.getSession(false);
        String author = "Admin";
        if (session != null && session.getAttribute("user") != null) {
            author = (String) ((Map<?, ?>) session.getAttribute("user")).get("fullName");
        }

        AnnouncementService as = (AnnouncementService) announcementService;
        Map<String, String> ann = as.postAnnouncement(title, content, author, priority);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",      true);
        response.put("message",      "Announcement posted!");
        response.put("announcement", ann);
        sendJson(res, 200, response);
    }

    private void handleChat(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String message = req.getParameter("message");
        if (message == null || message.trim().isEmpty()) {
            sendError(res, 400, "Message cannot be empty"); return;
        }
        String reply = campusAI(message.trim().toLowerCase());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("reply",   reply);
        sendJson(res, 200, response);
    }

    // ---- Campus AI Chatbot Logic ----
    private String campusAI(String msg) {
        if (contains(msg, "library", "book", "borrow", "reading"))
            return "📚 The Campus Library is open Monday–Saturday, 8 AM – 8 PM, and Sunday 10 AM – 4 PM. You can search available books in the Library tab. New arrivals are updated every Monday!";

        if (contains(msg, "timetable", "schedule", "class", "lecture", "timing"))
            return "🕐 Your timetable is available in the Timetable section of your dashboard. Lectures run from 9 AM – 5 PM, Mon–Fri. Lab sessions are on Wed & Thu afternoons.";

        if (contains(msg, "complaint", "issue", "problem", "report"))
            return "📝 You can submit a complaint using the Complaints tab. Categories include: Infrastructure, Academic, Hostel, Cafeteria, and IT Support. All complaints are reviewed within 48 hours.";

        if (contains(msg, "wifi", "internet", "network"))
            return "📶 Campus WiFi: Network SSID is 'SmartCampus_Secure'. Use your student credentials to connect. IT helpdesk: it.support@campus.edu | Ext. 1200.";

        if (contains(msg, "cafeteria", "canteen", "food", "lunch", "meal"))
            return "🍽️ Cafeteria hours: Breakfast 7:30–9:00 AM, Lunch 12:00–2:00 PM, Snacks 4:00–5:30 PM. Special meal plans available at the admin office.";

        if (contains(msg, "hostel", "dorm", "accommodation", "room"))
            return "🏠 Hostel allocation is managed by the Student Affairs office (Block C, Room 102). Warden: +1-555-CAMPUS. Check-in before 10 PM on weekdays, 11 PM weekends.";

        if (contains(msg, "exam", "result", "grade", "marks"))
            return "📊 Exam schedules and results are posted on the Student Portal and campus notice boards. Contact your department office for grade queries. Results publish within 3 weeks of exam completion.";

        if (contains(msg, "fee", "payment", "tuition", "due"))
            return "💳 Fee payment portal is available at finance.campus.edu. Due date for this semester: March 31. Late fee applies after the deadline. Contact finance@campus.edu for queries.";

        if (contains(msg, "admission", "register", "enrol"))
            return "🎓 For new admissions, visit admissions.campus.edu. Required documents: 10+2 Marksheet, ID Proof, Passport Photos. Admissions office: Block A, Room 001.";

        if (contains(msg, "sports", "gym", "fitness", "court"))
            return "⚽ Sports facilities include: Football field, Cricket ground, Basketball courts, Tennis courts, and a Fitness centre. Gym hours: 6 AM – 9 PM daily. Bring your student ID.";

        if (contains(msg, "medical", "health", "clinic", "doctor", "nurse"))
            return "🏥 Campus Medical Centre is in Block H. Hours: 8 AM – 8 PM (Mon-Sat). Emergency: Ext. 999. Nearest hospital: City General (2 km).";

        if (contains(msg, "contact", "phone", "email", "address"))
            return "📞 Campus Contacts:\n• Reception: +1-555-CAMPUS\n• Admin: admin@campus.edu\n• IT Support: Ext. 1200\n• Library: Ext. 1350\n• Medical: Ext. 999";

        if (contains(msg, "hi", "hello", "hey", "good morning", "good afternoon"))
            return "👋 Hello! I'm CampusBot, your Smart Campus Assistant. I can help you with library hours, timetables, complaints, facilities, contacts, and more. What can I help you with today?";

        if (contains(msg, "thank", "thanks", "bye", "goodbye"))
            return "😊 You're welcome! Have a great day on campus. Remember, I'm here 24/7 for any campus queries. Take care!";

        if (contains(msg, "help", "what can you do", "features"))
            return "🤖 I can help with:\n• 📚 Library hours & book search\n• 🕐 Class timetables\n• 📝 Complaint submission\n• 🍽️ Cafeteria menus\n• 📶 WiFi & IT support\n• 💳 Fee information\n• 🏥 Health & medical\n• ⚽ Sports & fitness\n• And much more! Just ask!";

        return "🤔 I'm not sure about that. Try asking about: library hours, timetable, complaints, cafeteria, WiFi, fees, hostel, or type 'help' to see all topics!";
    }

    private boolean contains(String msg, String... keywords) {
        for (String k : keywords) if (msg.contains(k)) return true;
        return false;
    }

    // ---- Static Timetable Data ----
    private List<Map<String, String>> getTimetableData() {
        List<Map<String, String>> schedule = new ArrayList<>();
        Object[][] data = {
            {"Monday",    "09:00","10:30","Data Structures","Dr. Smith",   "Room 204"},
            {"Monday",    "11:00","12:30","Algorithms",     "Dr. Johnson", "Lab 3"},
            {"Monday",    "14:00","15:30","Database Systems","Dr. Lee",    "Room 101"},
            {"Tuesday",   "09:00","10:30","OOP with Java",  "Dr. Patel",  "Room 202"},
            {"Tuesday",   "11:00","12:30","Computer Networks","Dr. Kim",   "Room 305"},
            {"Wednesday", "09:00","10:30","Data Structures","Dr. Smith",   "Room 204"},
            {"Wednesday", "14:00","17:00","Lab - Java",     "Dr. Patel",  "Lab 1"},
            {"Thursday",  "09:00","10:30","Algorithms",     "Dr. Johnson", "Lab 3"},
            {"Thursday",  "11:00","12:30","Database Systems","Dr. Lee",   "Room 101"},
            {"Thursday",  "14:00","17:00","Lab - Networks", "Dr. Kim",    "Lab 2"},
            {"Friday",    "09:00","10:30","OOP with Java",  "Dr. Patel",  "Room 202"},
            {"Friday",    "11:00","12:30","Computer Networks","Dr. Kim",   "Room 305"},
        };
        for (Object[] row : data) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("day",      (String) row[0]);
            entry.put("start",    (String) row[1]);
            entry.put("end",      (String) row[2]);
            entry.put("subject",  (String) row[3]);
            entry.put("lecturer", (String) row[4]);
            entry.put("room",     (String) row[5]);
            schedule.add(entry);
        }
        return schedule;
    }

    // ---- Utility Methods ----
    private void sendJson(HttpServletResponse res, int status, Object data) throws IOException {
        res.setStatus(status);
        PrintWriter out = res.getWriter();
        out.print(toJson(data));
        out.flush();
    }

    private void sendError(HttpServletResponse res, int status, String msg) throws IOException {
        Map<String, Object> err = Map.of("success", false, "error", msg);
        sendJson(res, status, err);
    }

    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        if (obj == null)  return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Boolean || obj instanceof Number) return obj.toString();
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            map.forEach((k, v) -> {
                if (sb.length() > 1) sb.append(",");
                sb.append("\"").append(escapeJson(k)).append("\":").append(toJson(v));
            });
            return sb.append("}").toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            list.forEach(item -> {
                if (sb.length() > 1) sb.append(",");
                sb.append(toJson(item));
            });
            return sb.append("]").toString();
        }
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    private String escapeJson(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }

    private void seedDataIfNeeded(FileManager fm) throws FileStorageException {
        // Seed books
        if (fm.readLines("books.txt").isEmpty()) {
            List<String> books = Arrays.asList(
                "BK001|Introduction to Java|James Gosling|978-0134685991|Programming|true|5",
                "BK002|Clean Code|Robert C. Martin|978-0132350884|Software Engineering|true|3",
                "BK003|Design Patterns|Gang of Four|978-0201633610|Software Engineering|true|2",
                "BK004|Data Structures & Algorithms|Thomas Cormen|978-0262033848|Computer Science|true|4",
                "BK005|Computer Networks|Andrew Tanenbaum|978-0132126953|Networking|false|1",
                "BK006|Database System Concepts|Silberschatz|978-0073523323|Databases|true|3",
                "BK007|Operating System Concepts|Silberschatz|978-1118063330|Systems|true|2",
                "BK008|Artificial Intelligence|Stuart Russell|978-0134610993|AI/ML|true|2",
                "BK009|The Pragmatic Programmer|Hunt & Thomas|978-0135957059|Software Engineering|true|3",
                "BK010|Head First Java|Kathy Sierra|978-0596009205|Programming|true|4"
            );
            fm.writeLines("books.txt", books);
        }
        // Seed announcements
        if (fm.readLines("announcements.txt").isEmpty()) {
            List<String> anns = Arrays.asList(
                "ANN-001|Mid-Semester Exams|Mid-semester examinations begin on March 20th. Check your schedule on the portal.|Admin|HIGH|2025-03-10 09:00:00",
                "ANN-002|Library Extended Hours|Library will remain open until 10 PM during exam week.|Library Dept|MEDIUM|2025-03-09 14:00:00",
                "ANN-003|Campus WiFi Upgrade|Network upgrade scheduled Sunday 2–6 AM. Brief outage expected.|IT Department|MEDIUM|2025-03-08 11:00:00",
                "ANN-004|Sports Day Registration|Annual Sports Day is April 5th. Register at the Sports Office by March 25.|Student Affairs|LOW|2025-03-07 10:00:00",
                "ANN-005|Fee Payment Reminder|Last date to pay tuition fees is March 31st. Avoid late charges.|Finance Office|HIGH|2025-03-06 09:00:00"
            );
            fm.writeLines("announcements.txt", anns);
        }
    }

    @Override
    public void destroy() {
        if (notificationThread != null) notificationThread.stop();
        libraryService.shutdown();
        complaintService.shutdown();
        announcementService.shutdown();
    }
}

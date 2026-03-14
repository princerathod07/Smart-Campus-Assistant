package com.campus.servlets;

import com.campus.service.bookservice;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private bookservice bookService;

    @Override
    public void init() throws ServletException {
        // REMOVED: FileManager, dataPath, and all fm.writeLines(...) blocks
        this.bookService = new bookservice();
        System.out.println("ApiServlet initialized: Connected to Supabase services.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        if (path != null && path.contains("/library/books")) {
            // Triggers the SQL fetch we wrote earlier
            bookService.getAllBooks();
            out.print("{\"status\": \"success\", \"data\": \"Fetched from database\"}");
        } else {
            res.setStatus(404);
            out.print("{\"error\": \"Endpoint not found\"}");
        }
    }
}
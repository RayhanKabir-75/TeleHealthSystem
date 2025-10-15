package com.mycompany.ths.server.app;

import com.mycompany.ths.server.db.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DoctorService {
    /** Returns doctor display names from users table where role='doctor'. */
    public static String listNames() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT username FROM users WHERE role='doctor' ORDER BY username")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // You can format however you like:
                    names.add("Dr. " + rs.getString("username"));
                }
            }
        }
        // Wire format: OK|Dr. lee|Dr. singh|Dr. ahmed
        return "OK|" + String.join("|", names);
    }
}

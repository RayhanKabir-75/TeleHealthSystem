package com.mycompany.ths.server.notes;

import com.mycompany.ths.server.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotesService {

    public void add(String patient, String doctor, String content) throws Exception {
        String sql = "INSERT INTO notes(patient, doctor, content) VALUES (?,?,?)";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patient);
            ps.setString(2, doctor);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    /** Return notes where this user is either the patient or the doctor. */
    public List<NoteRow> listForUser(String user) throws Exception {
        String sql = """
            SELECT id, patient, doctor, content, created_at
            FROM notes
            WHERE patient = ? OR doctor = ?
            ORDER BY created_at DESC
        """;
        List<NoteRow> out = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, user);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new NoteRow(
                            rs.getLong("id"),
                            rs.getString("patient"),
                            rs.getString("doctor"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return out;
    }
}

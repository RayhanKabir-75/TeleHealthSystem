package com.mycompany.ths.server.appt;

import com.mycompany.ths.server.db.DB;
import java.sql.*;
import java.time.LocalDateTime;

public final class ApptService {
  public static String book(String patient, String doctor, LocalDateTime when, String location) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "INSERT INTO appointments(patient, doctor, when_at, location, status) VALUES(?,?,?,?, 'PENDING')")) {
      ps.setString(1, patient);
      ps.setString(2, doctor);
      ps.setTimestamp(3, Timestamp.valueOf(when));
      ps.setString(4, location);
      ps.executeUpdate();
      return "OK";
    }
  }

  /** LIST_APPT|patient or LIST_APPT|doctor|Dr Name */
  public static String list(String who, String arg) throws SQLException {
    StringBuilder out = new StringBuilder("OK");
    String sql;
    boolean byDoctor = "doctor".equalsIgnoreCase(who);
    if (byDoctor) {
      sql = "SELECT id, patient, doctor, when_at, status, location FROM appointments WHERE doctor=? ORDER BY when_at";
    } else {
      sql = "SELECT id, patient, doctor, when_at, status, location FROM appointments WHERE patient=? ORDER BY when_at";
    }
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, arg);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.append('|')
             .append(rs.getLong("id")).append(',')
             .append(rs.getString("patient")).append(',')
             .append(rs.getString("doctor")).append(',')
             .append(rs.getTimestamp("when_at").toLocalDateTime()).append(',')
             .append(rs.getString("status")).append(',')
             .append(rs.getString("location"));
        }
      }
    }
    return out.toString();
  }

  public static String update(long id, LocalDateTime when, String status) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "UPDATE appointments SET when_at=?, status=? WHERE id=?")) {
      ps.setTimestamp(1, Timestamp.valueOf(when));
      ps.setString(2, status);
      ps.setLong(3, id);
      return ps.executeUpdate() == 1 ? "OK" : "ERR|Not found";
    }
  }
}

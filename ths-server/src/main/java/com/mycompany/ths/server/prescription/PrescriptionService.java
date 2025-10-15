package com.mycompany.ths.server.prescription;

import com.mycompany.ths.server.db.DB;
import java.sql.*;

public final class PrescriptionService {

  public static String add(String patient, String doctor, String medicine, int qty) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "INSERT INTO prescriptions(patient, doctor, medicine, qty, status) VALUES(?,?,?,?, 'PENDING')")) {
      ps.setString(1, patient);
      ps.setString(2, doctor);
      ps.setString(3, medicine);
      ps.setInt(4, qty);
      ps.executeUpdate();
      return "OK";
    }
  }

  /** list by role:
   * LIST_PRESC|patient|<username>
   * LIST_PRESC|doctor|<doctorName>
   */
  public static String list(String who, String key) throws SQLException {
    String sql = "patient".equalsIgnoreCase(who)
        ? "SELECT id,patient,doctor,medicine,qty,status,created_at FROM prescriptions WHERE patient=? ORDER BY created_at DESC"
        : "SELECT id,patient,doctor,medicine,qty,status,created_at FROM prescriptions WHERE doctor=? ORDER BY created_at DESC";

    StringBuilder out = new StringBuilder("OK");
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, key);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.append('|')
             .append(rs.getLong("id")).append(',')
             .append(rs.getString("patient")).append(',')
             .append(rs.getString("doctor")).append(',')
             .append(rs.getString("medicine")).append(',')
             .append(rs.getInt("qty")).append(',')
             .append(rs.getString("status")).append(',')
             .append(rs.getTimestamp("created_at").toLocalDateTime());
        }
      }
    }
    return out.toString();
  }

  /** UPDATE_PRESC|<id>|<status>  (doctor approves/rejects/fills) */
  public static String updateStatus(long id, String status) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement("UPDATE prescriptions SET status=? WHERE id=?")) {
      ps.setString(1, status);
      ps.setLong(2, id);
      return ps.executeUpdate() == 1 ? "OK" : "ERR|Not found";
    }
  }
}

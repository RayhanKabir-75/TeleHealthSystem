package com.mycompany.ths.server.vitals;

import com.mycompany.ths.server.db.DB;
import java.sql.*;
import java.time.LocalDateTime;

public final class VitalsService {
  public static String add(String patient, int pulse, double temp, int resp, String bp) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "INSERT INTO vitals(patient, pulse, temp_c, resp, bp, recorded_at) VALUES(?,?,?,?,?,?)")) {
      ps.setString(1, patient);
      ps.setInt(2, pulse);
      ps.setDouble(3, temp);
      ps.setInt(4, resp);
      ps.setString(5, bp);
      ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
      ps.executeUpdate();
      return "OK";
    }
  }

  public static String list(String patient) throws SQLException {
    StringBuilder out = new StringBuilder("OK");
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "SELECT pulse, temp_c, resp, bp, recorded_at FROM vitals WHERE patient=? ORDER BY recorded_at DESC")) {
      ps.setString(1, patient);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.append('|')
             .append(rs.getInt("pulse")).append(',')
             .append(rs.getDouble("temp_c")).append(',')
             .append(rs.getInt("resp")).append(',')
             .append(rs.getString("bp")).append(',')
             .append(rs.getTimestamp("recorded_at").toLocalDateTime());
        }
      }
    }
    return out.toString();
  }
}

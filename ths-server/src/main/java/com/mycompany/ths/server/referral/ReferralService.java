package com.mycompany.ths.server.referral;

import com.mycompany.ths.server.db.DB;
import java.sql.*;
import java.time.LocalDateTime;

public final class ReferralService {

  /** ADD_REFERRAL|patient|doctor|hospital|procedure|YYYY-MM-DDTHH:MM */
  public static String add(String patient, String doctor, String hospital, String procedure, LocalDateTime whenAt)
      throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "INSERT INTO referrals(patient, doctor, hospital, procedure_desc, when_at, status) " +
           "VALUES(?,?,?,?,?, 'PENDING')")) {
      ps.setString(1, patient);
      ps.setString(2, doctor);
      ps.setString(3, hospital);
      ps.setString(4, procedure);
      ps.setTimestamp(5, Timestamp.valueOf(whenAt));
      ps.executeUpdate();
      return "OK";
    }
  }

  /** LIST_REFERRAL|patient|<user>  OR  LIST_REFERRAL|doctor|<docName> */
  public static String list(String who, String key) throws SQLException {
    String sql = "patient".equalsIgnoreCase(who)
      ? "SELECT id,patient,doctor,hospital,procedure_desc,when_at,status,created_at " +
        "FROM referrals WHERE patient=? ORDER BY when_at DESC"
      : "SELECT id,patient,doctor,hospital,procedure_desc,when_at,status,created_at " +
        "FROM referrals WHERE doctor=? ORDER BY when_at DESC";

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
             .append(rs.getString("hospital")).append(',')
             .append(rs.getString("procedure_desc").replace('|','/').replace(',',';')).append(',')
             .append(rs.getTimestamp("when_at").toLocalDateTime()).append(',')
             .append(rs.getString("status")).append(',')
             .append(rs.getTimestamp("created_at").toLocalDateTime());
        }
      }
    }
    return out.toString();
  }

  /** UPDATE_REFERRAL|<id>|YYYY-MM-DDTHH:MM|<status> */
  public static String update(long id, LocalDateTime whenAt, String status) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "UPDATE referrals SET when_at=?, status=? WHERE id=?")) {
      ps.setTimestamp(1, Timestamp.valueOf(whenAt));
      ps.setString(2, status);
      ps.setLong(3, id);
      return ps.executeUpdate() == 1 ? "OK" : "ERR|Not found";
    }
  }
}

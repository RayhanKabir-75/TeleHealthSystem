package com.mycompany.ths.server.profile;

import com.mycompany.ths.server.db.DB;
import java.sql.*;
import java.time.LocalDate;

public final class ProfileService {

  /** GET_PROFILE|<username>  -> OK|full_name,dob,gender,address */
  public static String get(String username) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
           "SELECT full_name, dob, gender, address FROM user_profile WHERE username=?")) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return "OK|,,,"; // empty profile
        String name = nz(rs.getString(1));
        Date dob = rs.getDate(2);
        String gender = nz(rs.getString(3));
        String addr = nz(rs.getString(4));
        return "OK|" + name + "," + (dob==null? "" : dob.toLocalDate()) + "," + gender + "," + addr.replace(',', ';');
      }
    }
  }

  /** UPSERT_PROFILE|username|fullName|YYYY-MM-DD|gender|address */
  public static String upsert(String username, String name, String dobIso, String gender, String address) throws SQLException {
    try (Connection c = DB.get()) {
      // Upsert: try update; if 0 rows, insert
      try (PreparedStatement up = c.prepareStatement(
        "UPDATE user_profile SET full_name=?, dob=?, gender=?, address=? WHERE username=?")) {
        up.setString(1, name);
        up.setObject(2, (dobIso==null||dobIso.isBlank())? null : java.sql.Date.valueOf(LocalDate.parse(dobIso)));
        up.setString(3, gender);
        up.setString(4, address);
        up.setString(5, username);
        int n = up.executeUpdate();
        if (n == 1) return "OK";
      }
      try (PreparedStatement ins = c.prepareStatement(
        "INSERT INTO user_profile(username, full_name, dob, gender, address) VALUES(?,?,?,?,?)")) {
        ins.setString(1, username);
        ins.setString(2, name);
        ins.setObject(3, (dobIso==null||dobIso.isBlank())? null : java.sql.Date.valueOf(LocalDate.parse(dobIso)));
        ins.setString(4, gender);
        ins.setString(5, address);
        ins.executeUpdate();
        return "OK";
      }
    }
  }

  private static String nz(String s) { return s==null? "" : s; }
}

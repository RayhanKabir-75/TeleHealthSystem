package com.mycompany.ths.server.auth;

import com.mycompany.ths.server.db.DB;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

public final class AuthService {
  private static String hash(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte x : b) sb.append(String.format("%02x", x));
      return sb.toString();
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  public static String signup(String user, String pass, String role) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
            "INSERT INTO users(username, pass_hash, role) VALUES(?,?,?)")) {
      ps.setString(1, user);
      ps.setString(2, hash(pass));
      ps.setString(3, role);
      ps.executeUpdate();
      return "OK";
    } catch (SQLIntegrityConstraintViolationException dup) {
      return "ERR|User exists";
    }
  }

  /** returns "OK|role" or "ERR|reason" */
  public static String login(String user, String pass) throws SQLException {
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(
            "SELECT pass_hash, role FROM users WHERE username=?")) {
      ps.setString(1, user);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return "ERR|No such user";
        String hash = rs.getString(1);
        String role = rs.getString(2);
        return hash.equals(hash(pass)) ? ("OK|" + role) : "ERR|Invalid password";
      }
    }
  }
}

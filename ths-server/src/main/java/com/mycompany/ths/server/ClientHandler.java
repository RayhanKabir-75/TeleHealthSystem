package com.mycompany.ths.server;

import com.mycompany.ths.server.db.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
  private final Socket s;
  public ClientHandler(Socket s){ this.s = s; }

  @Override public void run() {
    try (var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         var out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true)) {

      String line;
      while ((line = in.readLine()) != null) {
        String[] p = line.split("\\|");
        switch (p[0]) {
          case "PING" -> out.println("PONG");
          case "LOGIN" -> out.println(login(p[1], p[2]));
          case "BOOK_APPT" -> out.println(bookAppt(p[1], p[2], p[3]));
          case "ADD_VITAL" -> out.println(addVital(p[1], p[2], p[3], p[4], p[5]));
          case "ADD_RX"    -> out.println(addRx(p[1], p[2], Integer.parseInt(p[3])));
          case "LIST_APPT" -> out.println(listApptJson(p[1]));     // return JSON text
          case "UPDATE_APPT" -> out.println(updateAppt(Integer.parseInt(p[1]), p[2], p[3]));
          case "ADD_NOTE" -> out.println(addNote(Integer.parseInt(p[1]), p[2]));
          case "ADD_REF"  -> out.println(addReferral(p[1], p[2], p[3]));
          default -> out.println("ERR|UNKNOWN");
        }
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  private String login(String user, String pass) {
    try (var c = DB.get();
         var ps = c.prepareStatement("SELECT role FROM users WHERE username=? AND password=?")) {
      ps.setString(1, user); ps.setString(2, pass);
      var rs = ps.executeQuery();
      return rs.next() ? "OK|" + rs.getString(1) : "ERR|BAD_CREDENTIALS";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String bookAppt(String patient, String doctor, String isoDateTime) {
    try (var c = DB.get();
         var ps = c.prepareStatement(
           "INSERT INTO appointments(patient,doctor,datetime,status) VALUES(?,?,?,?)")) {
      ps.setString(1, patient); ps.setString(2, doctor);
      ps.setString(3, isoDateTime); ps.setString(4, "PENDING");
      ps.executeUpdate();
      return "OK|BOOKED";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String addVital(String patient, String pulse, String temp, String resp, String bp) {
    try (var c = DB.get();
         var ps = c.prepareStatement(
           "INSERT INTO vitals(patient,pulse,temperature,respiration,bp) VALUES(?,?,?,?,?)")) {
      ps.setString(1, patient);
      ps.setInt(2, Integer.parseInt(pulse));
      ps.setDouble(3, Double.parseDouble(temp));
      ps.setInt(4, Integer.parseInt(resp));
      ps.setString(5, bp);
      ps.executeUpdate();
      return "OK|VITAL_SAVED";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String addRx(String patient, String med, int qty) {
    try (var c = DB.get();
         var ps = c.prepareStatement(
           "INSERT INTO prescriptions(patient,medicine,quantity,status) VALUES(?,?,?,?)")) {
      ps.setString(1, patient); ps.setString(2, med);
      ps.setInt(3, qty); ps.setString(4, "PENDING");
      ps.executeUpdate();
      return (qty < 5) ? "OK|LOW_STOCK" : "OK|RX_ADDED";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String listApptJson(String who) {
    // keep simple: return pipe/CSV or JSON text (you can parse on client)
    try (var c = DB.get();
         var ps = c.prepareStatement(
           "SELECT id,patient,doctor,datetime,status FROM appointments WHERE patient=? OR doctor=? ORDER BY datetime")) {
      ps.setString(1, who); ps.setString(2, who);
      var rs = ps.executeQuery();
      var sb = new StringBuilder("OK");
      while (rs.next()) {
        sb.append("|").append(rs.getInt(1)).append(",").append(rs.getString(2)).append(",")
          .append(rs.getString(3)).append(",").append(rs.getTimestamp(4).toLocalDateTime()).append(",")
          .append(rs.getString(5));
      }
      return sb.toString();
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String updateAppt(int id, String newIsoDT, String status) {
    try (var c = DB.get();
         var ps = c.prepareStatement("UPDATE appointments SET datetime=?, status=? WHERE id=?")) {
      ps.setString(1, newIsoDT); ps.setString(2, status); ps.setInt(3, id);
      return ps.executeUpdate() == 1 ? "OK|UPDATED" : "ERR|NOT_FOUND";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String addNote(int apptId, String text) {
    try (var c = DB.get();
         var ps = c.prepareStatement(
           "INSERT INTO notes(appointment_id, text, created_at) VALUES (?,?,?)")) {
      // create table notes(id, appointment_id, text, created_at) in SQL
      ps.setInt(1, apptId); ps.setString(2, text);
      ps.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
      ps.executeUpdate();
      return "OK|NOTE_ADDED";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }

  private String addReferral(String patient, String hospital, String procedure) {
    try (var c = DB.get();
         var ps = c.prepareStatement(
           "INSERT INTO referrals(patient, hospital, procedure, date) VALUES (?,?,?,CURRENT_DATE)")) {
      ps.setString(1, patient); ps.setString(2, hospital); ps.setString(3, procedure);
      ps.executeUpdate();
      return "OK|REF_ADDED";
    } catch (Exception e) { return "ERR|" + e.getMessage(); }
  }
}

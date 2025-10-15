package com.mycompany.ths.server;

import com.mycompany.ths.server.appt.ApptService;
import com.mycompany.ths.server.auth.AuthService;
import com.mycompany.ths.server.db.DB;
import com.mycompany.ths.server.vitals.VitalsService;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import com.mycompany.ths.server.notes.NotesService;
import com.mycompany.ths.server.notes.NoteRow;
import java.time.LocalDateTime;

public class THSServer {

    // 1) <<< place the shared in-memory stores here (class level, not inside main) >>>
    private static final ConcurrentHashMap<String, String> USERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> ROLES = new ConcurrentHashMap<>();
    private static final NotesService NOTES = new NotesService();

    private static final java.util.concurrent.atomic.AtomicInteger APPT_SEQ
            = new java.util.concurrent.atomic.AtomicInteger(1);

    private static final java.util.List<Appt> APPTS
            = new java.util.concurrent.CopyOnWriteArrayList<>();

    private enum ApptStatus {
        PENDING, CONFIRMED, CANCELLED
    }

    private static final class Appt {

        final int id;
        final String patient;
        String doctor;
        java.time.LocalDateTime when;
        String location;
        ApptStatus status;

        Appt(int id, String patient, String doctor,
                java.time.LocalDateTime when, String location) {
            this.id = id;
            this.patient = patient;
            this.doctor = doctor;
            this.when = when;
            this.location = (location == null || location.isBlank()) ? "Online" : location;
            this.status = ApptStatus.PENDING;
        }
    }

    private static final java.util.List<Vital> VITALS
            = new java.util.concurrent.CopyOnWriteArrayList<>();

    private static final class Vital {

        final String user;
        final int pulse;
        final double temp;
        final int resp;
        final String bp;
        final java.time.LocalDateTime at;

        Vital(String user, int pulse, double temp, int resp, String bp, java.time.LocalDateTime at) {
            this.user = user;
            this.pulse = pulse;
            this.temp = temp;
            this.resp = resp;
            this.bp = bp;
            this.at = at;
        }
    }

    private static final java.util.concurrent.atomic.AtomicInteger APPT_ID = new java.util.concurrent.atomic.AtomicInteger(1);

// ---- PRESCRIPTIONS ----
    private static final java.util.concurrent.atomic.AtomicInteger PRESC_ID = new java.util.concurrent.atomic.AtomicInteger(1);
    private static final java.util.List<String> PRESCS
            = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
// row: id,patient,doctor,medicine,qty,status,createdIso

// ---- REFERRALS ----
    private static final java.util.concurrent.atomic.AtomicInteger REF_ID = new java.util.concurrent.atomic.AtomicInteger(1);
    private static final java.util.List<String> REFS
            = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
// row: id,patient,doctor,hospital,procedure,whenIso,status

    private static String hash(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) {
                sb.append(String.format("%02x", x));
            }
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }

    // 2) <<< the request dispatcher >>>
    private static String handle(String line) {
        try {
            if (line == null) {
                return "ERR|Empty";
            }
            line = line.trim();
            System.out.println("REQ: " + line);

            String[] parts = line.split("\\|");
            if (parts.length == 0) {
                return "ERR|BadRequest";
            }

            String cmd = parts[0].trim().toUpperCase();

            switch (cmd) {
                case "PING":
                    return "PONG";

                case "SIGNUP": { // SIGNUP|user|pass|role
                    if (parts.length < 4) {
                        return "ERR|Bad SIGNUP";
                    }
                    return AuthService.signup(parts[1].trim(), parts[2], parts[3].trim().toLowerCase());
                }
                case "LOGIN": { // LOGIN|user|pass
                    if (parts.length < 3) {
                        return "ERR|Bad LOGIN";
                    }
                    return AuthService.login(parts[1].trim(), parts[2]);
                }

                case "BOOK_APPT": { // BOOK_APPT|patient|doctor|2025-10-15T14:30|loc
                    var dt = java.time.LocalDateTime.parse(parts[3]);
                    return ApptService.book(parts[1], parts[2], dt, parts.length > 4 ? parts[4] : "Online");
                }
                case "LIST_APPT": { // LIST_APPT|user|role
                    String user = parts.length > 1 ? parts[1].trim() : "";
                    String role = parts.length > 2 ? parts[2].trim().toLowerCase() : "patient";

                    String sql = role.equals("doctor")
                            ? "SELECT id,patient,doctor,when_at,location,status FROM appointments WHERE doctor=? ORDER BY when_at DESC"
                            : "SELECT id,patient,doctor,when_at,location,status FROM appointments WHERE patient=? ORDER BY when_at DESC";

                    try (var c = DB.get(); var ps = c.prepareStatement(sql)) {
                        ps.setString(1, user);
                        try (var rs = ps.executeQuery()) {
                            StringBuilder sb = new StringBuilder("OK");
                            while (rs.next()) {
                                sb.append("|").append(rs.getLong(1)).append(",")
                                        .append(rs.getString(2)).append(",")
                                        .append(rs.getString(3)).append(",")
                                        .append(rs.getTimestamp(4).toLocalDateTime()).append(",")
                                        .append(rs.getString(5)).append(",")
                                        .append(rs.getString(6));
                            }
                            return sb.toString();
                        }
                    }
                }

                case "UPDATE_APPT": { // UPDATE_APPT|id|2025-10-20T09:00|APPROVED
                    long id = Long.parseLong(parts[1]);
                    var dt = java.time.LocalDateTime.parse(parts[2]);
                    return ApptService.update(id, dt, parts[3]);
                }

                case "LIST_DOCTORS": {
                    return com.mycompany.ths.server.app.DoctorService.listNames();
                }

                case "ADD_VITAL": { // ADD_VITAL|patient|72|36.8|16|120/80
                    return VitalsService.add(parts[1], Integer.parseInt(parts[2]),
                            Double.parseDouble(parts[3]),
                            Integer.parseInt(parts[4]), parts[5]);
                }
                case "LIST_VITAL": { // LIST_VITAL|patient
                    return VitalsService.list(parts[1]);
                }

                // ===== Prescriptions =====
                case "ADD_PRESC": { // ADD_PRESC|patient|doctor|medicine|qty
                    if (parts.length < 5) {
                        return "ERR|Bad ADD_PRESC";
                    }
                    return com.mycompany.ths.server.prescription.PrescriptionService.add(
                            parts[1], parts[2], parts[3], Integer.parseInt(parts[4]));
                }
                // LIST_PRESC|username|role
                case "LIST_PRESC": {
                    if (parts.length < 3) {
                        return "ERR|Bad LIST_PRESC";
                    }
                    String user = parts[1].trim();
                    String role = parts[2].trim().toLowerCase();

                    String sql = role.equals("doctor")
                            ? "SELECT id, patient, doctor, medicine, qty, status, created_at "
                            + "FROM prescriptions WHERE doctor=? ORDER BY created_at DESC"
                            : "SELECT id, patient, doctor, medicine, qty, status, created_at "
                            + "FROM prescriptions WHERE patient=? ORDER BY created_at DESC";

                    try (var c = com.mycompany.ths.server.db.DB.get(); var ps = c.prepareStatement(sql)) {
                        ps.setString(1, user);
                        try (var rs = ps.executeQuery()) {
                            StringBuilder sb = new StringBuilder("OK");
                            while (rs.next()) {
                                sb.append('|')
                                        .append(rs.getLong(1)).append(',')
                                        .append(rs.getString(2)).append(',')
                                        .append(rs.getString(3)).append(',')
                                        .append(rs.getString(4)).append(',')
                                        .append(rs.getInt(5)).append(',')
                                        .append(rs.getString(6)).append(',')
                                        .append(rs.getTimestamp(7)); // ISO-ish
                            }
                            return sb.toString();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "ERR|" + e.getMessage();
                    }
                }
                case "UPDATE_PRESC": { // UPDATE_PRESC|id|status
                    if (parts.length < 3) {
                        return "ERR|Bad UPDATE_PRESC";
                    }
                    return com.mycompany.ths.server.prescription.PrescriptionService.updateStatus(
                            Long.parseLong(parts[1]), parts[2]);
                }

// ===== Notes =====
                case "ADD_NOTE": { // ADD_NOTE|patient|doctor|content
                    if (parts.length < 4) {
                        return "ERR|Bad ADD_NOTE";
                    }
                    NOTES.add(parts[1].trim(), parts[2].trim(), parts[3]);
                    return "OK";
                }

                case "LIST_NOTE": { // LIST_NOTE|username
                    if (parts.length < 2) {
                        return "ERR|Bad LIST_NOTE";
                    }
                    var rows = NOTES.listForUser(parts[1].trim());

                    StringBuilder sb = new StringBuilder("OK");
                    for (NoteRow n : rows) {
                        String safe = n.getContent() == null ? "" : n.getContent().replace(',', ';');
                        sb.append('|')
                                .append(n.getId()).append(',')
                                .append(n.getPatient()).append(',')
                                .append(n.getDoctor()).append(',')
                                .append(safe).append(',')
                                .append(n.getCreatedAt());
                    }
                    return sb.toString();
                }

// ===== Profile =====
                case "GET_PROFILE": { // GET_PROFILE|username
                    if (parts.length < 2) {
                        return "ERR|Bad GET_PROFILE";
                    }
                    return com.mycompany.ths.server.profile.ProfileService.get(parts[1]);
                }
                case "UPSERT_PROFILE": { // UPSERT_PROFILE|username|fullName|YYYY-MM-DD|gender|address
                    if (parts.length < 6) {
                        return "ERR|Bad UPSERT_PROFILE";
                    }
                    return com.mycompany.ths.server.profile.ProfileService.upsert(
                            parts[1], parts[2], parts[3], parts[4], parts[5]);
                }
                case "ADD_REF":
                case "ADD_REFERRAL": {
                    // ADD_REFERRAL|patient|doctor|hospital|procedure|YYYY-MM-DD[ T]HH:mm[:ss]
                    if (parts.length < 6) {
                        return "ERR|Bad ADD_REFERRAL";
                    }

                    // parts[5] is the datetime string from client (allow space or T, with/without seconds)
                    String ts = parts[5].trim().replace(' ', 'T');
                    if (ts.length() == 16) {
                        ts = ts + ":00";      // yyyy-MM-ddTHH:mm  -> add seconds
                    }
                    java.time.LocalDateTime whenAt = java.time.LocalDateTime.parse(ts);

                    return com.mycompany.ths.server.referral.ReferralService.add(
                            parts[1].trim(), // patient
                            parts[2].trim(), // doctor
                            parts[3].trim(), // hospital
                            parts[4].trim(), // procedure
                            whenAt);
                }

                case "LIST_REF":
                case "LIST_REFERRAL": {
                    // LIST_REF|username|role   where role = patient|doctor
                    if (parts.length < 3) {
                        return "ERR|Bad LIST_REF";
                    }
                    String user = parts[1].trim();
                    String role = parts[2].trim().toLowerCase();
                    // ReferralService.list expects (who, key) -> ("patient"|"doctor", username)
                    return com.mycompany.ths.server.referral.ReferralService.list(role, user);
                }

                case "UPDATE_REF":
                case "UPDATE_REFERRAL": { // UPDATE_REFERRAL|id|YYYY-MM-DDTHH:mm[:ss]|status
                    if (parts.length < 4) {
                        return "ERR|Bad UPDATE_REF";
                    }
                    long id = Long.parseLong(parts[1].trim());

                    String ts = parts[2].trim().replace(' ', 'T'); // allow space or T
                    if (ts.length() == 16) {
                        ts += ":00";            // allow HH:mm without seconds
                    }
                    var whenAt = java.time.LocalDateTime.parse(ts);

                    String status = parts[3].trim();
                    return com.mycompany.ths.server.referral.ReferralService.update(id, whenAt, status);
                }

                default:
                    return "ERR|Unknown command";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ERR|" + ex.getMessage();
        }
    }

    // 3) server socket loop
    public static void main(String[] args) throws Exception {
        com.mycompany.ths.server.db.SchemaInit.run();
        try (ServerSocket ss = new ServerSocket(5555)) {
            System.out.println("THS Server listening on 5555");
            while (true) {
                Socket s = ss.accept();
                new Thread(new ClientHandler(s)).start();
            }
        }
    }

    // 4) per-connection reader/writer that calls handle()
    static class ClientHandler implements Runnable {

        private final Socket socket;

        ClientHandler(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream())); var out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.println(handle(line)); // <<< call the dispatcher
                }
            } catch (IOException e) {
                System.out.println("Client closed: " + e.getMessage());
            }
        }
    }
}

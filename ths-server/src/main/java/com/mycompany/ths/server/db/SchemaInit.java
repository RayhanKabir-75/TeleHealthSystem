package com.mycompany.ths.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

public final class SchemaInit {
    private static final String DB_NAME  = "ths_enhanced";
    private static final String ROOT_URL = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";
    private static final String DB_URL   = "jdbc:mysql://localhost:3306/" + DB_NAME + "?serverTimezone=UTC";

    // TODO: set to your local MySQL credentials
    private static final String USER = "admin";
    private static final String PASS = "admin";

    private SchemaInit() {}

    public static void run() {
        try {
            // 1) Ensure database exists
            try (Connection c = DriverManager.getConnection(ROOT_URL, USER, PASS);
                 Statement  s = c.createStatement()) {
                s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " DEFAULT CHARACTER SET utf8mb4");
            }

            // 2) Create / patch tables inside DB
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement  s = c.createStatement()) {

                // --- USERS (with pass_hash) ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS users (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username  VARCHAR(64) UNIQUE NOT NULL,
                      pass_hash CHAR(64)     NOT NULL,
                      role      ENUM('patient','doctor','admin') NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                // --- APPOINTMENTS ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS appointments (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      patient  VARCHAR(64) NOT NULL,
                      doctor   VARCHAR(64) NOT NULL,
                      when_at  DATETIME     NOT NULL,
                      location VARCHAR(128) NOT NULL,
                      status   ENUM('PENDING','APPROVED','REJECTED','CANCELLED','COMPLETED')
                               NOT NULL DEFAULT 'PENDING',
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      INDEX ix_patient_when (patient, when_at),
                      INDEX ix_doctor_when  (doctor,  when_at)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                // --- VITALS ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS vitals (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      patient    VARCHAR(64) NOT NULL,
                      pulse      INT         NOT NULL,
                      temp_c     DOUBLE      NOT NULL,
                      resp       INT         NOT NULL,
                      bp         VARCHAR(16) NOT NULL,
                      recorded_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      INDEX ix_patient_time (patient, recorded_at)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                // --- PRESCRIPTIONS ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS prescriptions (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      patient   VARCHAR(64) NOT NULL,
                      doctor    VARCHAR(64) NOT NULL,
                      medicine  VARCHAR(128) NOT NULL,
                      qty       INT         NOT NULL,
                      status    ENUM('PENDING','APPROVED','REJECTED','FILLED')
                                NOT NULL DEFAULT 'PENDING',
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      INDEX ix_presc_patient (patient),
                      INDEX ix_presc_doctor  (doctor)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                // --- NOTES ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS notes (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      patient   VARCHAR(64) NOT NULL,
                      doctor    VARCHAR(64) NOT NULL,
                      content   TEXT        NOT NULL,
                      created_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      INDEX ix_notes_patient_time (patient, created_at)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                // --- REFERRALS ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS referrals (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      patient       VARCHAR(64)  NOT NULL,
                      doctor        VARCHAR(64)  NOT NULL,
                      hospital      VARCHAR(128) NOT NULL,
                      procedure_desc VARCHAR(256) NOT NULL,
                      when_at       DATETIME     NOT NULL,
                      status        ENUM('PENDING','APPROVED','REJECTED')
                                    NOT NULL DEFAULT 'PENDING',
                      created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      INDEX ix_ref_patient_time (patient, when_at),
                      INDEX ix_ref_doctor_time  (doctor,  when_at)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                // --- USER PROFILE ---
                s.addBatch("""
                    CREATE TABLE IF NOT EXISTS user_profile (
                      username  VARCHAR(64) PRIMARY KEY,
                      full_name VARCHAR(128),
                      dob       DATE,
                      gender    VARCHAR(16),
                      address   VARCHAR(256),
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                ON UPDATE CURRENT_TIMESTAMP,
                      CONSTRAINT uq_profile_user UNIQUE (username)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """);

                s.executeBatch();

                // Patch older schemas that might be missing pass_hash
                ensureUsersHasPassHash(c);
            }

            System.out.println("[Schema] database and tables ensured.");
        } catch (Exception e) {
            throw new RuntimeException("Schema init failed: " + e.getMessage(), e);
        }
    }

    /** Adds users.pass_hash if the column is missing (portable for older MySQL). */
    private static void ensureUsersHasPassHash(Connection c) {
        final String check =
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "  AND TABLE_NAME = 'users' " +
            "  AND COLUMN_NAME = 'pass_hash'";
        try (PreparedStatement ps = c.prepareStatement(check);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate(
                        "ALTER TABLE users " +
                        "ADD COLUMN pass_hash CHAR(64) NOT NULL AFTER username"
                    );
                    System.out.println("[Schema] users.pass_hash column added.");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to ensure users.pass_hash: " + ex.getMessage(), ex);
        }
    }
    private static boolean hasColumn(Connection c, String table, String column) throws Exception {
    final String q = """
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
          AND COLUMN_NAME = ?
    """;
    try (PreparedStatement ps = c.prepareStatement(q)) {
        ps.setString(1, table);
        ps.setString(2, column);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
}

private static void ensureAppointmentsWhenAt(Connection c) {
    try (Statement s = c.createStatement()) {
        if (!hasColumn(c, "appointments", "when_at")) {
            // If older schemas had a 'datetime' column, rename it
            if (hasColumn(c, "appointments", "datetime")) {
                s.executeUpdate("ALTER TABLE appointments " +
                                "CHANGE COLUMN `datetime` `when_at` DATETIME NOT NULL");
                System.out.println("[Schema] appointments.datetime -> when_at");
            } else if (hasColumn(c, "appointments", "date_time")) {
                s.executeUpdate("ALTER TABLE appointments " +
                                "CHANGE COLUMN `date_time` `when_at` DATETIME NOT NULL");
                System.out.println("[Schema] appointments.date_time -> when_at");
            } else {
                // otherwise just add it
                s.executeUpdate("ALTER TABLE appointments " +
                                "ADD COLUMN `when_at` DATETIME NOT NULL AFTER doctor");
                System.out.println("[Schema] appointments.when_at added");
            }
        }
    } catch (Exception ex) {
        throw new RuntimeException("Failed to ensure appointments.when_at: " + ex.getMessage(), ex);
    }
}

}

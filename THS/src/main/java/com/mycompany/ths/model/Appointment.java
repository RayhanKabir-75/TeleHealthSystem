package com.mycompany.ths.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Appointment {

    public enum Status { PENDING, CONFIRMED, CANCELLED, COMPLETED }

    private final int id;
    private final String patient;
    private final String doctor;
    private final LocalDateTime dateTime;
    private final String location;
    private final Status status;

    // Full constructor
    public Appointment(int id, String patient, String doctor,
                       LocalDateTime dateTime, String location, Status status) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.location = location;
        this.status = status == null ? Status.PENDING : status;
    }

    // ---- STATIC FACTORY expected by AppointmentService ----
    public static Appointment from(int id, String patient, String doctor,
                                   LocalDateTime dateTime, String status, String location) {
        Status st;
        try {
            st = status == null ? Status.PENDING : Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            st = Status.PENDING;
        }
        return new Appointment(id, patient, doctor, dateTime, location, st);
    }

    // (optional) parse a single CSV row: "id,patient,doctor,datetime,status,location"
    public static Appointment fromCsv(String row) {
        String[] c = row.split(",", -1);
        int id = Integer.parseInt(c[0].trim());
        String patient = c[1].trim();
        String doctor = c[2].trim();
        LocalDateTime dt = LocalDateTime.parse(c[3].trim());
        String status = c[4].trim();
        String location = c.length > 5 ? c[5].trim() : "Online";
        return from(id, patient, doctor, dt, status, location);
    }

    // Getters (used by TableView cell value factories)
    public int getId() { return id; }
    public String getPatient() { return patient; }
    public String getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getLocation() { return location; }
    public Status getStatus() { return status; }

    @Override public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient='" + patient + '\'' +
                ", doctor='" + doctor + '\'' +
                ", dateTime=" + dateTime +
                ", location='" + location + '\'' +
                ", status=" + status +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment a)) return false;
        return id == a.id;
    }

    @Override public int hashCode() { return Objects.hash(id); }
}

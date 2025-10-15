package com.mycompany.ths.model;

import java.time.LocalDateTime;

public class Prescription {
    private final long id;
    private final String patient;
    private final String doctor;
    private final String medicine;
    private final int qty;
    private final String status;
    private final LocalDateTime createdAt;

    public Prescription(long id, String patient, String doctor, String medicine,
                        int qty, String status, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.medicine = medicine;
        this.qty = qty;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getPatient() { return patient; }
    public String getDoctor() { return doctor; }
    public String getMedicine() { return medicine; }
    public int getQty() { return qty; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

package com.mycompany.ths.model;

import java.time.LocalDateTime;

/** Vital signs entered by a patient. */
public class VitalSign {

    private String username;            // owner (patient username)
    private int pulse;                  // bpm
    private double temperature;         // °C
    private int respiration;            // breaths/min
    private String bloodPressure;       // e.g., "120/80"
    private LocalDateTime recordedAt;   // timestamp

    public VitalSign() {}

    public VitalSign(String username, int pulse, double temperature, int respiration, String bloodPressure) {
        this.username = username;
        this.pulse = pulse;
        this.temperature = temperature;
        this.respiration = respiration;
        this.bloodPressure = bloodPressure;
    }

    // Getters used by TableView & controller
    public String getUsername() { return username; }
    public int getPulse() { return pulse; }
    public double getTemperature() { return temperature; }
    public int getRespiration() { return respiration; }
    public String getBloodPressure() { return bloodPressure; }
    public LocalDateTime getRecordedAt() { return recordedAt; }

    // Setters used when parsing server response
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    // (Optional) Aliases if your older code used different names:
    public double getTemp(){ return getTemperature(); }
    public int getResp(){ return getRespiration(); }
    public String getBp(){ return getBloodPressure(); }
    public LocalDateTime getTimestamp(){ return getRecordedAt(); }

    @Override
    public String toString() {
        return "VitalSign{" +
                "username='" + username + '\'' +
                ", pulse=" + pulse +
                ", temperature=" + temperature +
                ", respiration=" + respiration +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", recordedAt=" + recordedAt +
                '}';
    }
}

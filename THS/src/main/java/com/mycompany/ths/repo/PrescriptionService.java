package com.mycompany.ths.repo;

import com.mycompany.ths.model.Prescription;
import com.mycompany.ths.util.Remote;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionService {
    // server returns "yyyy-MM-dd HH:mm:ss"
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Load prescriptions for the given user and role ("patient" or "doctor"). */
    public List<Prescription> listFor(String username, String role) throws Exception {
        String res = Remote.send("LIST_PRESC|" + username + "|" + role);
        if (!res.startsWith("OK")) throw new Exception(res);

        String[] rows = res.split("\\|", -1);
        List<Prescription> out = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            // id,patient,doctor,medicine,qty,status,created_at
            String[] c = rows[i].split(",", -1);
            if (c.length < 7) continue;

            LocalDateTime created =
                LocalDateTime.parse(c[6].replace('T',' ').substring(0, 19), TS);

            out.add(new Prescription(
                Long.parseLong(c[0]), // id
                c[1],                 // patient
                c[2],                 // doctor
                c[3],                 // medicine
                Integer.parseInt(c[4]), // qty
                c[5],                 // status
                created               // createdAt
            ));
        }
        return out;
    }

    public String add(String patient, String doctor, String medicine, int qty) throws Exception {
        return Remote.send("ADD_PRESC|" + patient + "|" + doctor + "|" + medicine + "|" + qty);
    }

    public String updateStatus(long id, String status) throws Exception {
        return Remote.send("UPDATE_PRESC|" + id + "|" + status);
    }

    public String doctorAdjustQty(long id, int newQty) throws Exception {
        return Remote.send("ADJUST_QTY|" + id + "|" + newQty);
    }
}

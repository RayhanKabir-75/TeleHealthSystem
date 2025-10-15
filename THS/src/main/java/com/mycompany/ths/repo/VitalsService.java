package com.mycompany.ths.repo;

import com.mycompany.ths.model.VitalSign;
import com.mycompany.ths.util.Remote;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Protocol used with server:
 *  ADD_VITAL|username|pulse|temp|resp|bp
 *  LIST_VITAL|username
 *
 * LIST_VITAL response:
 *  OK|pulse,temp,resp,bp,datetime|pulse,temp,resp,bp,datetime|...
 *  where datetime is ISO-8601 (LocalDateTime.parse-friendly)
 */
public class VitalsService {

    public String add(String username, int pulse, double temp, int resp, String bp) throws Exception {
        return Remote.send("ADD_VITAL|" + username + "|" + pulse + "|" + temp + "|" + resp + "|" + bp);
    }

    public List<VitalSign> listParsed(String username) throws Exception {
        String res = Remote.send("LIST_VITAL|" + username);
        List<VitalSign> out = new ArrayList<>();
        if (res == null || res.isBlank() || !res.startsWith("OK")) return out;

        String[] rows = res.split("\\|");
        for (int i = 1; i < rows.length; i++) {
            String row = rows[i].trim();
            if (row.isEmpty()) continue;
            String[] c = row.split(",", -1); // pulse,temp,resp,bp,datetime
            if (c.length < 4) continue;

            VitalSign v = new VitalSign(
                    username,
                    Integer.parseInt(c[0].trim()),
                    Double.parseDouble(c[1].trim()),
                    Integer.parseInt(c[2].trim()),
                    c[3].trim()
            );
            if (c.length >= 5 && !c[4].trim().isEmpty()) {
                try { v.setRecordedAt(LocalDateTime.parse(c[4].trim())); } catch (Exception ignore) {}
            }
            out.add(v);
        }
        return out;
    }
}

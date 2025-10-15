package com.mycompany.ths.repo;

import com.mycompany.ths.util.Remote;

public class ReferralService {
    public String add(String patient, String hospital, String procedure) throws Exception {
        return Remote.send("ADD_REF|" + patient + "|" + hospital + "|" + procedure); // OK|REF_ADDED
    }
}

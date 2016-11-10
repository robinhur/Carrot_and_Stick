package com.huza.carrot_and_stick;

/**
 * Created by HuZA on 2016-10-23.
 */

public class UserData {

    String uid;
    String email;
    String name;
    int credit;

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public UserData() {}

    public UserData(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public UserData(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

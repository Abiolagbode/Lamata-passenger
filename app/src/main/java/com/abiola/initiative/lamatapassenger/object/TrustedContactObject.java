package com.abiola.initiative.lamatapassenger.object;


public class TrustedContactObject {
    private String uid;
    private String name;
    private String email;
    private String mobnum;
    private String prof_pic;

    public TrustedContactObject(String uid, String name, String email, String mobnum, String prof_pic) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.mobnum = mobnum;
        this.prof_pic = prof_pic;
    }

    public String getUid() {
        return uid;
    }

    public String getProf_pic() {
        return prof_pic;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobnum() {
        return mobnum;
    }
}

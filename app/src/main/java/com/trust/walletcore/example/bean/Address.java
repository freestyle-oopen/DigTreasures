package com.trust.walletcore.example.bean;

public class Address {
    private String zjc;
    private String account;

    public Address(String zjc, String account) {
        this.zjc = zjc;
        this.account = account;
    }

    public String getZjc() {
        return zjc;
    }

    public void setZjc(String zjc) {
        this.zjc = zjc;
    }

    public String getAddress() {
        return account;
    }

    public void setAddress(String account) {
        this.account = account;
    }
}

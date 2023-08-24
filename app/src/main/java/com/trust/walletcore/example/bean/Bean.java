package com.trust.walletcore.example.bean;

import java.util.List;

public class Bean {

    private String status;
    private String message;
    private List<Result> result;
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }
    public List<Result> getResult() {
        return result;
    }

}

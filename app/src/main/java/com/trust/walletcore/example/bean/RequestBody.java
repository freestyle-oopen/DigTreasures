package com.trust.walletcore.example.bean;
import java.util.ArrayList;
import java.util.List;

public class RequestBody {

    private String jsonrpc = "2.0";
    private int id;
    private String method = "eth_getBalance";
    private ArrayList<String> params;
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public String getMethod() {
        return method;
    }

    public void setParams(ArrayList<String> params) {
        this.params = params;
    }
    public ArrayList<String> getParams() {
        return params;
    }

}

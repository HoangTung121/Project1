package com.example.myreadbookapplication.model;

public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;

    public boolean isSuccess(){ return success;}
    public String getMessage(){ return message;}
    public Object getData(){ return data;}
}

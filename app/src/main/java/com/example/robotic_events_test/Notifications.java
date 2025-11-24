package com.example.robotic_events_test;

import java.sql.Timestamp;
import java.util.Date;

public class Notifications {
    private Boolean respondable;
    private String message;
    private String senderId;
    private String recieverId;

    private long timestamp;



    public Notifications(Boolean repsondable,String message, String senderId, String recieverId, long timestamp){
        this.respondable = respondable;
        this.message = message;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId(){
        return senderId;
    }

    public String getRecieverId(){
        return recieverId;
    }

    public long getTimestamp(){
        return timestamp;
    }




}

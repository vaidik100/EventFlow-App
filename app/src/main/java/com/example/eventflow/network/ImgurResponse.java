package com.example.eventflow.network;

public class ImgurResponse {
    public Data data;
    public boolean success;
    public int status;

    public class Data {
        public String link; // This is the public image URL
    }
}

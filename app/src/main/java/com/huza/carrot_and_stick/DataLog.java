package com.huza.carrot_and_stick;

/**
 * Created by HuZA on 2016-12-29.
 */

public class DataLog {

    long timestamp;
    String updown;
    int delta;
    String content;

    public DataLog() {
    }
    public DataLog(long timestamp, String updown, int delta, String content) {
        this.timestamp = timestamp;
        this.updown = updown;
        this.delta = delta;
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUpdown() {
        return updown;
    }

    public void setUpdown(String updown) {
        this.updown = updown;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }
}

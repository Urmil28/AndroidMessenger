package com.ravensltd.ravens.ChatUI;

/**
 * Created by jatin on 6/10/17.
 */
//package com.ravensltd.ravens.ChatUI;
public class MessageItemInfo {
    private String message,type;
    private Long time;
    private boolean seen;
    private String from;

    public MessageItemInfo(String message, String type, Long time, boolean seen) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public MessageItemInfo() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}

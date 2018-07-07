package com.ravensltd.ravens.home.storiesFragment;

/**
 * Created by jatin on 10/10/17.
 */

public class StoryItemInfo {

    private String storyImage, thumbImage, uid,storykey;
    private long time;
    int likes;


    public StoryItemInfo() {
    }

    public StoryItemInfo(String storyImage, String thumbImage, String uid, long time, int likes) {
        this.storyImage = storyImage;
        this.thumbImage = thumbImage;
        this.uid = uid;
        this.time = time;
        this.likes = likes;
    }

    public String getStoryImage() {
        return storyImage;
    }

    public void setStoryImage(String storyImage) {
        this.storyImage = storyImage;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getStorykey() {
        return storykey;
    }

    public void setStorykey(String storykey) {
        this.storykey = storykey;
    }
}
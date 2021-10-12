package com.example.wolf;

public class Chating {
    private String messageText;
    private String messageUser;
    private int messageIdentity;
    private int usercode;

    public Chating(String messageText, String messageUser,int messageIdentity,int usercode) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageIdentity = messageIdentity;
        this.usercode = usercode;
        // Initialize to current time
        //顯示時間
        //messageTime = new Date().getTime();
    }

    public Chating(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public int getMessageIdentity(){return messageIdentity;}
    public void setMessageIdentity(int messageIdentity) {
        this.messageIdentity = messageIdentity;
    }

    public int getUsercode(){return usercode;}
    public void setUsercode(int usercode) {
        this.usercode = usercode;
    }

}

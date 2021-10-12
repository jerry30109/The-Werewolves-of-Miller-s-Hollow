package com.example.wolf;

public class UserDataInfo
{
    private String UserID = null;
    private int sex = 3; // 0 男 1 女
    private int Identity = 12; // 0 狼人 1 預言家 2 守衛 3 村民
    private boolean Live = true; // true 生 false 死
    private boolean speak = false; //true 可 false 不可
    private String Vote = null;

    public UserDataInfo()
    {
        this.UserID = "";
        this.sex = 3;
        this.Identity = 12;
        this.Live = true;
        this.speak = false;
        this.Vote = null;
    }
    public UserDataInfo(String ID,int sexy,int i,boolean l,boolean s,String v)
    {
        this.UserID = ID;
        this.sex = sexy;
        this.Identity = i;
        this.Live = l;
        this.speak = s;
        this.Vote = v;
    }
    public void setUserID(String ID)
    {
        this.UserID = ID;
    }
    public void setSex(int s){this.sex = s;}
    public void setIdentity(int i)
    {
        this.Identity = i;
    }
    public void setLive(boolean l)
    {
        this.Live = l;
    }
    public void setSpeak(boolean s)
    {
        this.speak = s;
    }
    public void setVote(String v){this.Vote = v;}
    public String getUserID()
    {
        return this.UserID;
    }
    public int getSex(){return this.sex;}
    public int getIdentity()
    {
        return this.Identity;
    }
    public Boolean getLive()
    {
        return this.Live;
    }
    public Boolean getSpeak()
    {
        return this.speak;
    }
    public String getVote(){return this.Vote;}
}

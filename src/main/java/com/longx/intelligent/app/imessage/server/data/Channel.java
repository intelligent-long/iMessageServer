package com.longx.intelligent.app.imessage.server.data;

/**
 * Created by LONG on 2024/6/2 at 6:04 PM.
 */
public class Channel {
    private String imessageId;
    private String imessageIdUser;
    private String email;
    private String username;
    private String note;
    private Avatar avatar;
    private Integer sex;
    private Region firstRegion;
    private Region secondRegion;
    private Region thirdRegion;
    private boolean associated;

    public Channel() {
    }

    public Channel(String imessageId, String imessageIdUser, String email, String username, Avatar avatar, Integer sex, Region firstRegion, Region secondRegion, Region thirdRegion, boolean associated, String note) {
        this.imessageId = imessageId;
        this.imessageIdUser = imessageIdUser;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.sex = sex;
        this.firstRegion = firstRegion;
        this.secondRegion = secondRegion;
        this.thirdRegion = thirdRegion;
        this.associated = associated;
        this.note = note;
    }

    public String getImessageId() {
        return imessageId;
    }

    public String getImessageIdUser() {
        return imessageIdUser;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Integer getSex() {
        return sex;
    }

    public Region getFirstRegion() {
        return firstRegion;
    }

    public Region getSecondRegion() {
        return secondRegion;
    }

    public Region getThirdRegion() {
        return thirdRegion;
    }

    public boolean isAssociated() {
        return associated;
    }

    public String getNote() {
        return note;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public void setFirstRegion(Region firstRegion) {
        this.firstRegion = firstRegion;
    }

    public void setSecondRegion(Region secondRegion) {
        this.secondRegion = secondRegion;
    }

    public void setThirdRegion(Region thirdRegion) {
        this.thirdRegion = thirdRegion;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "imessageId='" + imessageId + '\'' +
                ", imessageIdUser='" + imessageIdUser + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", note='" + note + '\'' +
                ", avatar=" + avatar +
                ", sex=" + sex +
                ", firstRegion=" + firstRegion +
                ", secondRegion=" + secondRegion +
                ", thirdRegion=" + thirdRegion +
                ", associated=" + associated +
                '}';
    }
}

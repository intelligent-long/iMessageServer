package com.longx.intelligent.app.imessage.server.data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by LONG on 2024/3/30 at 6:54 PM.
 */
public class OfflineDetail {
    public static int REASON_NEW_LOGIN_IMESSAGE_ID_PASSWORD = 0;
    public static int REASON_NEW_LOGIN_EMAIL_PASSWORD = 1;
    public static int REASON_NEW_LOGIN_VERIFICATION_CODE = 2;
    public static int REASON_RESET_PASSWORD = 3;
    private int reason;
    private Date time;
    private String ip;
    private String desc;

    public OfflineDetail(){}

    public OfflineDetail(int reason, Date time, String ip) {
        this.reason = reason;
        this.time = time;
        this.ip = ip;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy 年 M 月 d 日 HH 时 mm 分 ss 秒");
        String formattedTime = simpleDateFormat.format(time);
        if(reason == REASON_NEW_LOGIN_IMESSAGE_ID_PASSWORD){
            desc = "你的账户于 " + formattedTime + "通过 iMessage ID 和密码在其他地方登陆，登陆设备 IP: " + ip + "，本次会话已失效。";
        }else if(reason == REASON_NEW_LOGIN_EMAIL_PASSWORD){
            desc = "你的账户于 " + formattedTime + "通过邮箱和密码在其他地方登陆，登陆设备 IP: " + ip + "，本次会话已失效。";
        }else if(reason == REASON_NEW_LOGIN_VERIFICATION_CODE){
            desc = "你的账户于 " + formattedTime + "通过邮箱和验证码在其他地方登陆，登陆设备 IP: " + ip + "，本次会话已失效。";
        }else if(reason == REASON_RESET_PASSWORD){
            desc = "你的账户于 " + formattedTime + "重设了密码，操作设备 IP: " + ip + "，本次会话已失效。";
        }
    }

    public int getReason() {
        return reason;
    }

    public Date getTime() {
        return time;
    }

    public String getIp() {
        return ip;
    }

    public String getDesc() {
        return desc;
    }
}

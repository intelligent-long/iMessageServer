package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.util.Logger;
import com.longx.intelligent.app.imessage.server.value.RedisKeys;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    private static final String VERIFY_CODE_EMAIL_SUBJECT = "iMessage";

    public boolean sendEmail(String to, String subject, String content) {
        try {
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            Properties properties = new Properties();
            properties.put("mail.smtp.ssl.trust", "*");
            properties.put("mail.smtp.ssl.socketFactory", sf);
            Session session = Session.getInstance(properties);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(from);
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject(subject);
            message.setText(content);
            javaMailSender.send(message);
            return true;
        }catch (Exception e){
            Logger.err("邮件发送失败 ->");
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendVerificationCodeEmail(String to, String code){
        return sendEmail(to, VERIFY_CODE_EMAIL_SUBJECT,
                code + " 是你的验证码，" + RedisKeys.VerifyCode.EXPIRE_MINUTES_VERIFY_CODE + " 分钟内有效，不要将验证码泄漏给他人。\n\n " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }
}
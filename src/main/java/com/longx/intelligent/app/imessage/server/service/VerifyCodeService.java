package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

/**
 * Created by LONG on 2024/3/30 at 12:47 AM.
 */
@Service
public class VerifyCodeService {
    public enum VerifyResult{CORRECT, INCORRECT, TRY_LATER}
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private EmailService emailService;

    private String generateCode(){
        int code = (int)(Math.random() * 1000000);
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern("000000");
        return decimalFormat.format(code);
    }

    public boolean canSend(String email){
        return redisOperationService.VERIFY_CODE.checkVerifyCodeAvailability(email);
    }


    public boolean send(String email){
        String verifyCode = generateCode();
        redisOperationService.VERIFY_CODE.enterVerifyCodeValidityPeriod(email, verifyCode);
        boolean success = emailService.sendVerificationCodeEmail(email, verifyCode);
        if(success){
            Logger.info("发送了一个新的验证码 > " + "email: " + email + ", code: " + verifyCode);
            return true;
        }else {
            finish(email);
            return false;
        }
    }

    public VerifyResult verify(String email, String verifyCode){
        if(redisOperationService.VERIFY_CODE.checkMaxVerifyFailureTimesReached(email)){
            Logger.info("新的验证码验证，次数过多 > " + "email: " + email + ", code: " + verifyCode);
            return VerifyResult.TRY_LATER;
        }
        if(redisOperationService.VERIFY_CODE.checkVerifyCodeCorrect(email, verifyCode)){
            Logger.info("新的验证码验证，正确 > " + "email: " + email + ", code: " + verifyCode);
            finish(email);
            return VerifyResult.CORRECT;
        }else {
            redisOperationService.VERIFY_CODE.incrementVerifyCodeFailureTimes(email);
            Logger.info("新的验证码验证，错误 > " + "email: " + email + ", code: " + verifyCode);
            return VerifyResult.INCORRECT;
        }
    }

    private void finish(String email){
        redisOperationService.VERIFY_CODE.finishVerification(email);
    }
}

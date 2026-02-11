package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.OfflineDetail;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.*;
import com.longx.intelligent.app.imessage.server.util.NetworkUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
import com.longx.intelligent.app.imessage.server.value.RedisKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Created by LONG on 2024/3/27 at 9:54 PM.
 */
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private VerifyCodeService verifyCodeService;
    @Autowired
    private AuthService authService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private PermissionService permissionService;

    @PostMapping("register")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OperationData register(@Valid @RequestBody RegistrationPostBody postBody){

        if(userService.findUserByEmail(postBody.getEmail()) != null){
            return new OperationData(-101, "邮箱已存在");
        }

        VerifyCodeService.VerifyResult verifyResult = verifyCodeService.verify(postBody.getEmail(), postBody.getVerifyCode());
        switch (verifyResult){
            case TRY_LATER -> {
                return new OperationData(-102, "请稍后再试");
            }
            case INCORRECT -> {
                return new OperationData(-103, "验证码错误");
            }
            case CORRECT -> {
                String newUserImessageId = authService.createNewUser(postBody.getEmail(), postBody.getPassword(), postBody.getUsername());
                if(newUserImessageId != null){
                    User user = userService.findUserByImessageId(newUserImessageId);
                    if(user != null){
                        return OperationData.success(user.getSelf(permissionService));
                    }
                }
            }
        }

        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationData.failure();
    }

    @PostMapping("verify_code/send")
    public OperationStatus sendVerifyCode(@Valid @RequestBody SendVerifyCodePostBody postBody){
        if(!verifyCodeService.canSend(postBody.getEmail())){
            return new OperationStatus(-101, "两次获取验证码请至少间隔 " + Constants.VERIFY_CODE_SEND_INTERVAL_MINUTES + " 分钟");
        }

        if(!verifyCodeService.send(postBody.getEmail())){
            return new OperationStatus(-102, "验证码发送失败");
        }

        OperationStatus success = OperationStatus.success();
        success.putDetail("notice", "包含验证码的邮件已发送到你的邮箱，请查看收件箱，" + RedisKeys.VerifyCode.EXPIRE_MINUTES_VERIFY_CODE + " 分钟内有效。");
        return success;
    }

    @PostMapping("login/imessage_id_user")
    public OperationData imessageIdUserLogin(@Valid @RequestBody ImessageIdUserLoginPostBody postBody, HttpSession session, HttpServletRequest request){
        User user = userService.findUserByImessageIdUser(postBody.getImessageIdUser());
        if(user == null){
            return new OperationData(-101, "请检查 Apollo ID 和密码");
        }

        boolean loginAttemptsLimitExceeded = authService.isLoginAttemptsLimitExceeded(user.getImessageId());
        if(loginAttemptsLimitExceeded){
            return new OperationData(-102, "已达最大登录失败次数，请稍后再试。");
        }

        boolean passed = authService.passLogin(user, postBody.getPassword());
        if(!passed){
            authService.incrementLoginFailureTimes(user.getImessageId());
            return new OperationData(-101, "请检查 Apollo ID 和密码");
        }else {
            authService.login(session, user,
                    new OfflineDetail(OfflineDetail.REASON_NEW_LOGIN_IMESSAGE_ID_PASSWORD, new Date(), NetworkUtil.getIp(request)));
            return OperationData.success(user.getSelf(permissionService));
        }
    }

    @PostMapping("login/email")
    public OperationData emailLogin(@Valid @RequestBody EmailLoginPostBody postBody, HttpSession session, HttpServletRequest request){
        User user = userService.findUserByEmail(postBody.getEmail());
        if(user == null){
            return new OperationData(-101, "请检查邮箱和密码");
        }

        boolean loginAttemptsLimitExceeded = authService.isLoginAttemptsLimitExceeded(user.getImessageId());
        if(loginAttemptsLimitExceeded){
            return new OperationData(-102, "已达最大登录失败次数，请稍后再试。");
        }

        boolean passed = authService.passLogin(user, postBody.getPassword());
        if(!passed){
            authService.incrementLoginFailureTimes(user.getImessageId());
            return new OperationData(-101, "请检查邮箱和密码");
        }else {
            authService.login(session, user,
                    new OfflineDetail(OfflineDetail.REASON_NEW_LOGIN_EMAIL_PASSWORD, new Date(), NetworkUtil.getIp(request)));
            return OperationData.success(user.getSelf(permissionService));
        }
    }

    @PostMapping("login/verify_code")
    public OperationData verifyCodeLogin(@Valid @RequestBody VerifyCodeLoginPostBody postBody, HttpSession session, HttpServletRequest request){
        User user = userService.findUserByEmail(postBody.getEmail());
        if(user == null){
            return new OperationData(-101, "请检查邮箱和验证码");
        }

        boolean loginAttemptsLimitExceeded = authService.isLoginAttemptsLimitExceeded(user.getImessageId());
        if(loginAttemptsLimitExceeded){
            return new OperationData(-102, "已达最大登录失败次数，请稍后再试。");
        }

        VerifyCodeService.VerifyResult verifyResult = verifyCodeService.verify(user.getEmail(), postBody.getVerifyCode());
        switch (verifyResult){
            case TRY_LATER -> {
                return new OperationData(-103, "请稍后再试");
            }
            case INCORRECT -> {
                authService.incrementLoginFailureTimes(user.getImessageId());
                return new OperationData(-101, "请检查邮箱和验证码");
            }
            case CORRECT -> {
                authService.login(session, user,
                        new OfflineDetail(OfflineDetail.REASON_NEW_LOGIN_VERIFICATION_CODE, new Date(), NetworkUtil.getIp(request)));
                return OperationData.success(user.getSelf(permissionService));
            }
        }
        return OperationData.failure();
    }

    @PostMapping("password/reset")
    public OperationStatus resetPassword(@Valid @RequestBody ResetPasswordPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        if(user != null) {
            return OperationStatus.failure();
        }else {
            user = userService.findUserByEmail(postBody.getEmail());
            if(user == null){
                return OperationStatus.failure();
            }
        }

        VerifyCodeService.VerifyResult verifyResult = verifyCodeService.verify(user.getEmail(), postBody.getVerifyCode());
        switch (verifyResult){
            case TRY_LATER -> {
                return new OperationStatus(-101, "请稍后再试");
            }
            case INCORRECT -> {
                return new OperationStatus(-102, "验证码错误");
            }
            case CORRECT -> {
                if(userService.updatePassword(user.getEmail(), postBody.getPassword())){
                    return OperationStatus.success();
                }
            }
        }
        return OperationStatus.failure();
    }

    @PostMapping("password/change")
    public OperationStatus changePassword(@Valid @RequestBody ChangePasswordPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);

        VerifyCodeService.VerifyResult verifyResult = verifyCodeService.verify(user.getEmail(), postBody.getVerifyCode());
        switch (verifyResult){
            case TRY_LATER -> {
                return new OperationStatus(-101, "请稍后再试");
            }
            case INCORRECT -> {
                return new OperationStatus(-102, "验证码错误");
            }
            case CORRECT -> {
                if(userService.updatePassword(user.getEmail(), postBody.getPassword())){
                    sessionService.findAndSetUserToSession(session, user.getImessageId());
                    return OperationStatus.success();
                }
            }
        }

        return OperationStatus.failure();
    }

    @PostMapping("logout")
    public OperationStatus logout(HttpSession session){
        sessionService.removeUserOfSession(session);
        sessionService.invalidateSession(session);
        return OperationStatus.success();
    }

    @GetMapping("offline_detail")
    public OperationData getOfflineDetail(HttpServletRequest request){
        String sessionId = sessionService.getSessionIdFromCookie(request);
        if(sessionId == null) return OperationData.failure();
        OfflineDetail offlineDetail = authService.getOfflineDetail(sessionId);
        if(offlineDetail == null) return new OperationData(-101, "无详情");
        authService.removeOfflineDetail(sessionId);
        return OperationData.success(offlineDetail);
    }

}

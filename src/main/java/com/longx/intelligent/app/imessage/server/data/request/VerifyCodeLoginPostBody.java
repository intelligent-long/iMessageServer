package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidEmail;
import com.longx.intelligent.app.imessage.server.data.validation.ValidVerifyCode;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/3/31 at 5:30 PM.
 */
@Validated
public class VerifyCodeLoginPostBody {
    @ValidEmail
    private final String email;

    @ValidVerifyCode
    private final String verifyCode;

    public VerifyCodeLoginPostBody() {
        this(null, null);
    }

    public VerifyCodeLoginPostBody(String email, String verifyCode) {
        this.email = email;
        this.verifyCode = verifyCode;
    }

    public String getEmail() {
        return email;
    }

    public String getVerifyCode() {
        return verifyCode;
    }
}

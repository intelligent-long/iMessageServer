package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidEmail;
import com.longx.intelligent.app.imessage.server.data.validation.ValidPassword;
import com.longx.intelligent.app.imessage.server.data.validation.ValidVerifyCode;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/3/31 at 6:14 PM.
 */
@Validated
public class ResetPasswordPostBody {
    @ValidEmail
    private final String email;
    @ValidPassword
    private final String password;
    @ValidVerifyCode
    private final String verifyCode;

    public ResetPasswordPostBody() {
        this(null, null, null);
    }

    public ResetPasswordPostBody(String email, String password, String verifyCode) {
        this.email = email;
        this.password = password;
        this.verifyCode = verifyCode;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getVerifyCode() {
        return verifyCode;
    }
}

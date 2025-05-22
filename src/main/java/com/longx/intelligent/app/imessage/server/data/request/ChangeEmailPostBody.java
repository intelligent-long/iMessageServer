package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidEmail;
import com.longx.intelligent.app.imessage.server.data.validation.ValidVerifyCode;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/8 at 12:13 AM.
 */
@Validated
public class ChangeEmailPostBody {
    @ValidEmail
    private final String email;

    @ValidVerifyCode
    private final String verifyCode;

    public ChangeEmailPostBody() {
        this(null, null);
    }

    public ChangeEmailPostBody(String email, String verifyCode) {
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

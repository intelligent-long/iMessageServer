package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidPassword;
import com.longx.intelligent.app.imessage.server.data.validation.ValidVerifyCode;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/2 at 5:04 PM.
 */
@Validated
public class ChangePasswordPostBody {
    @ValidPassword
    private final String password;

    @ValidVerifyCode
    private final String verifyCode;

    public ChangePasswordPostBody() {
        this(null, null);
    }

    public ChangePasswordPostBody(String password, String verifyCode) {
        this.password = password;
        this.verifyCode = verifyCode;
    }

    public String getPassword() {
        return password;
    }

    public String getVerifyCode() {
        return verifyCode;
    }
}

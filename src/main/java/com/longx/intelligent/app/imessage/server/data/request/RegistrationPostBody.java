package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidEmail;
import com.longx.intelligent.app.imessage.server.data.validation.ValidPassword;
import com.longx.intelligent.app.imessage.server.data.validation.ValidUsername;
import com.longx.intelligent.app.imessage.server.data.validation.ValidVerifyCode;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/3/30 at 12:25 AM.
 */
@Validated
public class RegistrationPostBody {
    @ValidEmail
    private final String email;
    @ValidUsername
    private final String username;
    @ValidPassword
    private final String password;
    @ValidVerifyCode
    private final String verifyCode;

    public RegistrationPostBody() {
        this(null, null, null, null);
    }

    public RegistrationPostBody(String email, String username, String password, String verifyCode) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.verifyCode = verifyCode;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getVerifyCode() {
        return verifyCode;
    }
}

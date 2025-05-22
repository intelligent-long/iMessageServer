package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidEmail;
import com.longx.intelligent.app.imessage.server.data.validation.ValidPassword;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/3/31 at 5:06 PM.
 */
@Validated
public class EmailLoginPostBody {
    @ValidEmail
    private final String email;
    @ValidPassword
    private final String password;

    public EmailLoginPostBody() {
        this(null, null);
    }

    public EmailLoginPostBody(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

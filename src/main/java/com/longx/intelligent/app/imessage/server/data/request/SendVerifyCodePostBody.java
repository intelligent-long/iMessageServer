package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidEmail;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/3/30 at 3:38 PM.
 */
@Validated
public class SendVerifyCodePostBody {

    @ValidEmail
    private final String email;

    public SendVerifyCodePostBody() {
        this(null);
    }

    public SendVerifyCodePostBody(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}

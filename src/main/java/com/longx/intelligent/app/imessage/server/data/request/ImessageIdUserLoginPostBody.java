package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageIdUser;
import com.longx.intelligent.app.imessage.server.data.validation.ValidPassword;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/3/30 at 5:26 PM.
 */
@Validated
public class ImessageIdUserLoginPostBody {
    @ValidImessageIdUser
    private final String imessageIdUser;

    @ValidPassword
    private final String password;

    public ImessageIdUserLoginPostBody() {
        this(null, null);
    }

    public ImessageIdUserLoginPostBody(String imessageIdUser, String password) {
        this.imessageIdUser = imessageIdUser;
        this.password = password;
    }

    public String getImessageIdUser() {
        return imessageIdUser;
    }

    public String getPassword() {
        return password;
    }
}

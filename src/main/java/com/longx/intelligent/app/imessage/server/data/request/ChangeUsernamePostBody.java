package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidUsername;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/7 at 11:52 PM.
 */
@Validated
public class ChangeUsernamePostBody {

    @ValidUsername
    private final String username;

    public ChangeUsernamePostBody() {
        this(null);
    }

    public ChangeUsernamePostBody(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidImessageIdUser;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/3 at 9:43 PM.
 */
@Validated
public class ChangeImessageIdUserPostBody {
    @ValidImessageIdUser
    private final String imessageIdUser;

    public ChangeImessageIdUserPostBody() {
        this(null);
    }

    public ChangeImessageIdUserPostBody(String imessageIdUser) {
        this.imessageIdUser = imessageIdUser;
    }

    public String getImessageIdUser() {
        return imessageIdUser;
    }
}

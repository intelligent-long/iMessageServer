package com.longx.intelligent.app.imessage.server.data.request;

import com.longx.intelligent.app.imessage.server.data.validation.ValidSex;
import org.springframework.validation.annotation.Validated;

/**
 * Created by LONG on 2024/4/11 at 1:49 AM.
 */
@Validated
public class ChangeSexPostBody {

    @ValidSex
    private Integer sex;

    public ChangeSexPostBody() {
    }

    public ChangeSexPostBody(Integer sex) {
        this.sex = sex;
    }

    public Integer getSex() {
        return sex;
    }
}

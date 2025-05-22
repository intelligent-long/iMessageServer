package com.longx.intelligent.app.imessage.server.data;

import java.io.Serializable;

/**
 * Created by LONG on 2025/4/28 at 6:42 AM.
 */
public class Region  implements Serializable {
    private final Integer adcode;
    private final String name;

    public Region() {
        this(null, null);
    }

    public Region(Integer adcode, String name) {
        this.adcode = adcode;
        this.name = name;
    }

    public Integer getAdcode() {
        return adcode;
    }

    public String getName() {
        return name;
    }
}

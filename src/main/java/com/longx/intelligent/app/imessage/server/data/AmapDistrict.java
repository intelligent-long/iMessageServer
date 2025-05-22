package com.longx.intelligent.app.imessage.server.data;

import java.util.List;

/**
 * @author 龍天翔
 * @date 2022/12/20 3:24 AM
 */
public class AmapDistrict {
    private Integer adcode;
    private String name;
    private List<AmapDistrict> districts;

    public AmapDistrict(){}

    public AmapDistrict(Integer adcode, String name, List<AmapDistrict> districts) {
        this.adcode = adcode;
        this.name = name;
        this.districts = districts;
    }

    public Integer getAdcode() {
        return adcode;
    }

    public String getName() {
        return name;
    }

    public List<AmapDistrict> getDistricts() {
        return districts;
    }
}

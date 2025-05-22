package com.longx.intelligent.app.imessage.server.data;

import java.util.List;

/**
 * @author 龍天翔
 * @date 2022/12/20 9:48 AM
 */
public class AmapApiDistrictResponse {
    private int status;
    private String info;
    private List<AmapDistrict> districts;

    public AmapApiDistrictResponse(){}

    public AmapApiDistrictResponse(int status, String info, List<AmapDistrict> districts) {
        this.status = status;
        this.info = info;
        this.districts = districts;
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    public List<AmapDistrict> getDistricts() {
        return districts;
    }

    @Override
    public String toString() {
        return "AmapApiDistrictResponse{" +
                "status=" + status +
                ", info='" + info + '\'' +
                ", districts=" + districts +
                '}';
    }
}

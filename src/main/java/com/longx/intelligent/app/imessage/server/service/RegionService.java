package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.AmapDistrict;
import com.longx.intelligent.app.imessage.server.mapper.AmapDistrictMapper;
import com.longx.intelligent.app.imessage.server.mapper.GroupChannelMapper;
import com.longx.intelligent.app.imessage.server.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by LONG on 2025/4/28 at 6:47 AM.
 */
@Service
public class RegionService {
    @Autowired
    private AmapDistrictMapper amapDistrictMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupChannelMapper groupChannelMapper;

    public List<AmapDistrict> getFirstRegions(){
        return amapDistrictMapper.getFirstRegions();
    }

    public List<AmapDistrict> getSecondRegions(int firstRegionAdcode){
        return amapDistrictMapper.getSecondRegions(firstRegionAdcode);
    }

    public List<AmapDistrict> getThirdRegions(int secondRegionAdcode){
        return amapDistrictMapper.getThirdRegions(secondRegionAdcode);
    }

    public boolean isFirstRegionExist(int firstRegionAdcode){
        return amapDistrictMapper.isFirstRegionExist(firstRegionAdcode);
    }

    public boolean isSecondRegionExist(int firstRegionAdcode, int secondRegionAdcode){
        return amapDistrictMapper.isSecondRegionExist(firstRegionAdcode, secondRegionAdcode);
    }

    public boolean isThirdRegionExist(int secondRegionAdcode, int thirdRegionAdcode){
        return amapDistrictMapper.isThirdRegionExist(secondRegionAdcode, thirdRegionAdcode);
    }

    public boolean changeChannelRegion(Integer firstRegionAdcode, Integer secondRegionAdcode, Integer thirdRegionAdcode, String imessageId){
        return userMapper.changeRegion(firstRegionAdcode, secondRegionAdcode, thirdRegionAdcode, imessageId) == 1;
    }

    public boolean changeGroupChannelRegion(Integer firstRegionAdcode, Integer secondRegionAdcode, Integer thirdRegionAdcode, String groupChannelId, String owner){
        return groupChannelMapper.changeRegion(firstRegionAdcode, secondRegionAdcode, thirdRegionAdcode, groupChannelId, owner) == 1;
    }
}

package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.AmapApiDistrictResponse;
import com.longx.intelligent.app.imessage.server.data.AmapDistrict;
import com.longx.intelligent.app.imessage.server.mapper.AmapDistrictMapper;
import com.longx.intelligent.app.imessage.server.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 龍天翔
 * @date 2022/12/20 9:33 AM
 */
@Service
public class SyncAmapDistrictService {

    @Autowired
    private AmapDistrictMapper amapDistrictMapper;

    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void sync(){
        Logger.info("开始更新地区数据库...");

        clearAll();

        String url = "https://restapi.amap.com/v3/config/district?subdistrict=3&key=1e188e76ffb23987b9c0c61ec8e156e5";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AmapApiDistrictResponse> amapApiDistrictResponseResponseEntity = restTemplate.getForEntity(url, AmapApiDistrictResponse.class);
        AmapApiDistrictResponse amapApiDistrictResponse = amapApiDistrictResponseResponseEntity.getBody();

        List<AmapDistrict> firstRegions = amapApiDistrictResponse.getDistricts();
        for (AmapDistrict firstRegion : firstRegions) {
            if(firstRegion.getName().equals("中华人民共和国")){
                firstRegions.remove(firstRegion);
                firstRegions.add(new AmapDistrict(firstRegion.getAdcode(), "中国", firstRegion.getDistricts()));
            }
        }
        insertData(firstRegions);

        List<AmapDistrict> foreign = new ArrayList<>();
        List<AmapDistrict> foreignAmapDistricts = new ArrayList<>();
        foreignAmapDistricts.add(new AmapDistrict(2010000, "亚洲", null));
        foreignAmapDistricts.add(new AmapDistrict(2020000, "欧洲", null));
        foreignAmapDistricts.add(new AmapDistrict(2030000, "北美洲", null));
        foreignAmapDistricts.add(new AmapDistrict(2040000, "南美洲", null));
        foreignAmapDistricts.add(new AmapDistrict(2050000, "非洲", null));
        foreignAmapDistricts.add(new AmapDistrict(2060000, "大洋洲", null));
        foreignAmapDistricts.add(new AmapDistrict(2070000, "南极洲", null));
        foreign.add(new AmapDistrict(2000000, "国外", foreignAmapDistricts));
        insertData(foreign);
    }

    private void insertData(List<AmapDistrict> firstRegions){
        amapDistrictMapper.insertFirstRegions(firstRegions);
        for (AmapDistrict firstRegion : firstRegions) {
            List<AmapDistrict> secondRegions = firstRegion.getDistricts();
            if(secondRegions == null || secondRegions.isEmpty()){
                continue;
            }
            amapDistrictMapper.insertSecondRegions(secondRegions, firstRegion.getAdcode());
            for (AmapDistrict secondRegion : secondRegions) {
                List<AmapDistrict> thirdRegions = secondRegion.getDistricts();
                if(thirdRegions == null || thirdRegions.isEmpty()){
                    continue;
                }
                amapDistrictMapper.insertThirdRegions(thirdRegions, secondRegion.getAdcode());
            }
        }
    }

    private void clearAll(){
        amapDistrictMapper.clearFirstRegions();
        amapDistrictMapper.clearSecondRegions();
        amapDistrictMapper.clearThirdRegions();
    }

    public boolean isDataEmpty(){
        return amapDistrictMapper.countFirstRegion() == 0;
    }
}

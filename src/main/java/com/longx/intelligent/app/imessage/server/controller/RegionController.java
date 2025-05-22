package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.AmapDistrict;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.service.RegionService;
import com.longx.intelligent.app.imessage.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by LONG on 2025/4/28 at 10:31 AM.
 */
@RestController
@RequestMapping("region")
public class RegionController {
    @Autowired
    private RegionService regionService;

    @GetMapping("first_region/all")
    public OperationData getAllFirstRegion(){
        List<AmapDistrict> firstRegions = regionService.getFirstRegions();
        return new OperationData(0, "成功", firstRegions);
    }

    @GetMapping("second_region/all/{firstRegionAdcode}")
    public OperationData getAllSecondRegion(@PathVariable int firstRegionAdcode){
        List<AmapDistrict> secondRegions = regionService.getSecondRegions(firstRegionAdcode);
        return new OperationData(0, "成功", secondRegions);
    }

    @GetMapping("third_region/all/{secondRegionAdcode}")
    public OperationData getAllThirdRegion(@PathVariable int secondRegionAdcode){
        List<AmapDistrict> thirdRegions = regionService.getThirdRegions(secondRegionAdcode);
        return new OperationData(0, "成功", thirdRegions);
    }
}

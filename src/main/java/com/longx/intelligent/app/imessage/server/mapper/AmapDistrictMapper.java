package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.AmapDistrict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmapDistrictMapper {

    int clearFirstRegions();
    int clearSecondRegions();
    int clearThirdRegions();

    int insertFirstRegions(@Param("firstRegions") List<AmapDistrict> firstRegions);

    int insertSecondRegions(@Param("secondRegions") List<AmapDistrict> secondRegions, int firstRegionAdcode);

    int insertThirdRegions(@Param("thirdRegions") List<AmapDistrict> thirdRegions, int secondRegionAdcode);

    List<AmapDistrict> getFirstRegions();

    List<AmapDistrict> getSecondRegions(int firstRegionAdcode);

    List<AmapDistrict> getThirdRegions(int secondRegionAdcode);

    boolean isFirstRegionExist(int firstRegionAdcode);

    boolean isSecondRegionExist(int firstRegionAdcode, int secondRegionAdcode);

    boolean isThirdRegionExist(int secondRegionAdcode, int thirdRegionAdcode);

    String findFirstRegionName(int firstRegionAdcode);

    String findSecondRegionName(int secondRegionAdcode);

    String findThirdRegionName(int thirdRegionAdcode);

    int countFirstRegion();

}

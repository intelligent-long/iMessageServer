package com.longx.intelligent.app.imessage.server.initrunner;

import com.longx.intelligent.app.imessage.server.service.SyncAmapDistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Created by LONG on 2023/8/11 at 7:57 AM.
 */
@Component
public class CheckAndSyncDistrictsRunner implements ApplicationRunner {
    @Autowired
    private SyncAmapDistrictService syncDistrictsService;

    @Override
    public void run(ApplicationArguments args) {
        boolean dataEmpty = syncDistrictsService.isDataEmpty();
        if(dataEmpty){
            syncDistrictsService.sync();
        }
    }
}

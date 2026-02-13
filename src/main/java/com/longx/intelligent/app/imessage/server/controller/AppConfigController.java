package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.config.ImessageConfig;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by LONG on 2026/2/13 at 22:02.
 */
@RestController
@RequestMapping("server_config")
public class AppConfigController {
    @Autowired
    private ImessageConfig imessageConfig;

    @GetMapping("current_client_version")
    public OperationData getCurrentClientVersionCode(){
        int currentClientVersionCode = imessageConfig.getCurrentClientVersion();
        return OperationData.success(currentClientVersionCode);
    }

}

package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.config.ImessageConfig;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.service.UrlService;
import com.longx.intelligent.app.imessage.server.value.UrlConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by LONG on 2024/10/28 at 10:49 PM.
 */
@RestController
@RequestMapping("url")
public class UrlMapController {
    @Autowired
    private UrlService urlService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ImessageConfig imessageConfig;

    @GetMapping("imessage_web/home")
    public OperationData imessageWebHome(){
        String imessageWebBaseUrl = urlService.fetchImessageWebBaseUrl();
        if(imessageWebBaseUrl == null) return OperationData.failure();
        return restTemplate.getForEntity(imessageWebBaseUrl + UrlConstants.IMESSAGE_WEB_API_RES_HOME, OperationData.class).getBody();
    }

    @GetMapping("imessage_web/release_data/{versionCode}")
    public OperationData releaseData(@PathVariable("versionCode") int versionCode){
        String imessageWebBaseUrl = urlService.fetchImessageWebBaseUrl();
        if(imessageWebBaseUrl == null) return OperationData.failure();
        return restTemplate.getForEntity(imessageWebBaseUrl + UrlConstants.IMESSAGE_WEB_API_RES_RELEASE_DATA, OperationData.class, versionCode).getBody();
    }

    @GetMapping("imessage_web/release_data/updatable")
    public OperationData updatableReleaseData(){
        String imessageWebBaseUrl = urlService.fetchImessageWebBaseUrl();
        if(imessageWebBaseUrl == null) return OperationData.failure();
        int currentClientVersion = imessageConfig.getCurrentClientVersion();
        return restTemplate.getForEntity(imessageWebBaseUrl + UrlConstants.IMESSAGE_WEB_API_RES_RELEASE_DATA, OperationData.class, currentClientVersion).getBody();
    }

    @GetMapping("imessage_web/release/{versionCode}")
    public OperationData release(@PathVariable("versionCode") int versionCode){
        String imessageWebBaseUrl = urlService.fetchImessageWebBaseUrl();
        if(imessageWebBaseUrl == null) return OperationData.failure();
        return restTemplate.getForEntity(imessageWebBaseUrl + UrlConstants.IMESSAGE_WEB_API_RES_RELEASE, OperationData.class, versionCode).getBody();
    }

    @GetMapping("imessage_web/download/file/all/{versionCode}")
    public OperationData allDownloadFiles(@PathVariable("versionCode") int versionCode){
        String imessageWebBaseUrl = urlService.fetchImessageWebBaseUrl();
        if(imessageWebBaseUrl == null) return OperationData.failure();
        return restTemplate.getForEntity(imessageWebBaseUrl + UrlConstants.IMESSAGE_WEB_API_RES_ALL_DOWNLOAD_FILE, OperationData.class, versionCode).getBody();
    }

    @GetMapping("imessage_web/download/file/{fileId}")
    public OperationData downloadFile(@PathVariable("fileId") String fileId){
        String imessageWebBaseUrl = urlService.fetchImessageWebBaseUrl();
        if(imessageWebBaseUrl == null) return OperationData.failure();
        return restTemplate.getForEntity(imessageWebBaseUrl + UrlConstants.IMESSAGE_WEB_API_RES_DOWNLOAD_FILE, OperationData.class, fileId).getBody();
    }
}

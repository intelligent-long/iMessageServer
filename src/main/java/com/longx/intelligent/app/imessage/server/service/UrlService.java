package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.ServerLocation;
import com.longx.intelligent.app.imessage.server.value.UrlConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by LONG on 2024/10/29 at 12:56 AM.
 */
@Service
public class UrlService {
    @Autowired
    private RestTemplate restTemplate;

    public String fetchImessageWebBaseUrl(){
        ResponseEntity<ServerLocation> entity = restTemplate.getForEntity(UrlConstants.SERVER_FINDER_FIND_IMESSAGE_WEB_LOCATION_URL, ServerLocation.class);
        ServerLocation serverLocation = entity.getBody();
        if(serverLocation != null) {
            if (serverLocation.getHost() != null) {
                if (serverLocation.getPort() != null) {
                    return "http://" + serverLocation.getHost() + ":" + serverLocation.getPort();
                } else {
                    return "http://" + serverLocation.getHost();
                }
            }
            if (serverLocation.getBaseUrl() != null) return serverLocation.getBaseUrl();
        }
        return null;
    }


}

package com.longx.intelligent.app.imessage.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by LONG on 2025/4/12 at 2:22 PM.
 */
@RestController
@RequestMapping("group_chat")
public class GroupChatController {

    @PostMapping("message/text/send")
    public void sendTextMessage(){

    }
}

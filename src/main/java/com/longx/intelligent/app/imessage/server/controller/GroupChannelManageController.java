package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.GroupChannel;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.ChangeGroupChannelJoinVerificationPostBody;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.GroupChannelService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Created by LONG on 2025/5/7 at 5:19 AM.
 */
@RestController
@RequestMapping("group_channel_management")
public class GroupChannelManageController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private GroupChannelService groupChannelService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("group_join_verification/change")
    public OperationStatus changeGroupJoinVerification(@Valid @RequestBody ChangeGroupChannelJoinVerificationPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(postBody.getGroupId(), currentUser.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(currentUser.getImessageId())){
            return new OperationStatus(-102, "非法身份");
        }
        if(Objects.equals(groupChannel.getGroupJoinVerification(), postBody.getGroupJoinVerification())){
            return new OperationStatus(-103, "请修改入群验证");
        }
        if(groupChannelService.updateGroupJoinVerification(postBody.getGroupId(), postBody.getGroupJoinVerification(), currentUser.getImessageId())){
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_UPDATE, groupChannel.getGroupChannelId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }
}

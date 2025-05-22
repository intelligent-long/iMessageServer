package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.BroadcastChannelPermission;
import com.longx.intelligent.app.imessage.server.data.BroadcastPermission;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.BroadcastService;
import com.longx.intelligent.app.imessage.server.service.PermissionService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by LONG on 2024/6/7 at 2:47 PM.
 */
@RestController
@RequestMapping("permission")
public class PermissionController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private BroadcastService broadcastService;

    @PostMapping("user/profile_visibility/change")
    public OperationStatus changeUserProfileVisibility(@RequestBody ChangeUserProfileVisibilityPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        boolean success = permissionService.insertOrUpdateUserProfileVisibility(user.getImessageId(), postBody.isEmailVisible(), postBody.isSexVisible(), postBody.isRegionVisible());
        if(!success){
            return OperationStatus.failure();
        }else {
            simpMessagingTemplate.convertAndSendToUser(user.getImessageId(), StompDestinations.USER_PROFILE_UPDATE, "");
            return OperationStatus.success();
        }
    }

    @PostMapping("user/ways_to_find_me/change")
    public OperationStatus changeWaysToFindMe(@RequestBody ChangeWaysToFindMePostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        boolean success = permissionService.insertOrUpdateWaysToFindMe(user.getImessageId(), postBody.isByImessageId(), postBody.isByEmail());
        if(!success){
            return OperationStatus.failure();
        }else {
            simpMessagingTemplate.convertAndSendToUser(user.getImessageId(), StompDestinations.USER_PROFILE_UPDATE, "");
            return OperationStatus.success();
        }
    }

    @PostMapping("channel/chat_message/allow_chat_message/change")
    public OperationStatus changeAllowChatMessage(@RequestBody ChangeAllowChatMessagePostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        boolean success = permissionService.insertOrUpdateAllowChatMessage(user.getImessageId(), postBody.getChannelId(), postBody.getChatMessageAllow());
        if(!success){
            return OperationStatus.failure();
        }else {
            simpMessagingTemplate.convertAndSendToUser(user.getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
            return OperationStatus.success();
        }
    }

    @GetMapping("broadcast/channel_permission")
    @Transactional
    public OperationData getBroadcastChannelPermission(HttpSession session){
        User user = sessionService.getUserOfSession(session);

        BroadcastChannelPermission broadcastChannelPermission = permissionService.findBroadcastChannelPermission(user.getImessageId());
        if(broadcastChannelPermission != null) {
            return OperationData.success(broadcastChannelPermission);
        }else {
            boolean success = permissionService.insertOrUpdateBroadcastChannelPermission(user.getImessageId(), BroadcastChannelPermission.PUBLIC);
            boolean success1 = permissionService.updateAllBroadcastChannelPermissionExcludeConnectedChannels(user.getImessageId(), new HashSet<>());
            if(success && success1){
                BroadcastChannelPermission broadcastChannelPermissionAfter = permissionService.findBroadcastChannelPermission(user.getImessageId());
                if(broadcastChannelPermissionAfter != null) return OperationData.success(broadcastChannelPermissionAfter);
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationData.failure();
    }

    @PostMapping("broadcast/channel_permission/change")
    @Transactional
    public OperationStatus changeBroadcastChannelPermission(@RequestBody ChangeBroadcastChannelPermissionPostBody postBody, HttpSession session){
        if(postBody.getPermission() != BroadcastChannelPermission.PUBLIC &&
                postBody.getPermission() != BroadcastChannelPermission.PRIVATE &&
                postBody.getPermission() != BroadcastChannelPermission.CONNECTED_CHANNEL_CIRCLE){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationStatus(-101, "权限不合法");
        }

        User user = sessionService.getUserOfSession(session);
        boolean success = permissionService.insertOrUpdateBroadcastChannelPermission(user.getImessageId(), postBody.getPermission());
        if(!success){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        if(postBody.getExcludeConnectedChannels() == null){
            permissionService.deleteAllBroadcastChannelPermissionExcludeConnectedChannels(user.getImessageId());
        }else {
            boolean success1 = permissionService.updateAllBroadcastChannelPermissionExcludeConnectedChannels(user.getImessageId(), postBody.getExcludeConnectedChannels());
            if (!success1) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }

        return OperationStatus.success();
    }

    @GetMapping("broadcast/exclude_channel")
    public OperationData getExcludeBroadcastChannels(HttpSession session){
        User user = sessionService.getUserOfSession(session);
        Set<String> excludeBroadcastChannels = permissionService.findExcludeBroadcastChannels(user.getImessageId());
        return OperationData.success(excludeBroadcastChannels);
    }

    @PostMapping("broadcast/exclude_channel/change")
    @Transactional
    public OperationStatus changeExcludeBroadcastChannels(@RequestBody ChangeExcludeBroadcastChannelPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        boolean success = permissionService.updateAllExcludeBroadcastChannels(user.getImessageId(), postBody.getExcludeBroadcastChannelIds());
        if(!success) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        return OperationStatus.success();
    }

    @PostMapping("broadcast/permission/change")
    @Transactional
    public OperationData changeBroadcastPermission(@RequestBody ChangeBroadcastPermissionPostBody postBody, HttpSession session){
        if(postBody.getBroadcastPermission().getPermission() != BroadcastPermission.PUBLIC &&
                postBody.getBroadcastPermission().getPermission() != BroadcastPermission.PRIVATE &&
                postBody.getBroadcastPermission().getPermission() != BroadcastPermission.CONNECTED_CHANNEL_CIRCLE){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-101, "权限不合法");
        }

        User user = sessionService.getUserOfSession(session);
        boolean success = permissionService.insertOrUpdateBroadcastPermission(postBody.getBroadcastPermission().getBroadcastId(), user.getImessageId(), postBody.getBroadcastPermission().getPermission());
        if(!success){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationData.failure();
        }
        if(postBody.getBroadcastPermission().getExcludeConnectedChannels() == null){
            permissionService.deleteAllBroadcastPermissionExcludeConnectedChannels(postBody.getBroadcastPermission().getBroadcastId(), user.getImessageId());
        }else {
            boolean success1 = permissionService.updateAllBroadcastPermissionExcludeConnectedChannels(postBody.getBroadcastPermission().getBroadcastId(), user.getImessageId(), postBody.getBroadcastPermission().getExcludeConnectedChannels());
            if (!success1) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationData.failure();
            }
        }

        return OperationData.success(broadcastService.findBroadcast(postBody.getBroadcastPermission().getBroadcastId(), user.getImessageId()));
    }
}

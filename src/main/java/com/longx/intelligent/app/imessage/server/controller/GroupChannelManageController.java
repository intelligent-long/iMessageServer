package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.data.request.ChangeGroupChannelJoinVerificationPostBody;
import com.longx.intelligent.app.imessage.server.data.request.ManageGroupChannelDisconnectPostBody;
import com.longx.intelligent.app.imessage.server.data.request.TransferGroupChannelManagerPostBody;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.ChannelService;
import com.longx.intelligent.app.imessage.server.service.GroupChannelService;
import com.longx.intelligent.app.imessage.server.service.RedisOperationService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by LONG on 2025/5/7 at 5:19 AM.
 */
@RestController
@RequestMapping("group_channel_manage")
public class GroupChannelManageController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private GroupChannelService groupChannelService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private ChannelService channelService;

    @PostMapping("group_join_verification/enabled/change")
    public OperationStatus changeGroupJoinVerification(@Valid @RequestBody ChangeGroupChannelJoinVerificationPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(postBody.getGroupId(), currentUser.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道。");
        }
        if(!groupChannel.getOwner().equals(currentUser.getImessageId())){
            return new OperationStatus(-102, "非法身份。");
        }
        if(Objects.equals(groupChannel.getGroupJoinVerificationEnabled(), postBody.getGroupJoinVerificationEnabled())){
            return new OperationStatus(-103, "请修改入群验证。");
        }
        if(groupChannelService.updateGroupJoinVerificationEnabled(postBody.getGroupId(), postBody.getGroupJoinVerificationEnabled(), currentUser.getImessageId())){
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_UPDATE, groupChannel.getGroupChannelId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @PostMapping("disconnect/{groupChannelId}")
    @Transactional
    public OperationStatus manageGroupChannelDisconnectChannel(@PathVariable("groupChannelId") String groupChannelId, @RequestBody ManageGroupChannelDisconnectPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道。");
        }
        String owner = groupChannel.getOwner();
        if(!owner.equals(currentUser.getImessageId())){
            return new OperationStatus(-102, "请联系群管理员进行频道管理。");
        }
        if(postBody.getChannelIds().isEmpty()){
            return new OperationStatus(-103, "参数异常。");
        }
        for (String channelId : postBody.getChannelIds()) {
            if(!groupChannelService.isGroupChannelAssociated(groupChannelId, channelId)){
                return new OperationStatus(-103, "参数异常。");
            }
        }
        if(postBody.getChannelIds().contains(currentUser.getImessageId()) || postBody.getChannelIds().contains(owner)){
            return new OperationStatus(-103, "参数异常。");
        }
        List<String> associatedImessageIds = new ArrayList<>();
        associatedImessageIds.add(currentUser.getImessageId());
        groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId()).getGroupChannelAssociations().forEach(groupChannelAssociation -> {
            associatedImessageIds.add(groupChannelAssociation.getRequester().getImessageId());
        });
        for (String channelId : postBody.getChannelIds()) {
            if(!groupChannelService.setGroupChannelAssociationToInactive(groupChannelId, channelId)){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }else {
                GroupChannelNotification groupChannelNotification = new GroupChannelNotification(UUID.randomUUID().toString(), GroupChannelNotification.Type.PASSIVE_DISCONNECT,
                        groupChannelId, channelId, true, currentUser.getImessageId(), new Date(), false);
                associatedImessageIds.forEach(associatedImessageId -> {
                    redisOperationService.GROUP_CHANNEL_NOTIFICATION.saveNotification(associatedImessageId, groupChannelNotification);
                });
            }
        }
        associatedImessageIds.forEach(associatedImessageId -> {
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNELS_UPDATE, "");
        });
        return OperationStatus.success();
    }

    @PostMapping("transfer_manager")
    public OperationStatus transferGroupChannelManager(@Valid @RequestBody TransferGroupChannelManagerPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel toTransferGroupChannel = groupChannelService.findGroupChannelById(postBody.getToTransferGroupChannelId(), currentUser.getImessageId());
        if(toTransferGroupChannel == null){
            return new OperationStatus(-101, "没有群频道。");
        }
        String owner = toTransferGroupChannel.getOwner();
        if(!owner.equals(currentUser.getImessageId())){
            return new OperationStatus(-102, "请联系群管理员进行频道管理。");
        }else if(owner.equals(postBody.getTransferToChannelId())){
            return new OperationStatus(-103, "非法身份。");
        }
        if(!groupChannelService.isGroupChannelAssociated(postBody.getToTransferGroupChannelId(), postBody.getTransferToChannelId())){
            return new OperationStatus(-104, "参数异常。");
        }
        if (groupChannelService.isTransferManagerInInviting(postBody.getTransferToChannelId(), toTransferGroupChannel.getGroupChannelId())) {
            return new OperationStatus(-105, "群频道管理员已经在移交中。");
        }
        List<String> receiveNotificationsImessageIds = new ArrayList<>();
        receiveNotificationsImessageIds.add(currentUser.getImessageId());
        receiveNotificationsImessageIds.add(postBody.getTransferToChannelId());
        GroupChannelNotification groupChannelNotification = new GroupChannelNotification(UUID.randomUUID().toString(), GroupChannelNotification.Type.INVITE_TRANSFER_MANAGER,
                postBody.getToTransferGroupChannelId(), postBody.getTransferToChannelId(), null, currentUser.getImessageId(), new Date(), false);
        receiveNotificationsImessageIds.forEach(receiveNotificationsImessageId -> {
            redisOperationService.GROUP_CHANNEL_NOTIFICATION.saveNotification(receiveNotificationsImessageId, groupChannelNotification);
        });
        receiveNotificationsImessageIds.forEach(receiveNotificationsImessageId -> {
            simpMessagingTemplate.convertAndSendToUser(receiveNotificationsImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(receiveNotificationsImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(receiveNotificationsImessageId, StompDestinations.GROUP_CHANNELS_UPDATE, "");
        });
        return OperationStatus.success();
    }

}

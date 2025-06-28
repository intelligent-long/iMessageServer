package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.ChannelService;
import com.longx.intelligent.app.imessage.server.service.PermissionService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.service.UserService;
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
 * Created by LONG on 2024/4/28 at 12:14 AM.
 */
@RestController
@RequestMapping("channel")
public class ChannelController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private PermissionService permissionService;

    @GetMapping("find/imessage_id/{imessageId}")
    public OperationData findChannelByImessageId(@PathVariable String imessageId, HttpSession session){
        Channel channel = channelService.findChannelByImessageId(imessageId, session);
        if(channel == null) return new OperationData(-101, "没有找到频道");
        return OperationData.success(channel);
    }

    @GetMapping("find/imessage_id_user/{imessageIdUser}")
    public OperationData findChannelByImessageIdUser(@PathVariable String imessageIdUser, HttpSession session){
        Channel channel = channelService.findChannelByImessageIdUser(imessageIdUser, session);
        if(channel == null) return new OperationData(-101, "没有找到频道");
        return OperationData.success(channel);
    }

    @GetMapping("find/email/{email}")
    public OperationData findChannelByEmail(@PathVariable String email, HttpSession session) {
        Channel channel = channelService.findChannelByEmail(email, session);
        if(channel == null) return new OperationData(-101, "没有找到频道");
        return OperationData.success(channel);
    }

    @PostMapping("add/request")
    public OperationStatus requestAdd(@Valid @RequestBody RequestAddChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        User toUser = userService.findUserByImessageIdUser(postBody.getImessageIdUser());
        if(toUser == null) return new OperationStatus(-101, "目标用户不存在");
        if(toUser.getImessageId().equals(currentUser.getImessageId())) return new OperationStatus(-102, "不能添加自己");
        if(channelService.isChannelAssociated(currentUser.getImessageId(), toUser.getImessageId())){
            return new OperationStatus(-103, "频道已经建立关联");
        }
        if(channelService.isInAdding(currentUser.getImessageId(), toUser.getImessageId())){
            return new OperationStatus(-104, "频道已经在添加中");
        }
        Date requestTime = new Date();
        Channel requesterChannelInfo = channelService.findChannelByImessageId(currentUser.getImessageId(), currentUser.getImessageId());
        Channel responderChannelInfo = channelService.findChannelByImessageId(toUser.getImessageId(), currentUser.getImessageId());
        String uuid = UUID.randomUUID().toString();
        channelService.saveRequester(new ChannelAddition(uuid, requesterChannelInfo, responderChannelInfo,
                postBody.getMessage(), postBody.getNote(), postBody.getNewTagNames(), postBody.getToAddTagIds(), requestTime, null,
                false, false, false));
        channelService.saveResponder(new ChannelAddition(uuid, requesterChannelInfo, responderChannelInfo,
                postBody.getMessage(), postBody.getNote(), postBody.getNewTagNames(), postBody.getToAddTagIds(), requestTime, null,
                false, false, false));
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_UPDATE, "");
        simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_UPDATE, "");
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
        simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("add/accept")
    @Transactional
    public OperationStatus acceptAdd(@Valid @RequestBody AcceptAddChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        ChannelAddition requesterChannelAddition = channelService.getRequesterChannelAdditionByUuid(postBody.getUuid(), session);
        User toUser = userService.findUserByImessageIdUser(requesterChannelAddition.requesterChannel().getImessageId());
        if(toUser == null) return new OperationStatus(-101, "目标用户不存在");
        if(!channelService.isInAdding(toUser.getImessageId(), currentUser.getImessageId())){
            return new OperationStatus(-102, "频道未在添加中");
        }
        if(channelService.isChannelAssociated(currentUser.getImessageId(), toUser.getImessageId())){
            return new OperationStatus(-103, "频道已经建立关联");
        }
        Date respondTime = new Date();
        ChannelAddition channelAddition = new ChannelAddition(requesterChannelAddition.uuid(), requesterChannelAddition.requesterChannel(),
                requesterChannelAddition.responderChannel(), requesterChannelAddition.message(), requesterChannelAddition.note(),
                requesterChannelAddition.newTagNames(), requesterChannelAddition.toAddTagIds(),
                requesterChannelAddition.requestTime(), respondTime, true, false, requesterChannelAddition.isExpired());
        channelService.acceptChangeRequester(channelAddition);
        channelService.acceptChangeResponder(channelAddition);
        boolean success = channelService.insertChannelAssociation(new ChannelAssociation(UUID.randomUUID().toString(), currentUser.getImessageId(), toUser.getImessageId(),
                false, requesterChannelAddition.requestTime(), respondTime, true, null, null));
        boolean success1 = channelService.insertChannelAssociation(new ChannelAssociation(UUID.randomUUID().toString(), toUser.getImessageId(), currentUser.getImessageId(),
                true, requesterChannelAddition.requestTime(), respondTime, true, null, null));
        boolean success2 = true;
        if(requesterChannelAddition.note() != null) {
            success2 = channelService.newChannelNote(toUser.getImessageId(), currentUser.getImessageId(), requesterChannelAddition.note());
        }
        boolean success3 = true;
        if(requesterChannelAddition.newTagNames() != null){
            for (String newTagName : requesterChannelAddition.newTagNames()) {
                String tagId = channelService.insertChannelTag(toUser.getImessageId(), newTagName);
                if(tagId == null){
                    success3 = false;
                }
                if(!channelService.insertTagChannel(tagId, toUser.getImessageId(), currentUser.getImessageId())){
                    success3 = false;
                }
            }
        }
        boolean success4 = true;
        if(requesterChannelAddition.toAddTagIds() != null){
            for (String toAddTagId : requesterChannelAddition.toAddTagIds()) {
                if(!channelService.insertTagChannel(toAddTagId, toUser.getImessageId(), currentUser.getImessageId())){
                    success4 = false;
                }
            }
        }
        if(success && success1 && success2 && success3 && success4) {
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
            return OperationStatus.success();
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationStatus.failure();
    }

    @GetMapping("add/activities/not_viewed_count")
    public OperationData getAdditionActivitiesNotViewedCount(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        ChannelAdditionNotViewedCount notViewedCount = channelService.getChannelAdditionNotViewedCount(currentUser.getImessageId());
        return OperationData.success(notViewedCount);
    }

    @GetMapping("add/activity/all")
    public OperationData getAllAdditionActivities(HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        List<ChannelAddition> allChannelAddition = channelService.getAllChannelAddition(currentUser.getImessageId(), session);
        return OperationData.success(allChannelAddition);
    }

    @PostMapping("add/activity/{uuid}/view")
    public OperationStatus viewOneAdditionActivity(@PathVariable String uuid, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        boolean success = channelService.viewOneChannelAddition(currentUser.getImessageId(), uuid);
        if(success){
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            return OperationStatus.success();
        }else {
            return OperationStatus.failure();
        }
    }

    @GetMapping("association/all")
    public OperationData getAllAssociatedChannel(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<ChannelAssociation> allChannelAssociations = channelService.findAllChannelAssociations(currentUser.getImessageId());
        return OperationData.success(allChannelAssociations);
    }

    @PostMapping("association/delete")
    @Transactional
    public OperationStatus deleteAssociatedChannel(@RequestBody DeleteChannelAssociationPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        String channelImessageId = postBody.getChannelImessageId();
        String channelNote = channelService.findChannelNote(currentUser.getImessageId(), channelImessageId);
        if(channelNote != null && !channelService.updateChannelNoteToInactive(currentUser.getImessageId(), channelImessageId)){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        if(!channelService.deleteTagChannelOfAll(currentUser.getImessageId(), channelImessageId)){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        if(!permissionService.deleteChatMessageAllow(currentUser.getImessageId(), channelImessageId)){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        if(!channelService.deleteChannelAssociation(currentUser.getImessageId(), channelImessageId)){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        permissionService.deleteBroadcastChannelPermissionExcludeConnectedChannel(currentUser.getImessageId(), channelImessageId);
        permissionService.deleteExcludeBroadcastChannel(currentUser.getImessageId(), channelImessageId);
        permissionService.deleteAllBroadcastPermissionExcludeConnectedChannels(currentUser.getImessageId(), channelImessageId);

        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
        simpMessagingTemplate.convertAndSendToUser(channelImessageId, StompDestinations.CHANNELS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/note/set")
    public OperationStatus setNoteToAssociatedChannel(@RequestBody @Valid SetNoteToAssociatedChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        String channelImessageId = postBody.getChannelImessageId();
        if(!channelService.isChannelAssociated(currentUser.getImessageId(), channelImessageId)){
            return new OperationStatus(-101, "未建立关系");
        }
        Channel channel = channelService.findChannelByImessageId(channelImessageId, currentUser.getImessageId());
        if(Objects.equals(channel.getNote(), postBody.getNote())){
            return new OperationStatus(-102, "请修改备注");
        }
        if(!channelService.newChannelNote(currentUser.getImessageId(), channelImessageId, postBody.getNote())){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/note/delete/{channelImessageId}")
    public OperationStatus deleteNoteOfAssociatedChannel(@PathVariable String channelImessageId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        if(!channelService.isChannelAssociated(currentUser.getImessageId(), channelImessageId)){
            return new OperationStatus(-101, "未建立关系");
        }
        if(!channelService.updateChannelNoteToInactive(currentUser.getImessageId(), channelImessageId)){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNELS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/add")
    public OperationStatus addTag(@Valid @RequestBody AddChannelTagPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        if(channelService.insertChannelTag(currentUser.getImessageId(), postBody.getName()) == null){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @GetMapping("association/tag/all")
    public OperationData getTags(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<ChannelTag> allChannelTags = channelService.findAllChannelTags(currentUser.getImessageId());
        return OperationData.success(allChannelTags);
    }

    @PostMapping("association/tag/name/change")
    public OperationStatus changeTagName(@Valid @RequestBody ChangeChannelTagNamePostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        ChannelTag channelTag = channelService.findChannelTag(currentUser.getImessageId(), postBody.getTagId());
        if(channelTag == null){
            return OperationStatus.failure();
        }
        if(channelTag.getName().equals(postBody.getName())){
            return new OperationStatus(-101, "请更改标签名称");
        }
        if(!channelService.updateChannelTagName(postBody.getName(), postBody.getTagId(), currentUser.getImessageId())){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/sort")
    @Transactional
    public OperationStatus sortTags(@Valid @RequestBody SortTagsPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        ArrayList<Integer> orders = new ArrayList<>(postBody.getOrderMap().values());
        orders.sort(Comparator.comparingInt(o -> o));
        for (int i = 0; i < orders.size(); i++) {
            if(i == 0){
                if(orders.get(i) != 0){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new OperationStatus(-101, "排序必须是从0开始的连续数字");
                }
            }else {
                if (orders.get(i) != orders.get(i - 1) + 1) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new OperationStatus(-101, "排序必须是从0开始的连续数字");
                }
            }
        }
        List<ChannelTag> allTag = channelService.findAllChannelTags(currentUser.getImessageId());
        if(allTag.size() != postBody.getOrderMap().size()){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationStatus(-101, "必须包括所有标签，且只能是自己创建的标签");
        }
        for (ChannelTag tag : allTag) {
            boolean have = false;
            for (Map.Entry<String, Integer> tagIdOrderEntry : postBody.getOrderMap().entrySet()) {
                if(tag.getTagId().equals(tagIdOrderEntry.getKey())){
                    have = true;
                    break;
                }
            }
            if(!have){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new OperationStatus(-101, "必须包括所有标签，且只能是自己创建的标签");
            }
        }
        boolean allOrderIsSame = true;
        for (ChannelTag tag : allTag) {
            for (Map.Entry<String, Integer> tagIdOrderEntry : postBody.getOrderMap().entrySet()) {
                if(tag.getTagId().equals(tagIdOrderEntry.getKey()) && tag.getOrder() != tagIdOrderEntry.getValue()){
                    allOrderIsSame = false;
                    break;
                }
            }
        }
        if(allOrderIsSame){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationStatus(-102, "请修改标签排序");
        }
        try {
            for (Map.Entry<String, Integer> stringIntegerEntry : postBody.getOrderMap().entrySet()) {
                boolean success = channelService.updateChannelTagOrder(stringIntegerEntry.getKey(), currentUser.getImessageId(), stringIntegerEntry.getValue());
                if(!success){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new OperationStatus(-103, "更新顺序失败");
                }
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationStatus(-103, "更新顺序失败");
        }
        List<ChannelTag> allTags = channelService.findAllChannelTags(currentUser.getImessageId());
        allTags.sort(Comparator.comparingInt(ChannelTag::getOrder));
        for (int i = 0; i < allTags.size(); i++) {
            if(i == 0){
                if(allTags.get(i).getOrder() != 0){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return OperationStatus.failure();
                }
            }else {
                if (allTags.get(i).getOrder() != allTags.get(i - 1).getOrder() + 1){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return OperationStatus.failure();
                }
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/channel/add")
    @Transactional
    public OperationStatus addChannelsToTag(@Valid @RequestBody AddChannelsToTagPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        for (String channelImessageIdToAdd : postBody.getChannelImessageIdList()) {
            boolean success = channelService.insertTagChannel(postBody.getTagId(), currentUser.getImessageId(), channelImessageIdToAdd);
            if(!success){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/channel/remove")
    @Transactional
    public OperationStatus removeChannelsOfTag(@Valid @RequestBody RemoveChannelsOfTagPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        for (String channelImessageIdToRemove : postBody.getChannelImessageIdList()) {
            boolean success = channelService.deleteTagChannel(postBody.getTagId(), currentUser.getImessageId(), channelImessageIdToRemove);
            if(!success){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/delete/{tagId}")
    @Transactional
    public OperationStatus deleteChannelTag(@PathVariable String tagId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        boolean success = channelService.updateChannelTagToInactive(tagId, currentUser.getImessageId());
        if(!success){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        boolean success1 = channelService.deleteAllTagChannel(tagId, currentUser.getImessageId());
        if(!success1){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/channel/set")
    @Transactional
    public OperationStatus setChannelTags(@Valid @RequestBody SetChannelTagsPostBody postBody, HttpSession session){
        if(postBody.getNewTagNames().isEmpty() && postBody.getToAddTagIds().isEmpty() && postBody.getToRemoveTagIds().isEmpty()){
            return new OperationStatus(-101, "请更改内容");
        }
        User currentUser = sessionService.getUserOfSession(session);
        for (String newTagName : postBody.getNewTagNames()) {
            String tagId = channelService.insertChannelTag(currentUser.getImessageId(), newTagName);
            if(tagId == null){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
            if(!channelService.insertTagChannel(tagId, currentUser.getImessageId(), postBody.getChannelImessageId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        for (String toAddTagId : postBody.getToAddTagIds()) {
            if(!channelService.insertTagChannel(toAddTagId, currentUser.getImessageId(), postBody.getChannelImessageId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        for (String toRemoveTagId : postBody.getToRemoveTagIds()) {
            if(!channelService.deleteTagChannel(toRemoveTagId, currentUser.getImessageId(), postBody.getChannelImessageId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @GetMapping("collection")
    public OperationData getChannelCollection(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<ChannelCollectionItem> allChannelCollections = channelService.findAllChannelCollections(currentUser.getImessageId());
        return OperationData.success(allChannelCollections);
    }

    @PostMapping("collection/add")
    @Transactional
    public OperationStatus addChannelCollection(@RequestBody AddChannelCollectionPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<String> channelIds = postBody.getChannelIds();
        for (String channelId : channelIds) {
            ChannelCollectionItem channelCollectionItem = new ChannelCollectionItem(UUID.randomUUID().toString(), currentUser.getImessageId(), channelId, new Date(), null, true);
            if(!channelService.addChannelCollection(currentUser, channelCollectionItem)){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_COLLECTIONS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("collection/remove")
    @Transactional
    public OperationStatus removeChannelCollection(@RequestBody RemoveChannelCollectionPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<String> uuids = postBody.getUuids();
        for (String uuid : uuids) {
            if(!channelService.removeChannelCollection(uuid, currentUser.getImessageId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.CHANNEL_COLLECTIONS_UPDATE, "");
        return OperationStatus.success();
    }
}
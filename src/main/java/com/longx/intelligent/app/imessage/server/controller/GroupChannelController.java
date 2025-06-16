package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.*;
import com.longx.intelligent.app.imessage.server.util.TimeUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by LONG on 2025/4/13 at 12:46 AM.
 */
@RestController
@RequestMapping("group_channel")
public class GroupChannelController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private GroupChannelService groupChannelService;
    @Autowired
    private StompService stompService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperationService redisOperationService;

    @GetMapping("find/group_channel_id/{groupChannelId}")
    public OperationData findGroupChannelById(@PathVariable String groupChannelId, @RequestParam(defaultValue = "idUser") String queryType, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = null;
        if(queryType.equals("idUser")){
            groupChannel = groupChannelService.findGroupChannelByIdUser(groupChannelId, currentUser.getImessageId());
        }else if(queryType.equals("id")) {
            groupChannel = groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId());
        }
        if(groupChannel == null) return new OperationData(-101, "无内容");
        return OperationData.success(groupChannel);
    }

    @PostMapping("create")
    @Transactional
    public OperationStatus createGroupChannel(@Valid @RequestBody CreateGroupChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        String groupChannelId = groupChannelService.createGroupChannel(currentUser.getImessageId(), postBody.getName());
        if(groupChannelId == null){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        String associationId = groupChannelService.insertGroupChannelAssociation(groupChannelId, currentUser.getImessageId(), currentUser.getImessageId(), null, new Date(), new Date(), null);
        if(associationId == null){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        if(postBody.getNote() != null){
            if(!groupChannelService.setNote(currentUser.getImessageId(), groupChannelId, postBody.getNote())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        if(postBody.getNewTagNames() != null){
            for (String newTagName : postBody.getNewTagNames()) {
                String tagId = groupChannelService.insertGroupChannelTag(currentUser.getImessageId(), newTagName);
                if(tagId == null){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return OperationStatus.failure();
                }
                if(!groupChannelService.insertTagGroupChannel(tagId, currentUser.getImessageId(), groupChannelId)){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return OperationStatus.failure();
                }
            }
        }
        if(postBody.getToAddTagIds() != null){
            for (String toAddTagId : postBody.getToAddTagIds()) {
                if(!groupChannelService.insertTagGroupChannel(toAddTagId, currentUser.getImessageId(), groupChannelId)){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return OperationStatus.failure();
                }
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNELS_UPDATE, "");
        return OperationStatus.success();
    }

    @GetMapping("association/all")
    public OperationData getAllAssociatedGroupChannel(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<GroupChannel> allGroupChannelAssociations = groupChannelService.findAllAssociatedGroupChannels(currentUser.getImessageId());
        return OperationData.success(allGroupChannelAssociations);
    }

    @PostMapping("info/group_name/change")
    public OperationStatus changeGroupName(@Valid @RequestBody ChangeGroupChannelNamePostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(postBody.getGroupId(), currentUser.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(currentUser.getImessageId())){
            return new OperationStatus(-102, "非法身份");
        }
        if(groupChannel.getName().equals(postBody.getNewGroupName())){
            return new OperationStatus(-103, "请修改群频道名称");
        }
        if(groupChannelService.updateGroupChannelName(postBody.getGroupId(), postBody.getNewGroupName(), currentUser.getImessageId())){
            stompService.sendGroupChannelUpdate(currentUser.getImessageId(), groupChannel.getGroupChannelId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @GetMapping("info/group_channel_id_user/can_change/{groupChannelId}")
    public OperationData groupChannelIdUserCanChange(HttpSession session, @PathVariable String groupChannelId){
        User user = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(groupChannelId, user.getImessageId());
        if(groupChannel == null){
            return new OperationData(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(user.getImessageId())){
            return new OperationData(-102, "非法身份");
        }
        Date groupChannelIdUserLastChangeTime = groupChannelService.findGroupChannelIdUserLastChangeTime(groupChannelId);
        if(groupChannelIdUserLastChangeTime != null && !TimeUtil.isDateAfter(groupChannelIdUserLastChangeTime, new Date(), Constants.CHANGE_GROUP_CHANNEL_ID_INTERVAL_DAYS * 24 * 60 * 60 * 1000L)){
            Date timeCanChange = TimeUtil.addDays(groupChannelIdUserLastChangeTime, Constants.CHANGE_GROUP_CHANNEL_ID_INTERVAL_DAYS);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy 年 M 月 d 日");
            String formattedCanChangeTime = simpleDateFormat.format(timeCanChange);
            return new OperationData(102, "群频道 ID " + Constants.CHANGE_GROUP_CHANNEL_ID_INTERVAL_DAYS + " 天内只能修改一次，" + formattedCanChangeTime + "后可修改。", timeCanChange);
        }else {
            return new OperationData(101, "群频道 ID " + Constants.CHANGE_GROUP_CHANNEL_ID_INTERVAL_DAYS + " 天内只能修改一次，当前可以修改。", null);
        }
    }

    @Transactional
    @PostMapping("info/group_channel_id_user/change")
    public OperationStatus changeGroupChannelIdUser(@Valid @RequestBody ChangeGroupChannelIdUserPostBody postBody, HttpSession session) {
        User user = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(postBody.getGroupChannelId(), user.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(user.getImessageId())){
            return new OperationStatus(-102, "非法身份");
        }
        Date groupChannelIdUserLastChangeTime = groupChannelService.findGroupChannelIdUserLastChangeTime(postBody.getGroupChannelId());
        if(groupChannelIdUserLastChangeTime != null && !TimeUtil.isDateAfter(groupChannelIdUserLastChangeTime, new Date(), Constants.CHANGE_GROUP_CHANNEL_ID_INTERVAL_DAYS * 24 * 60 * 60 * 1000L)){
            return new OperationStatus(-103, "当前时间不可修改群频道 ID");
        }
        if(!groupChannelService.isGroupChannelIdUserValid(postBody.getNewGroupChannelIdUser())){
            return new OperationStatus(-104, "群频道 ID 不合法");
        }
        if(groupChannelService.findGroupChannelByIdUser(postBody.getNewGroupChannelIdUser(), user.getImessageId()) != null){
            return new OperationStatus(-105, "群频道 ID 已存在");
        }
        if(postBody.getNewGroupChannelIdUser().equals(groupChannel.getGroupChannelIdUser())){
            return new OperationStatus(-106, "请修改群频道 ID");
        }
        if(groupChannelService.updateGroupChannelIdUserLastChangeTime(new Date(), groupChannel.getGroupChannelId(), user.getImessageId())) {
            if (groupChannelService.updateGroupChannelIdUser(postBody.getGroupChannelId(), postBody.getNewGroupChannelIdUser(), user.getImessageId())) {
                stompService.sendGroupChannelUpdate(user.getImessageId(), postBody.getGroupChannelId());
                return OperationStatus.success();
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationStatus.failure();
    }

    @PostMapping("info/region/change")
    public OperationStatus changeGroupChannelRegion(@Valid @RequestBody ChangeGroupChannelRegionPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(postBody.getGroupChannelId(), user.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(user.getImessageId())){
            return new OperationStatus(-102, "非法身份");
        }
        if((postBody.getThirdRegionAdcode() != null && postBody.getSecondRegionAdcode() == null)
                || (postBody.getSecondRegionAdcode() != null && postBody.getFirstRegionAdcode() == null)){
            return new OperationStatus(-103, "数据格式异常");
        }
        if(postBody.getFirstRegionAdcode() != null){
            boolean countryExist = regionService.isFirstRegionExist(postBody.getFirstRegionAdcode());
            if(!countryExist){
                return new OperationStatus(-104, "一级区域不合法");
            }
        }
        if(postBody.getSecondRegionAdcode() != null){
            boolean provinceExist = regionService.isSecondRegionExist(postBody.getFirstRegionAdcode(), postBody.getSecondRegionAdcode());
            if(!provinceExist){
                return new OperationStatus(-105, "二级区域不合法");
            }
        }
        if(postBody.getThirdRegionAdcode() != null){
            boolean cityExist = regionService.isThirdRegionExist(postBody.getSecondRegionAdcode(), postBody.getThirdRegionAdcode());
            if(!cityExist){
                return new OperationStatus(-106, "三级区域不合法");
            }
        }
        if(Objects.equals(postBody.getFirstRegionAdcode(), groupChannel.getFirstRegion() == null ? null : groupChannel.getFirstRegion().getAdcode())
                && Objects.equals(postBody.getSecondRegionAdcode(), groupChannel.getSecondRegion() == null ? null : groupChannel.getSecondRegion().getAdcode())
                && Objects.equals(postBody.getThirdRegionAdcode(), groupChannel.getThirdRegion() == null ? null : groupChannel.getThirdRegion().getAdcode())){
            return new OperationStatus(-107, "请修改群频道地区");
        }
        if(regionService.changeGroupChannelRegion(postBody.getFirstRegionAdcode(), postBody.getSecondRegionAdcode(), postBody.getThirdRegionAdcode(), groupChannel.getGroupChannelId(), user.getImessageId())){
            stompService.sendGroupChannelUpdate(user.getImessageId(), groupChannel.getGroupChannelId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @Transactional
    @PostMapping("info/avatar/change/{groupChannelId}")
    public OperationStatus changeAvatar(@RequestPart("avatar") MultipartFile avatarMultipartFile, @PathVariable String groupChannelId, HttpSession session) throws IOException {
        User user = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(groupChannelId, user.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(user.getImessageId())){
            return new OperationStatus(-102, "非法身份");
        }
        if(avatarMultipartFile.isEmpty()) return new OperationStatus(-103, "头像不能为空");

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(avatarMultipartFile.getBytes()));
        if (image == null) {
            return new OperationStatus(-104, "文件内容不是有效的图片");
        }
        int originalImageWidth = image.getWidth();
        int originalImageHeight = image.getHeight();
        int originalSize = Math.min(originalImageWidth, originalImageHeight);
        int width = Math.min(originalImageWidth, Constants.AVATAR_SIZE);
        int height = Math.min(originalImageHeight, Constants.AVATAR_SIZE);
        int size = Math.min(width, height);
        image = Thumbnails.of(image)
                .sourceRegion(Positions.CENTER, originalSize, originalSize)
                .size(size, size)
                .outputQuality(1.0)
                .outputFormat("jpg")
                .asBufferedImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] scaledAvatarData = baos.toByteArray();
        String extension = "jpg";
        GroupChannelAvatar avatar = new GroupChannelAvatar(DigestUtils.sha256Hex(scaledAvatarData), groupChannelId, extension, new Date());
        boolean b = groupChannelService.updateGroupChannelAvatar(avatar, scaledAvatarData);
        boolean b1 = groupChannelService.updateAvatarHashWithGroupChannel(avatar.getHash(), groupChannelId, user.getImessageId());
        if (b && b1) {
            stompService.sendGroupChannelUpdate(user.getImessageId(), groupChannelId);
            return OperationStatus.success();
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationStatus.failure();
    }

    @PostMapping("info/avatar/remove/{groupChannelId}")
    public OperationStatus removeAvatar(@PathVariable String groupChannelId, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(groupChannelId, user.getImessageId());
        if(groupChannel == null){
            return new OperationStatus(-101, "没有群频道");
        }
        if(!groupChannel.getOwner().equals(user.getImessageId())){
            return new OperationStatus(-102, "非法身份");
        }
        if(groupChannelService.updateAvatarHashWithGroupChannel(null, groupChannelId, user.getImessageId())){
            stompService.sendGroupChannelUpdate(user.getImessageId(), groupChannelId);
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @GetMapping("info/avatar/{avatarHash}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable("avatarHash") String avatarHash){
        GroupChannelAvatar avatar = groupChannelService.findAvatar(avatarHash);
        byte[] avatarData = groupChannelService.findAvatarData(avatarHash);
        if(avatar == null){
            String errorMessage = "{\"error\": \"Avatar not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", avatar.getGroupChannelId() + "_" + avatar.getHash() + "." + avatar.getExtension());
        headers.setContentLength(avatarData.length);
        return new ResponseEntity<>(avatarData, headers, HttpStatus.OK);
    }

    @PostMapping("association/note/set")
    public OperationStatus setNoteToAssociatedGroupChannel(@RequestBody @Valid SetNoteToAssociatedGroupChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        String groupChannelId = postBody.getGroupChannelId();
        if(!groupChannelService.isGroupChannelAssociated(groupChannelId, currentUser.getImessageId())){
            return new OperationStatus(-101, "未建立关系");
        }
        GroupChannel groupChannel = groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId());
        if(Objects.equals(groupChannel.getNote(), postBody.getNote())){
            return new OperationStatus(-102, "请修改备注");
        }
        if(!groupChannelService.setNote(currentUser.getImessageId(), groupChannelId, postBody.getNote())){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_UPDATE, groupChannelId);
        return OperationStatus.success();
    }

    @PostMapping("association/note/delete/{groupChannelId}")
    public OperationStatus deleteNoteOfAssociatedChannel(@PathVariable String groupChannelId, HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        if(!groupChannelService.isGroupChannelAssociated(groupChannelId, currentUser.getImessageId())){
            return new OperationStatus(-101, "未建立关系");
        }
        if(!groupChannelService.updateGroupChannelNoteToInactive(currentUser.getImessageId(), groupChannelId)){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_UPDATE, groupChannelId);
        return OperationStatus.success();
    }

    @PostMapping("association/tag/add")
    public OperationStatus addTag(@Valid @RequestBody AddGroupChannelTagPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        if(groupChannelService.insertGroupChannelTag(currentUser.getImessageId(), postBody.getName()) == null){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @GetMapping("association/tag/all")
    public OperationData getTags(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<GroupChannelTag> allGroupChannelTags = groupChannelService.findAllGroupChannelTags(currentUser.getImessageId());
        return OperationData.success(allGroupChannelTags);
    }

    @PostMapping("association/tag/sort")
    @Transactional
    public OperationStatus sortTags(@Valid @RequestBody SortGroupTagsPostBody postBody, HttpSession session){
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
        List<GroupChannelTag> allGroupChannelTags = groupChannelService.findAllGroupChannelTags(currentUser.getImessageId());
        if(allGroupChannelTags.size() != postBody.getOrderMap().size()){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationStatus(-101, "必须包括所有标签，且只能是自己创建的标签");
        }
        for (GroupChannelTag groupChannelTag : allGroupChannelTags) {
            boolean have = false;
            for (Map.Entry<String, Integer> tagIdOrderEntry : postBody.getOrderMap().entrySet()) {
                if(groupChannelTag.getTagId().equals(tagIdOrderEntry.getKey())){
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
        for (GroupChannelTag groupChannelTag : allGroupChannelTags) {
            for (Map.Entry<String, Integer> tagIdOrderEntry : postBody.getOrderMap().entrySet()) {
                if(groupChannelTag.getTagId().equals(tagIdOrderEntry.getKey()) && groupChannelTag.getOrder() != tagIdOrderEntry.getValue()){
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
                boolean success = groupChannelService.updateGroupChannelTagOrder(stringIntegerEntry.getKey(), currentUser.getImessageId(), stringIntegerEntry.getValue());
                if(!success){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new OperationStatus(-103, "更新顺序失败");
                }
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationStatus(-103, "更新顺序失败");
        }
        List<GroupChannelTag> allTags = groupChannelService.findAllGroupChannelTags(currentUser.getImessageId());
        allTags.sort(Comparator.comparingInt(GroupChannelTag::getOrder));
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
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/delete/{tagId}")
    @Transactional
    public OperationStatus deleteChannelTag(@PathVariable String tagId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        boolean success = groupChannelService.updateGroupChannelTagToInactive(tagId, currentUser.getImessageId());
        if(!success){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        boolean success1 = groupChannelService.deleteAllTagGroupChannel(tagId, currentUser.getImessageId());
        if(!success1){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/name/change")
    public OperationStatus changeTagName(@Valid @RequestBody ChangeGroupChannelTagNamePostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannelTag groupChannelTag = groupChannelService.findGroupChannelTag(currentUser.getImessageId(), postBody.getTagId());
        if(groupChannelTag == null){
            return OperationStatus.failure();
        }
        if(groupChannelTag.getName().equals(postBody.getName())){
            return new OperationStatus(-101, "请更改标签名称");
        }
        if(!groupChannelService.updateGroupChannelTagName(postBody.getName(), postBody.getTagId(), currentUser.getImessageId())){
            return OperationStatus.failure();
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/channel/add")
    @Transactional
    public OperationStatus addChannelsToTag(@Valid @RequestBody AddGroupChannelsToTagPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        for (String toAddGroupChannelId : postBody.getGroupChannelIdList()) {
            boolean success = groupChannelService.insertTagGroupChannel(postBody.getTagId(), currentUser.getImessageId(), toAddGroupChannelId);
            if(!success){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/channel/remove")
    @Transactional
    public OperationStatus removeChannelsOfTag(@Valid @RequestBody RemoveGroupChannelsOfTagPostBody postBody, HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        for (String groupChannelIdToRemove : postBody.getGroupChannelIdList()) {
            boolean success = groupChannelService.deleteTagGroupChannel(postBody.getTagId(), currentUser.getImessageId(), groupChannelIdToRemove);
            if(!success){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("association/tag/channel/set")
    @Transactional
    public OperationStatus setChannelTags(@Valid @RequestBody SetGroupChannelTagsPostBody postBody, HttpSession session){
        if(postBody.getNewTagNames().isEmpty() && postBody.getToAddTagIds().isEmpty() && postBody.getToRemoveTagIds().isEmpty()){
            return new OperationStatus(-101, "请更改内容");
        }
        User currentUser = sessionService.getUserOfSession(session);
        for (String newTagName : postBody.getNewTagNames()) {
            String tagId = groupChannelService.insertGroupChannelTag(currentUser.getImessageId(), newTagName);
            if(tagId == null){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
            if(!groupChannelService.insertTagGroupChannel(tagId, currentUser.getImessageId(), postBody.getGroupChannelId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        for (String toAddTagId : postBody.getToAddTagIds()) {
            if(!groupChannelService.insertTagGroupChannel(toAddTagId, currentUser.getImessageId(), postBody.getGroupChannelId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        for (String toRemoveTagId : postBody.getToRemoveTagIds()) {
            if(!groupChannelService.deleteTagGroupChannel(toRemoveTagId, currentUser.getImessageId(), postBody.getGroupChannelId())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }
        }
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_TAGS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("add/request")
    @Transactional
    public OperationStatus requestAdd(@Valid @RequestBody RequestAddGroupChannelPostBody postBody, HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannel groupChannel = groupChannelService.findGroupChannelByIdUser(postBody.getGroupChannelIdUser(), currentUser.getImessageId());
        if (groupChannel == null) return new OperationStatus(-101, "目标群频道不存在");
        for (GroupChannelAssociation groupChannelAssociation : groupChannel.getGroupChannelAssociations()) {
            if (groupChannelAssociation.getRequester().equals(currentUser.getImessageId())) {
                return new OperationStatus(-102, "群频道已经建立关联");
            }
        }
        boolean groupJoinNeedVerification = groupChannelService.isGroupJoinNeedVerification(groupChannel.getGroupChannelId());
        if (groupJoinNeedVerification && groupChannelService.isInAdding(currentUser.getImessageId(), groupChannel.getGroupChannelId())) {
            return new OperationStatus(-103, "群频道已经在添加中");
        }
        Date requestTime = new Date();
        String uuid = UUID.randomUUID().toString();
        Channel requesterChannel = channelService.findChannelByImessageId(currentUser.getImessageId(), currentUser.getImessageId());
        groupChannelService.saveRequester(GroupChannelAddition.create(uuid, requesterChannel, groupChannel, postBody.getMessage(), postBody.getNote(),
                postBody.getNewTagNames(), postBody.getToAddTagIds(), requestTime, null, false, false, false, postBody.getInviteUuid()));
        groupChannelService.saveResponder(GroupChannelAddition.create(uuid, requesterChannel, groupChannel, postBody.getMessage(), postBody.getNote(),
                postBody.getNewTagNames(), postBody.getToAddTagIds(), requestTime, null, false, false, false, postBody.getInviteUuid()));
        if (postBody.getInviteUuid() != null) {
            List<GroupChannelInvitation> groupChannelInvitations = groupChannelService.getGroupChannelInvitationByUuid(postBody.getInviteUuid(), session);
            groupChannelInvitations.forEach(groupChannelInvitation -> {
                GroupChannelInvitation respondedGroupChannelInvitation = GroupChannelInvitation.create(groupChannelInvitation.uuid(), groupChannelInvitation.inviter(), groupChannelInvitation.invitee(),
                        groupChannelInvitation.groupChannelInvitedTo(), groupChannelInvitation.message(), groupChannelInvitation.requestTime(), requestTime,
                        true, false, groupChannelInvitation.isExpired(), groupChannelInvitation.inviteType());
                if (groupChannelInvitation.inviteType() == GroupChannelInvitation.Type.INVITER) {
                    groupChannelService.saveInviter(respondedGroupChannelInvitation);
                } else if (groupChannelInvitation.inviteType() == GroupChannelInvitation.Type.INVITEE) {
                    groupChannelService.saveInvitee(respondedGroupChannelInvitation);
                }
            });
        }
        if (groupJoinNeedVerification) {
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(groupChannel.getOwner(), StompDestinations.GROUP_CHANNEL_ADDITIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(groupChannel.getOwner(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            return OperationStatus.success();
        }else {
            return acceptAdd(new AcceptAddGroupChannelPostBody(uuid), session);
        }
    }

    @GetMapping("add/activities/not_viewed_count")
    public OperationData getAdditionActivitiesNotViewedCount(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannelAdditionNotViewedCount notViewedCount = groupChannelService.getGroupChannelAdditionNotViewedCount(currentUser.getImessageId());
        return OperationData.success(notViewedCount);
    }

    @GetMapping("add/activity/all")
    public OperationData getAllAdditionActivities(HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        List<GroupChannelAddition> allChannelAddition = groupChannelService.getAllGroupChannelAddition(currentUser.getImessageId(), session);
        List<GroupChannelInvitation> allGroupChannelInvitation = groupChannelService.getAllGroupChannelInvitation(currentUser.getImessageId(), session);
        List<GroupChannelActivity> groupChannelActivities = new ArrayList<>();
        groupChannelActivities.addAll(allChannelAddition);
        groupChannelActivities.addAll(allGroupChannelInvitation);
        return OperationData.success(groupChannelActivities);
    }

    @PostMapping("add/accept")
    @Transactional
    public OperationStatus acceptAdd(@Valid @RequestBody AcceptAddGroupChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupChannelAddition requesterGroupChannelAddition = groupChannelService.getRequesterGroupChannelAdditionByUuid(postBody.getUuid(), session);
        User toUser = userService.findUserByImessageIdUser(requesterGroupChannelAddition.requesterChannel().getImessageId());
        if(toUser == null) return new OperationStatus(-101, "目标用户不存在");
        if(!groupChannelService.isInAdding(toUser.getImessageId(), requesterGroupChannelAddition.responderGroupChannel().getGroupChannelId())){
            return new OperationStatus(-102, "频道未在添加中");
        }
        if(groupChannelService.isGroupChannelAssociated(requesterGroupChannelAddition.responderGroupChannel().getGroupChannelId(), toUser.getImessageId())){
            return new OperationStatus(-103, "频道已经建立关联");
        }
        Date respondTime = new Date();
        GroupChannelAddition groupChannelAddition = GroupChannelAddition.create(requesterGroupChannelAddition.uuid(),
                requesterGroupChannelAddition.requesterChannel(), requesterGroupChannelAddition.responderGroupChannel(),
                requesterGroupChannelAddition.message(), requesterGroupChannelAddition.note(),
                requesterGroupChannelAddition.newTagNames(), requesterGroupChannelAddition.toAddTagIds(),
                requesterGroupChannelAddition.requestTime(), respondTime, true, false,
                requesterGroupChannelAddition.isExpired(), requesterGroupChannelAddition.inviteUuid());
        groupChannelService.acceptChangeRequester(groupChannelAddition);
        groupChannelService.acceptChangeResponder(groupChannelAddition);
        String groupChannelId = groupChannelService.insertGroupChannelAssociation(
                requesterGroupChannelAddition.responderGroupChannel().getGroupChannelId(), currentUser.getImessageId(), toUser.getImessageId(),
                requesterGroupChannelAddition.message(), requesterGroupChannelAddition.requestTime(), respondTime, requesterGroupChannelAddition.inviteUuid());
        boolean success1 = true;
        if(requesterGroupChannelAddition.note() != null){
            success1 = groupChannelService.setNote(toUser.getImessageId(), groupChannelId, requesterGroupChannelAddition.note());
        }
        boolean success2 = true;
        if(requesterGroupChannelAddition.newTagNames() != null){
            for (String newTagName : requesterGroupChannelAddition.newTagNames()) {
                String tagId = groupChannelService.insertGroupChannelTag(toUser.getImessageId(), newTagName);
                if(tagId == null){
                    success2 = false;
                }
                if(!groupChannelService.insertTagGroupChannel(tagId, toUser.getImessageId(), requesterGroupChannelAddition.responderGroupChannel().getGroupChannelId())){
                    success2 = false;
                }
            }
        }
        boolean success3 = true;
        if(requesterGroupChannelAddition.toAddTagIds() != null){
            for (String toAddTagId : requesterGroupChannelAddition.toAddTagIds()) {
                if(!groupChannelService.insertTagGroupChannel(toAddTagId, toUser.getImessageId(), requesterGroupChannelAddition.responderGroupChannel().getGroupChannelId())){
                    success3 = false;
                }
            }
        }
        if(groupChannelId != null && success1 && success2 && success3){
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNELS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(toUser.getImessageId(), StompDestinations.GROUP_CHANNELS_UPDATE, "");
            return OperationStatus.success();
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationStatus.failure();
    }

    @PostMapping("add/activity/{uuid}/view")
    public OperationStatus viewOneAdditionActivity(@PathVariable String uuid, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        boolean success = groupChannelService.viewOneChannelAddition(currentUser.getImessageId(), uuid);
        if(success){
            simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
            return OperationStatus.success();
        }else {
            return OperationStatus.failure();
        }
    }

    @PostMapping("add/invite")
    public OperationStatus invite(@RequestBody @Valid InviteJoinGroupChannelPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        boolean channelAssociated = channelService.isChannelAssociated(currentUser.getImessageId(), postBody.getInvitee().getImessageId());
        boolean groupChannelAssociated = groupChannelService.isGroupChannelAssociated(postBody.getGroupChannelInvitedTo().getGroupChannelId(), currentUser.getImessageId());
        if(!channelAssociated || !groupChannelAssociated){
            return new OperationStatus(-101, "频道未建立关联");
        }
        for (GroupChannelAssociation groupChannelAssociation : postBody.getGroupChannelInvitedTo().getGroupChannelAssociations()) {
            if(groupChannelAssociation.getRequester().equals(postBody.getInvitee().getImessageId())){
                return new OperationStatus(-102, "群频道已经建立关联");
            }
        }
        if(groupChannelService.isInInviting(currentUser.getImessageId(), postBody.getInvitee().getImessageId(), postBody.getGroupChannelInvitedTo().getGroupChannelId())){
            return new OperationStatus(-103, "频道已经在邀请中");
        }
        Date requestTime = new Date();
        String uuid = UUID.randomUUID().toString();
        Channel inviter = channelService.findChannelByImessageId(currentUser.getImessageId(), currentUser.getImessageId());
        groupChannelService.saveInviter(GroupChannelInvitation.create(uuid, inviter, postBody.getInvitee(), postBody.getGroupChannelInvitedTo(),
                postBody.getMessage(), requestTime, null, false, false, false, GroupChannelInvitation.Type.INVITER));
        groupChannelService.saveInvitee(GroupChannelInvitation.create(uuid, inviter, postBody.getInvitee(), postBody.getGroupChannelInvitedTo(),
                postBody.getMessage(), requestTime, null, false, false, false, GroupChannelInvitation.Type.INVITEE));
        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
        simpMessagingTemplate.convertAndSendToUser(postBody.getInvitee().getImessageId(), StompDestinations.GROUP_CHANNEL_ADDITIONS_NOT_VIEW_COUNT_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("disconnect/{groupChannelId}")
    @Transactional
    public OperationStatus disconnectChannel(@PathVariable("groupChannelId") String groupChannelId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        if(groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId()).getOwner().equals(currentUser.getImessageId())){
            return new OperationStatus(-101, "你是群主，无法退出群聊。请先转让群主身份再尝试退出。");
        }
        if(!groupChannelService.isGroupChannelAssociated(groupChannelId, currentUser.getImessageId())){
            return OperationStatus.failure();
        }
        if(!groupChannelService.setGroupChannelAssociationToInactive(groupChannelId, currentUser.getImessageId())){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationStatus.failure();
        }else {
            GroupChannelNotification groupChannelNotification = new GroupChannelNotification(UUID.randomUUID().toString(), GroupChannelNotification.Type.ACTIVE_DISCONNECT,
                    groupChannelId, currentUser.getImessageId(), false, currentUser.getImessageId(), new Date(), false);
            redisOperationService.GROUP_CHANNEL_NOTIFICATION.saveNotification(groupChannelNotification);
        }
        List<String> associatedImessageIds = new ArrayList<>();
        associatedImessageIds.add(currentUser.getImessageId());
        groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId()).getGroupChannelAssociations().forEach(groupChannelAssociation -> {
            associatedImessageIds.add(groupChannelAssociation.getRequester().getImessageId());
        });
        associatedImessageIds.forEach(associatedImessageId -> {
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNELS_UPDATE, "");
        });
        return OperationStatus.success();
    }

    @PostMapping("disconnect/manage/{groupChannelId}")
    @Transactional
    public OperationStatus manageGroupChannelDisconnectChannel(@PathVariable("groupChannelId") String groupChannelId, @RequestBody ManageGroupChannelDisconnectPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        String owner = groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId()).getOwner();
        if(!owner.equals(currentUser.getImessageId())){
            return new OperationStatus(-101, "请联系群管理员进行频道管理。");
        }
        if(postBody.getChannelIds().isEmpty()){
            return new OperationStatus(-102, "参数异常。");
        }
        for (String channelId : postBody.getChannelIds()) {
            if(!groupChannelService.isGroupChannelAssociated(groupChannelId, channelId)){
                return new OperationStatus(-102, "参数异常。");
            }
        }
        if(postBody.getChannelIds().contains(currentUser.getImessageId()) || postBody.getChannelIds().contains(owner)){
            return new OperationStatus(-103, "参数异常。");
        }
        for (String channelId : postBody.getChannelIds()) {
            if(!groupChannelService.setGroupChannelAssociationToInactive(groupChannelId, channelId)){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationStatus.failure();
            }else {
                GroupChannelNotification groupChannelNotification = new GroupChannelNotification(UUID.randomUUID().toString(), GroupChannelNotification.Type.PASSIVE_DISCONNECT,
                        groupChannelId, channelId, true, currentUser.getImessageId(), new Date(), false);
                redisOperationService.GROUP_CHANNEL_NOTIFICATION.saveNotification(groupChannelNotification);
            }
        }
        List<String> associatedImessageIds = new ArrayList<>();
        associatedImessageIds.add(currentUser.getImessageId());
        groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId()).getGroupChannelAssociations().forEach(groupChannelAssociation -> {
            associatedImessageIds.add(groupChannelAssociation.getRequester().getImessageId());
        });
        associatedImessageIds.forEach(associatedImessageId -> {
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNEL_NOTIFICATIONS_NOT_VIEW_COUNT_UPDATE, "");
            simpMessagingTemplate.convertAndSendToUser(associatedImessageId, StompDestinations.GROUP_CHANNELS_UPDATE, "");
        });
        return OperationStatus.success();
    }

    @GetMapping("group_channel_notifications")
    public OperationData getGroupChannelNotifications(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        List<GroupChannel> allAssociatedGroupChannels = groupChannelService.findAllAssociatedGroupChannels(currentUser.getImessageId());
        List<GroupChannelNotification> notifications = new ArrayList<>();
        allAssociatedGroupChannels.forEach(groupChannel -> {
            notifications.addAll(redisOperationService.GROUP_CHANNEL_NOTIFICATION.getNotifications(groupChannel.getGroupChannelId()));
        });
        notifications.addAll(redisOperationService.GROUP_CHANNEL_NOTIFICATION.getSelfNotifications(currentUser.getImessageId()));
        return OperationData.success(notifications);
    }

    @PostMapping("group_channel_notifications/view")
    public OperationStatus viewGroupChannelNotifications(@RequestBody ViewGroupChannelNotificationsPostBody postBody, HttpSession session) {
        postBody.getUuids().forEach(uuid -> {
            redisOperationService.GROUP_CHANNEL_NOTIFICATION.setToViewed(uuid);
        });
        return OperationStatus.success();
    }
}

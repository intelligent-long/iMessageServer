package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.ChatMessage;
import com.longx.intelligent.app.imessage.server.data.GroupChatMessage;
import com.longx.intelligent.app.imessage.server.data.GroupMessageViewed;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.exception.BadRequestException;
import com.longx.intelligent.app.imessage.server.service.GroupChannelService;
import com.longx.intelligent.app.imessage.server.service.GroupChatService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by LONG on 2025/4/12 at 2:22 PM.
 */
@RestController
@RequestMapping("group_chat")
public class GroupChatController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private GroupChannelService groupChannelService;
    @Autowired
    private GroupChatService groupChatService;
    @Autowired
    private Validator validator;

    @GetMapping("message/unviewed/all")
    public OperationData getAllUnviewedChatMessages(HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        List<GroupChatMessage> allUnviewedGroupChatMessages = groupChatService.getAllUnviewedGroupChatMessages(currentUser.getImessageId());
        return OperationData.success(allUnviewedGroupChatMessages);
    }

    @PostMapping("message/view/{messageUuid}")
    public OperationData viewMessage(@PathVariable String messageUuid, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        GroupMessageViewed groupMessageViewed = groupChatService.viewMessage(messageUuid, currentUser.getImessageId());
        if(groupMessageViewed.getViewedUuid() == null || groupMessageViewed.getGroupId() == null || groupMessageViewed.getFrom() == null) throw new BadRequestException("参数错误");
        return OperationData.success(groupMessageViewed);
    }

    @PostMapping("message/text/send")
    public void sendTextMessage(@Valid @RequestBody SendTextGroupChatMessagePostBody postBody, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);
        if(!groupChannelService.isInGroup(postBody.getToGroupId(), user.getImessageId())){
            pw.print(JsonUtil.toJson(new OperationStatus(-101, "未建立关系")));
            pw.close();
            return;
        }
        GroupChatMessage groupChatMessage = GroupChatMessage.newText(UUID.randomUUID().toString(), user.getImessageId(),
                postBody.getToGroupId(), new Date(), postBody.getText());
        groupChatService.sendGroupChatMessageStep1(groupChatMessage, null);
        try {
            pw.print(JsonUtil.toJson(OperationData.success(groupChatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            groupChatService.deleteGroupChatMessage(groupChatMessage.getTo(), groupChatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }
        groupChatService.sendGroupChatMessageStep2(groupChatMessage, null);
    }

    @PostMapping("message/image/send")
    public void sendImageMessage(@RequestPart("image") @NotNull MultipartFile image, @RequestPart("metadata") @NotBlank String metadata, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        SendImageGroupChatMessagePostBody postBody = JsonUtil.toObject(metadata, SendImageGroupChatMessagePostBody.class);

        Set<ConstraintViolation<SendImageGroupChatMessagePostBody>> violations = validator.validate(postBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            pw.print(JsonUtil.toJson(new OperationData(-101, errorMessage)));
            pw.close();
            return;
        }

        if(!groupChannelService.isInGroup(postBody.getToGroupChannelId(), user.getImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-102, "未建立关系")));
            pw.close();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        GroupChatMessage groupChatMessage  = GroupChatMessage.newImage(uuid, user.getImessageId(),
                postBody.getToGroupChannelId(), new Date(), postBody.getImageFileName(), uuid);
        groupChatService.sendGroupChatMessageStep1(groupChatMessage, image.getBytes());
        try {
            pw.print(JsonUtil.toJson(OperationData.success(groupChatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            groupChatService.deleteGroupChatMessage(groupChatMessage.getTo(), groupChatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        groupChatService.sendGroupChatMessageStep2(groupChatMessage, image.getBytes());
    }

    @GetMapping("message/image/new/{imageId}")
    public ResponseEntity<byte[]> getNewMessageImage(@PathVariable("imageId") String imageId){
        Object[] objects = groupChatService.getNewGroupChatMessageImage(imageId);
        byte[] chatMessageImage = (byte[]) objects[0];
        String chatMessageImageFileName = (String) objects[2];
        if(chatMessageImage == null) {
            String errorMessage = "{\"error\": \"Group chat message image not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", chatMessageImageFileName);
        headers.setContentLength(chatMessageImage.length);
        return new ResponseEntity<>(chatMessageImage, headers, HttpStatus.OK);
    }

    @PostMapping("message/video/send")
    public void sendVideoMessage(@RequestPart("video") @NotNull MultipartFile video, @RequestPart("metadata") @NotBlank String metadata, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        SendVideoGroupChatMessagePostBody postBody = JsonUtil.toObject(metadata, SendVideoGroupChatMessagePostBody.class);

        Set<ConstraintViolation<SendVideoGroupChatMessagePostBody>> violations = validator.validate(postBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            pw.print(JsonUtil.toJson(new OperationData(-101, errorMessage)));
            pw.close();
            return;
        }

        if(!groupChannelService.isInGroup(postBody.getToGroupChannelId(), user.getImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-102, "未建立关系")));
            pw.close();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        GroupChatMessage groupChatMessage  = GroupChatMessage.newVideo(uuid, user.getImessageId(),
                postBody.getToGroupChannelId(), new Date(), postBody.getVideoFileName(), uuid);
        groupChatService.sendGroupChatMessageStep1(groupChatMessage, video.getBytes());
        try {
            pw.print(JsonUtil.toJson(OperationData.success(groupChatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            groupChatService.deleteGroupChatMessage(groupChatMessage.getTo(), groupChatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        groupChatService.sendGroupChatMessageStep2(groupChatMessage, video.getBytes());
    }

    @GetMapping("message/video/new/{videoId}")
    public ResponseEntity<byte[]> getNewChatMessageVideo(@PathVariable("videoId") String videoId){
        Object[] objects = groupChatService.getNewGroupChatMessageVideo(videoId);
        byte[] chatMessageVideo = (byte[]) objects[0];
        String chatMessageVideoFileName = (String) objects[2];
        if(chatMessageVideo == null) {
            String errorMessage = "{\"error\": \"Group chat message video not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", chatMessageVideoFileName);
        headers.setContentLength(chatMessageVideo.length);
        return new ResponseEntity<>(chatMessageVideo, headers, HttpStatus.OK);
    }
}

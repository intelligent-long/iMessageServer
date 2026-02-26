package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.ChatMessage;
import com.longx.intelligent.app.imessage.server.data.ChatMessageAllow;
import com.longx.intelligent.app.imessage.server.data.MessageViewed;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.exception.BadRequestException;
import com.longx.intelligent.app.imessage.server.service.ChannelService;
import com.longx.intelligent.app.imessage.server.service.ChatService;
import com.longx.intelligent.app.imessage.server.service.PermissionService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.util.AudioUtil;
import com.longx.intelligent.app.imessage.server.util.JsonUtil;
import com.longx.intelligent.app.imessage.server.util.TimeUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by LONG on 2024/5/12 at 4:43 AM.
 */
@RestController
@RequestMapping("chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private Validator validator;
    @Autowired
    private PermissionService permissionService;

    @GetMapping("message/unviewed/all")
    public OperationData getAllUnviewedChatMessages(HttpSession session) {
        User currentUser = sessionService.getUserOfSession(session);
        List<ChatMessage> allUnviewedChatMessages = chatService.getAllUnviewedChatMessages(currentUser.getImessageId());
        return OperationData.success(allUnviewedChatMessages);
    }

    @PostMapping("message/view/{messageUuid}")
    public OperationData viewMessage(@PathVariable String messageUuid, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        MessageViewed messageViewed = chatService.viewMessage(currentUser.getImessageId(), messageUuid);
        if(messageViewed.viewedUuid() == null || messageViewed.other() == null) throw new BadRequestException("参数错误");
        return OperationData.success(messageViewed);
    }

    @PostMapping("message/text/send")
    public void sendTextMessage(@Valid @RequestBody SendTextChatMessagePostBody postBody, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);
        if(!channelService.isChannelAssociated(user.getImessageId(), postBody.getToImessageId())){
            pw.print(JsonUtil.toJson(new OperationStatus(-101, "未建立关系")));
            pw.close();
            return;
        }
        ChatMessage chatMessage  = ChatMessage.newText(UUID.randomUUID().toString(), user.getImessageId(),
                        postBody.getToImessageId(), new Date(), postBody.getText());
        chatService.sendChatMessageStep1(chatMessage, null);
        try {
            pw.print(JsonUtil.toJson(OperationData.success(chatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            chatService.deleteChatMessage(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }
        chatService.sendChatMessageStep2(chatMessage, null);
    }

    @PostMapping("message/image/send")
    public void sendImageMessage(@RequestPart("image") @NotNull MultipartFile image, @RequestPart("metadata") @NotBlank String metadata, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        SendImageChatMessagePostBody postBody = JsonUtil.toObject(metadata, SendImageChatMessagePostBody.class);

        Set<ConstraintViolation<SendImageChatMessagePostBody>> violations = validator.validate(postBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            pw.print(JsonUtil.toJson(new OperationData(-101, errorMessage)));
            pw.close();
            return;
        }

        if(!channelService.isChannelAssociated(user.getImessageId(), postBody.getToImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-102, "未建立关系")));
            pw.close();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        ChatMessage chatMessage  = ChatMessage.newImage(uuid, user.getImessageId(),
                postBody.getToImessageId(), new Date(), postBody.getImageFileName(), uuid);
        chatService.sendChatMessageStep1(chatMessage, image.getBytes());
        try {
            pw.print(JsonUtil.toJson(OperationData.success(chatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            chatService.deleteChatMessage(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        chatService.sendChatMessageStep2(chatMessage, image.getBytes());
    }

    @GetMapping("message/image/new/{imageId}")
    public ResponseEntity<byte[]> getNewMessageImage(@PathVariable("imageId") String imageId){
        Object[] objects = chatService.getNewChatMessageImage(imageId);
        byte[] chatMessageImage = (byte[]) objects[0];
        String chatMessageImageFileName = (String) objects[2];
        if(chatMessageImage == null) {
            String errorMessage = "{\"error\": \"Chat message image not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", chatMessageImageFileName);
        headers.setContentLength(chatMessageImage.length);
        return new ResponseEntity<>(chatMessageImage, headers, HttpStatus.OK);
    }

    @PostMapping("message/file/send")
    public void sendFileMessage(@RequestPart("file") @NotNull MultipartFile file, @RequestPart("metadata") @NotBlank String metadata, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        SendFileChatMessagePostBody postBody = JsonUtil.toObject(metadata, SendFileChatMessagePostBody.class);

        Set<ConstraintViolation<SendFileChatMessagePostBody>> violations = validator.validate(postBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            pw.print(JsonUtil.toJson(new OperationData(-101, errorMessage)));
            pw.close();
            return;
        }

        if(!channelService.isChannelAssociated(user.getImessageId(), postBody.getToImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-102, "未建立关系")));
            pw.close();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        ChatMessage chatMessage  = ChatMessage.newFile(uuid, user.getImessageId(),
                postBody.getToImessageId(), new Date(), postBody.getFileName(), uuid);
        chatService.sendChatMessageStep1(chatMessage, file.getBytes());
        try {
            pw.print(JsonUtil.toJson(OperationData.success(chatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            chatService.deleteChatMessage(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        chatService.sendChatMessageStep2(chatMessage, file.getBytes());
    }

    @GetMapping("message/file/new/{fileId}")
    public ResponseEntity<byte[]> getNewChatMessageFile(@PathVariable("fileId") String fileId){
        Object[] objects = chatService.getNewChatMessageFile(fileId);
        byte[] chatMessageFile = (byte[]) objects[0];
        String chatMessageFileFileName = (String) objects[2];
        if(chatMessageFile == null) {
            String errorMessage = "{\"error\": \"Chat message image not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", chatMessageFileFileName);
        headers.setContentLength(chatMessageFile.length);
        return new ResponseEntity<>(chatMessageFile, headers, HttpStatus.OK);
    }

    @PostMapping("message/video/send")
    public void sendVideoMessage(@RequestPart("video") @NotNull MultipartFile video, @RequestPart("metadata") @NotBlank String metadata, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        SendVideoChatMessagePostBody postBody = JsonUtil.toObject(metadata, SendVideoChatMessagePostBody.class);

        Set<ConstraintViolation<SendVideoChatMessagePostBody>> violations = validator.validate(postBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            pw.print(JsonUtil.toJson(new OperationData(-101, errorMessage)));
            pw.close();
            return;
        }

        if(!channelService.isChannelAssociated(user.getImessageId(), postBody.getToImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-102, "未建立关系")));
            pw.close();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        ChatMessage chatMessage  = ChatMessage.newVideo(uuid, user.getImessageId(),
                postBody.getToImessageId(), new Date(), postBody.getVideoFileName(), uuid);
        chatService.sendChatMessageStep1(chatMessage, video.getBytes());
        try {
            pw.print(JsonUtil.toJson(OperationData.success(chatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            chatService.deleteChatMessage(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        chatService.sendChatMessageStep2(chatMessage, video.getBytes());
    }

    @GetMapping("message/video/new/{videoId}")
    public ResponseEntity<byte[]> getNewChatMessageVideo(@PathVariable("videoId") String videoId){
        Object[] objects = chatService.getNewChatMessageVideo(videoId);
        byte[] chatMessageVideo = (byte[]) objects[0];
        String chatMessageVideoFileName = (String) objects[2];
        if(chatMessageVideo == null) {
            String errorMessage = "{\"error\": \"Chat message video not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", chatMessageVideoFileName);
        headers.setContentLength(chatMessageVideo.length);
        return new ResponseEntity<>(chatMessageVideo, headers, HttpStatus.OK);
    }

    @PostMapping("message/voice/send")
    public void sendVoiceMessage(@RequestPart("voice") @NotNull MultipartFile voice, @RequestPart("metadata") @NotBlank String metadata, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        SendVoiceChatMessagePostBody postBody = JsonUtil.toObject(metadata, SendVoiceChatMessagePostBody.class);

        Set<ConstraintViolation<SendVoiceChatMessagePostBody>> violations = validator.validate(postBody);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            pw.print(JsonUtil.toJson(new OperationData(-101, errorMessage)));
            pw.close();
            return;
        }

        ChatMessageAllow chatMessageAllow = permissionService.findChatMessageAllow(postBody.getToImessageId(), user.getImessageId());
        if(chatMessageAllow != null && !chatMessageAllow.isAllowVoice()){
            pw.print(JsonUtil.toJson(new OperationData(-199, "对方拒绝接收此类消息")));
            pw.close();
            return;
        }

        if(!channelService.isChannelAssociated(user.getImessageId(), postBody.getToImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-102, "未建立关系")));
            pw.close();
            return;
        }

        long audioDurationSec = AudioUtil.getAudioDurationSec(voice.getBytes());
        if(audioDurationSec == -1 || audioDurationSec > Constants.MAX_ALLOW_CHAT_VOICE_DURATION_SEC){
            pw.print(JsonUtil.toJson(new OperationData(-103, "语音不合法")));
            pw.close();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        ChatMessage chatMessage = ChatMessage.newVoice(uuid, user.getImessageId(), postBody.getToImessageId(), new Date(), uuid);
        chatService.sendChatMessageStep1(chatMessage, voice.getBytes());
        try {
            pw.print(JsonUtil.toJson(OperationData.success(chatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            chatService.deleteChatMessage(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        chatService.sendChatMessageStep2(chatMessage, voice.getBytes());
    }

    @GetMapping("message/voice/new/{voiceId}")
    public ResponseEntity<byte[]> getNewChatMessageVoice(@PathVariable("voiceId") String voiceId){
        Object[] objects = chatService.getNewChatMessageVoice(voiceId);
        byte[] chatMessageVoice = (byte[]) objects[0];
        if(chatMessageVoice == null) {
            String errorMessage = "{\"error\": \"Chat message video not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(chatMessageVoice.length);
        return new ResponseEntity<>(chatMessageVoice, headers, HttpStatus.OK);
    }

    @PostMapping("message/unsend/{receiver}/{chatMessageUuid}")
    public void unsendMessage(@PathVariable("receiver") String receiver, @PathVariable("chatMessageUuid") String chatMessageUuid, HttpServletResponse response, HttpSession session) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        User user = sessionService.getUserOfSession(session);

        ChatMessage chatMessageFound = chatService.findChatMessage(receiver, chatMessageUuid);
        if(chatMessageFound == null){
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }
        if(!chatMessageFound.getFrom().equals(user.getImessageId())){
            pw.print(JsonUtil.toJson(new OperationData(-101, "不是自己发送的消息")));
            pw.close();
            return;
        }
        if(TimeUtil.isDateAfter(chatMessageFound.getTime().getTime(), new Date().getTime(), Constants.MAX_ALLOW_UNSEND_MINUTES * 60 * 1000)){
            pw.print(JsonUtil.toJson(new OperationData(-102, "只能在 " + Constants.MAX_ALLOW_UNSEND_MINUTES + " 分钟内撤回")));
            pw.close();
            return;
        }

        ChatMessage unsendChatMessage = ChatMessage.newUnsend(UUID.randomUUID().toString(), user.getImessageId(), chatMessageFound.getTo(), new Date(), chatMessageFound.getUuid());
        chatService.sendChatMessageStep1(unsendChatMessage, null);

        try {
            pw.print(JsonUtil.toJson(OperationData.success(unsendChatMessage)));
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
            chatService.deleteChatMessage(unsendChatMessage.getTo(), unsendChatMessage.getFrom(), unsendChatMessage.getUuid());
            pw.print(JsonUtil.toJson(OperationData.failure()));
            pw.close();
            return;
        }

        chatService.deleteChatMessage(receiver, user.getImessageId(), chatMessageUuid);
        chatService.sendChatMessageStep2(unsendChatMessage, null);
    }
}

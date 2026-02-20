package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.GroupChatMessage;
import com.longx.intelligent.app.imessage.server.data.GroupMessageViewed;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.SendTextGroupChatMessagePostBody;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.exception.BadRequestException;
import com.longx.intelligent.app.imessage.server.service.GroupChannelService;
import com.longx.intelligent.app.imessage.server.service.GroupChatService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
        if(groupMessageViewed.viewedUuid() == null || groupMessageViewed.groupId() == null || groupMessageViewed.from() == null) throw new BadRequestException("参数错误");
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
}

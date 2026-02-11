package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.AmapDistrict;
import com.longx.intelligent.app.imessage.server.data.Avatar;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.service.*;
import com.longx.intelligent.app.imessage.server.util.TimeUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
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
 * Created by LONG on 2024/4/1 at 4:02 AM.
 */
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private UserService userService;
    @Autowired
    private StompService stompService;
    @Autowired
    private VerifyCodeService verifyCodeService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RegionService regionService;

    @GetMapping("who_am_i")
    public OperationData whoAmI(HttpSession session){
        User user = sessionService.getUserOfSession(session);
        if(user != null){
            return OperationData.success(user.getSelf(permissionService));
        }
        return OperationData.failure();
    }

    @GetMapping("info/avatar/{avatarHash}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable("avatarHash") String avatarHash){
        Avatar avatar = userService.findAvatar(avatarHash);
        byte[] avatarData = userService.findAvatarData(avatarHash);
        if(avatar == null){
            String errorMessage = "{\"error\": \"Avatar not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", avatar.getImessageId() + "_" + avatar.getHash() + "." + avatar.getExtension());
        headers.setContentLength(avatarData.length);
        return new ResponseEntity<>(avatarData, headers, HttpStatus.OK);
    }

    @PostMapping("info/avatar/change")
    @Transactional
    public OperationStatus changeAvatar(@RequestPart("avatar") MultipartFile avatarMultipartFile, HttpSession session) throws IOException {
        User user = sessionService.getUserOfSession(session);
        if(avatarMultipartFile.isEmpty()) return new OperationStatus(-101, "头像不能为空");

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(avatarMultipartFile.getBytes()));
        if (image == null) {
            return new OperationStatus(-102, "文件内容不是有效的图片");
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
        Avatar avatar = new Avatar(DigestUtils.sha256Hex(scaledAvatarData), user.getImessageId(), extension, new Date());
        if (userService.updateAvatar(avatar, scaledAvatarData) && userService.updateAvatarHashWithUser(avatar.getHash(), user.getImessageId())) {
            sessionService.findAndSetUserToSession(session, user.getImessageId());
            stompService.sendUserInfoUpdate(user.getImessageId());
            return OperationStatus.success();
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationStatus.failure();
    }

    @PostMapping("info/avatar/remove")
    public OperationStatus removeAvatar(HttpSession session){
        User user = sessionService.getUserOfSession(session);
        if(userService.updateAvatarHashWithUser(null, user.getImessageId())){
            sessionService.findAndSetUserToSession(session, user.getImessageId());
            stompService.sendUserInfoUpdate(user.getImessageId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @GetMapping("info/imessage_id_user/can_change")
    public OperationData imessageIdUserCanChange(HttpSession session){
        User user = sessionService.getUserOfSession(session);
        Date imessageIdUserLastChangeTime = userService.findImessageIdUserLastChangeTime(user.getImessageId());
        if(imessageIdUserLastChangeTime != null && !TimeUtil.isDateAfter(imessageIdUserLastChangeTime, new Date(), Constants.CHANGE_IMESSAGE_ID_INTERVAL_DAYS * 24 * 60 * 60 * 1000L)){
            Date timeCanChange = TimeUtil.addDays(imessageIdUserLastChangeTime, Constants.CHANGE_IMESSAGE_ID_INTERVAL_DAYS);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy 年 M 月 d 日");
            String formattedCanChangeTime = simpleDateFormat.format(timeCanChange);
            return new OperationData(102, "Apollo ID " + Constants.CHANGE_IMESSAGE_ID_INTERVAL_DAYS + " 天内只能修改一次，" + formattedCanChangeTime + "后可修改。", timeCanChange);
        }else {
            return new OperationData(101, "Apollo ID " + Constants.CHANGE_IMESSAGE_ID_INTERVAL_DAYS + " 天内只能修改一次，当前可以修改。", null);
        }
    }

    @Transactional
    @PostMapping("info/imessage_id_user/change")
    public OperationStatus changeImessageIdUser(@Valid @RequestBody ChangeImessageIdUserPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        Date imessageIdUserLastChangeTime = userService.findImessageIdUserLastChangeTime(user.getImessageId());
        if(imessageIdUserLastChangeTime != null && !TimeUtil.isDateAfter(imessageIdUserLastChangeTime, new Date(), Constants.CHANGE_IMESSAGE_ID_INTERVAL_DAYS * 24 * 60 * 60 * 1000L)){
            return new OperationStatus(-101, "当前时间不可修改 Apollo ID");
        }
        if(!userService.isImessageIdUserValid(postBody.getImessageIdUser())){
            return new OperationStatus(-102, "Apollo ID 不合法");
        }
        if(userService.findUserByImessageIdUser(postBody.getImessageIdUser()) != null){
            return new OperationStatus(-103, "Apollo ID 已存在");
        }
        if(postBody.getImessageIdUser().equals(user.getImessageIdUser())){
            return new OperationStatus(-104, "请修改 Apollo ID");
        }
        if(userService.updateImessageIdUserLastChangeTime(new Date(), user.getImessageId())) {
            if (userService.updateImessageIdUser(postBody.getImessageIdUser(), user.getImessageId())) {
                sessionService.findAndSetUserToSession(session, user.getImessageId());
                stompService.sendUserInfoUpdate(user.getImessageId());
                return OperationStatus.success();
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return OperationStatus.failure();
    }

    @PostMapping("info/username/change")
    public OperationStatus changeUsername(@Valid @RequestBody ChangeUsernamePostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        if(postBody.getUsername().equals(user.getUsername())){
            return new OperationStatus(-101, "请修改用户名");
        }
        if(userService.updateUsername(postBody.getUsername(), user.getImessageId())){
            sessionService.findAndSetUserToSession(session, user.getImessageId());
            stompService.sendUserInfoUpdate(user.getImessageId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @PostMapping("info/email/change")
    public OperationStatus changeEmail(@Valid @RequestBody ChangeEmailPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        VerifyCodeService.VerifyResult verifyResult = verifyCodeService.verify(postBody.getEmail(), postBody.getVerifyCode());
        switch (verifyResult){
            case TRY_LATER -> {
                return new OperationStatus(-101, "请稍后再试");
            }
            case INCORRECT -> {
                return new OperationStatus(-102, "验证码错误");
            }
            case CORRECT -> {
                if(postBody.getEmail().equals(user.getEmail())){
                    return new OperationStatus(-103, "请修改邮箱");
                }
                if(userService.updateEmail(postBody.getEmail(), user.getImessageId())){
                    sessionService.findAndSetUserToSession(session, user.getImessageId());
                    stompService.sendUserInfoUpdate(user.getImessageId());
                    return OperationStatus.success();
                }
            }
        }
        return OperationStatus.failure();
    }

    @PostMapping("info/sex/change")
    public OperationStatus changeSex(@Valid @RequestBody ChangeSexPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        if(Objects.equals(postBody.getSex(), user.getSex())){
            return new OperationStatus(-101, "请修改性别");
        }
        if(userService.updateSex(postBody.getSex(), user.getImessageId())){
            sessionService.findAndSetUserToSession(session, user.getImessageId());
            stompService.sendUserInfoUpdate(user.getImessageId());
            return OperationStatus.success();
        }
        return OperationStatus.failure();
    }

    @PostMapping("info/region/change")
    public OperationStatus changeRegion(@Valid @RequestBody ChangeRegionPostBody postBody, HttpSession session){
        User user = sessionService.getUserOfSession(session);
        if((postBody.getThirdRegionAdcode() != null && postBody.getSecondRegionAdcode() == null)
                || (postBody.getSecondRegionAdcode() != null && postBody.getFirstRegionAdcode() == null)){
            return new OperationStatus(-101, "数据格式异常");
        }
        if(postBody.getFirstRegionAdcode() != null){
            boolean countryExist = regionService.isFirstRegionExist(postBody.getFirstRegionAdcode());
            if(!countryExist){
                return new OperationStatus(-102, "一级区域不合法");
            }
        }
        if(postBody.getSecondRegionAdcode() != null){
            boolean provinceExist = regionService.isSecondRegionExist(postBody.getFirstRegionAdcode(), postBody.getSecondRegionAdcode());
            if(!provinceExist){
                return new OperationStatus(-103, "二级区域不合法");
            }
        }
        if(postBody.getThirdRegionAdcode() != null){
            boolean cityExist = regionService.isThirdRegionExist(postBody.getSecondRegionAdcode(), postBody.getThirdRegionAdcode());
            if(!cityExist){
                return new OperationStatus(-104, "三级区域不合法");
            }
        }
        if(Objects.equals(postBody.getFirstRegionAdcode(), user.getFirstRegion() == null ? null : user.getFirstRegion().getAdcode())
                && Objects.equals(postBody.getSecondRegionAdcode(), user.getSecondRegion() == null ? null : user.getSecondRegion().getAdcode())
                && Objects.equals(postBody.getThirdRegionAdcode(), user.getThirdRegion() == null ? null : user.getThirdRegion().getAdcode())){
            return new OperationStatus(-105, "请修改地区");
        }
        if(regionService.changeChannelRegion(postBody.getFirstRegionAdcode(), postBody.getSecondRegionAdcode(), postBody.getThirdRegionAdcode(), user.getImessageId())){
            sessionService.findAndSetUserToSession(session, user.getImessageId());
            stompService.sendUserInfoUpdate(user.getImessageId());
            return OperationStatus.success();
        }

        return OperationStatus.failure();
    }

}

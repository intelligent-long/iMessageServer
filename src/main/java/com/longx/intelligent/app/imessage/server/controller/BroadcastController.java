package com.longx.intelligent.app.imessage.server.controller;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.data.request.*;
import com.longx.intelligent.app.imessage.server.data.response.OperationData;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.data.response.PaginatedOperationData;
import com.longx.intelligent.app.imessage.server.service.*;
import com.longx.intelligent.app.imessage.server.util.MediaUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

import java.util.*;

/**
 * Created by LONG on 2024/7/28 at 1:51 AM.
 */
@RestController
@RequestMapping("broadcast")
public class BroadcastController {
    @Autowired
    private BroadcastService broadcastService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private PermissionService permissionService;

    @PostMapping("send")
    @Transactional
    public OperationData sendBroadcast(@RequestPart("body") @Valid SendBroadcastPostBody postBody,
                                         @RequestPart(value = "medias", required = false) List<MultipartFile> medias,
                                         HttpSession session){
        if((postBody.getText() == null || postBody.getText().isEmpty()) && (medias == null || medias.isEmpty())){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-101, "广播内容不合法");
        }

        if(medias != null && (
                (postBody.getMediaTypes() != null && postBody.getMediaTypes().size() != medias.size()) ||
                (postBody.getMediaExtensions() != null && postBody.getMediaExtensions().size() != medias.size())
        )){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-102, "广播媒体不合法");
        }

        for (Integer mediaType : postBody.getMediaTypes()) {
            if(!(mediaType.equals(BroadcastMedia.TYPE_IMAGE) || mediaType.equals(BroadcastMedia.TYPE_VIDEO))){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new OperationData(-102, "广播媒体不合法");
            }
        }

        if(medias != null){
            int imageCount = 0;
            int videoCount = 0;
            for (Integer mediaType : postBody.getMediaTypes()) {
                if(mediaType == BroadcastMedia.TYPE_IMAGE) {
                    imageCount ++;
                }else if(mediaType == BroadcastMedia.TYPE_VIDEO){
                    videoCount ++;
                }
            }
            if(imageCount > Constants.MAX_BROADCAST_IMAGE_COUNT || videoCount > Constants.MAX_BROADCAST_VIDEO_COUNT){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new OperationData(-103, "广播图片数量不能超过 " + Constants.MAX_BROADCAST_IMAGE_COUNT
                        + ", 视频数量不能超过 " + Constants.MAX_BROADCAST_VIDEO_COUNT);
            }
            for (MultipartFile media : medias) {
                if(media.getSize() > Constants.MAX_BROADCAST_VIDEO_FILE_SIZE_BYTE){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new OperationData(-104, "广播视频文件不能超过 " + Constants.MAX_BROADCAST_VIDEO_FILE_SIZE_BYTE + " Bytes");
                }
            }
        }

        if(postBody.getBroadcastPermission().getPermission() != BroadcastPermission.PUBLIC &&
                postBody.getBroadcastPermission().getPermission() != BroadcastPermission.PRIVATE &&
                postBody.getBroadcastPermission().getPermission() != BroadcastPermission.CONNECTED_CHANNEL_CIRCLE){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-105, "权限不合法");
        }

        User currentUser = sessionService.getUserOfSession(session);
        String broadcastId = broadcastService.sendBroadcast(currentUser.getImessageId(), new Date(), postBody.getText());
        if(broadcastId == null){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationData.failure();
        }

        if(medias != null) {
            try {
                for (int i = 0; i < medias.size(); i++) {
                    MultipartFile media = medias.get(i);
                    Size size = null;
                    Long videoDuration = null;
                    if(postBody.getMediaTypes().get(i).equals(BroadcastMedia.TYPE_IMAGE)){
                        size = MediaUtil.getImageSize(media.getInputStream());
                    }else if(postBody.getMediaTypes().get(i).equals(BroadcastMedia.TYPE_VIDEO)){
                        VideoInfo videoInfo = MediaUtil.getVideoInfo(media.getInputStream(), postBody.getMediaExtensions().get(i));
                        size = videoInfo.getSize();
                        videoDuration = videoInfo.getDuration();
                    }
                    BroadcastMedia broadcastMedia = new BroadcastMedia(UUID.randomUUID().toString(), broadcastId,
                            media.getBytes(), postBody.getMediaTypes().get(i), postBody.getMediaExtensions().get(i), i,
                            size, videoDuration);
                    boolean success = broadcastService.insertBroadcastMedia(broadcastMedia);
                    if (!success) {
                        throw new Exception("广播媒体插入失败 > index: " + i);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationData.failure();
            }
        }

        boolean success = permissionService.insertOrUpdateBroadcastPermission(broadcastId, currentUser.getImessageId(), postBody.getBroadcastPermission().getPermission());
        if(!success){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationData.failure();
        }
        if(postBody.getBroadcastPermission().getExcludeConnectedChannels() == null){
            permissionService.deleteAllBroadcastPermissionExcludeConnectedChannels(broadcastId, currentUser.getImessageId());
        }else {
            boolean success1 = permissionService.updateAllBroadcastPermissionExcludeConnectedChannels(broadcastId, currentUser.getImessageId(), postBody.getBroadcastPermission().getExcludeConnectedChannels());
            if (!success1) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationData.failure();
            }
        }

        new Thread(() -> {
            channelService.findAllChannelAssociations(currentUser.getImessageId()).forEach(channelAssociation -> {
                simpMessagingTemplate.convertAndSendToUser(channelAssociation.getChannelImessageId(), StompDestinations.BROADCASTS_NEWS_UPDATE, currentUser.getImessageId());
                simpMessagingTemplate.convertAndSendToUser(channelAssociation.getChannelImessageId(), StompDestinations.RECENT_BROADCAST_MEDIAS_UPDATE, currentUser.getImessageId());
            });
        }).start();

        return OperationData.success(broadcastService.findBroadcast(broadcastId, currentUser.getImessageId()));
    }

    @GetMapping("limit")
    public PaginatedOperationData<Broadcast> getBroadcastsLimit(@RequestParam(value = "last_broadcast_id", required = false) String lastBroadcastId, @RequestParam("ps") int ps, @RequestParam("desc") boolean desc, HttpSession session){
        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        User currentUser = sessionService.getUserOfSession(session);

        List<ChannelAssociation> allChannelAssociations = channelService.findAllChannelAssociations(currentUser.getImessageId());
        List<String> channelIds = new ArrayList<>();
        channelIds.add(currentUser.getImessageId());
        allChannelAssociations.forEach(channelAssociation -> {
            channelIds.add(channelAssociation.getChannelImessageId());
        });

        List<Broadcast> broadcastsLimit = broadcastService.findBroadcastsLimit(lastBroadcastId, ps, channelIds, currentUser.getImessageId(), desc);
        List<Broadcast> broadcastsLimitFiltered = broadcastService.filterBroadcastListToVisibleContents(broadcastsLimit, currentUser.getImessageId());
        while (!broadcastsLimit.isEmpty() && broadcastsLimitFiltered.size() < ps && broadcastService.getBroadcastPosition(broadcastsLimit.getLast().getBroadcastId()) > 0) {
            List<Broadcast> broadcastsLimitSupplement = broadcastService.findBroadcastsLimit(broadcastsLimit.getLast().getBroadcastId(), ps - broadcastsLimitFiltered.size(), channelIds, currentUser.getImessageId(), desc);
            if(broadcastsLimitSupplement.isEmpty()) break;
            broadcastsLimit.addAll(broadcastsLimitSupplement);
            broadcastsLimitSupplement = broadcastService.filterBroadcastListToVisibleContents(broadcastsLimitSupplement, currentUser.getImessageId());
            broadcastsLimitFiltered.addAll(broadcastsLimitSupplement);
        }
        if(broadcastsLimitFiltered.isEmpty()) return new PaginatedOperationData<>(-102, "没有内容", null, false);
        broadcastsLimitFiltered.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        broadcastsLimitFiltered.forEach(broadcast -> {
            List<BroadcastMedia> broadcastMedias = broadcastService.findBroadcastMedias(broadcast.getBroadcastId());
            if(!broadcastMedias.isEmpty()) broadcast.setBroadcastMedias(broadcastMedias);
        });
        boolean hasMore = false;
        if(!broadcastsLimitFiltered.isEmpty()) {
            List<Broadcast> broadcastsLimit1 = broadcastService.findBroadcastsLimit(broadcastsLimit.getLast().getBroadcastId(), 50, channelIds, currentUser.getImessageId(), desc);
            List<Broadcast> broadcastsLimit1Filter = broadcastService.filterBroadcastListToVisibleContents(broadcastsLimit1, currentUser.getImessageId());
            if(!broadcastsLimit1.isEmpty()) {
                if (!broadcastsLimit1Filter.isEmpty()) {
                    hasMore = true;
                } else {
                    while (broadcastService.getBroadcastPosition(broadcastsLimit1.getLast().getBroadcastId()) > 0) {
                        List<Broadcast> broadcastsLimitSupplement = broadcastService.findBroadcastsLimit(broadcastsLimit1.getLast().getBroadcastId(), 50, channelIds, currentUser.getImessageId(), desc);
                        if(broadcastsLimitSupplement.isEmpty()) break;
                        broadcastsLimit1.addAll(broadcastsLimitSupplement);
                        broadcastsLimitSupplement = broadcastService.filterBroadcastListToVisibleContents(broadcastsLimitSupplement, currentUser.getImessageId());
                        if (!broadcastsLimitSupplement.isEmpty()) {
                            hasMore = true;
                            break;
                        }
                    }
                }
            }
        }
        return PaginatedOperationData.paginatedSuccess(broadcastsLimitFiltered, hasMore);
    }

    @GetMapping("media/data/{mediaId}")
    public ResponseEntity<byte[]> getMediaData(@PathVariable("mediaId") String mediaId,
                                               @RequestHeader(value = "Range", required = false) String range) {
        BroadcastMedia media = broadcastService.findBroadcastMedia(mediaId);
        byte[] mediaData = broadcastService.findBroadcastMediaData(mediaId);

        if (media == null) {
            String errorMessage = "{\"error\": \"Media not found.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorMessage.getBytes());
        }

        long length = mediaData.length;
        if (range != null) {
            String[] ranges = range.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = (ranges.length > 1) ? Long.parseLong(ranges[1]) : length - 1;

            start = Math.max(0, start);
            end = Math.min(length - 1, end);

            byte[] partialData = Arrays.copyOfRange(mediaData, (int) start, (int) (end + 1));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", media.getBroadcastId() + "_" + media.getMediaId() + "." + media.getExtension());
            headers.setContentLength(end - start + 1);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            return new ResponseEntity<>(partialData, headers, HttpStatus.PARTIAL_CONTENT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", media.getBroadcastId() + "_" + media.getMediaId() + "." + media.getExtension());
        headers.setContentLength(length);
        return new ResponseEntity<>(mediaData, headers, HttpStatus.OK);
    }


    @PostMapping("delete")
    public OperationStatus deleteBroadcast(@RequestParam("broadcast_id") String broadcastId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        if(broadcastService.deleteBroadcast(broadcastId, currentUser.getImessageId())){
            return OperationStatus.success();
        }else {
            return OperationStatus.failure();
        }
    }

    @GetMapping("channel/limit")
    public PaginatedOperationData<Broadcast> getChannelBroadcastsLimit(HttpSession session, @RequestParam("channel_id") String channelId, @RequestParam(value = "last_broadcast_id", required = false) String lastBroadcastId, @RequestParam("ps") int ps, @RequestParam("desc") boolean desc){
        User currentUser = sessionService.getUserOfSession(session);

        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        List<String> channelIds = new ArrayList<>();
        channelIds.add(channelId);

        List<Broadcast> broadcastsLimit = broadcastService.findBroadcastsLimit(lastBroadcastId, ps, channelIds, currentUser.getImessageId(), desc);
        List<Broadcast> broadcastsLimitFiltered = broadcastService.filterBroadcastListToVisibleContentsForChannel(broadcastsLimit, currentUser.getImessageId());
        while (!broadcastsLimit.isEmpty() && broadcastsLimitFiltered.size() < ps && broadcastService.getBroadcastPosition(broadcastsLimit.getLast().getBroadcastId()) > 0) {
            List<Broadcast> broadcastsLimitSupplement = broadcastService.findBroadcastsLimit(broadcastsLimit.getLast().getBroadcastId(), ps - broadcastsLimitFiltered.size(), channelIds, currentUser.getImessageId(), desc);
            if(broadcastsLimitSupplement.isEmpty()) break;
            broadcastsLimit.addAll(broadcastsLimitSupplement);
            broadcastsLimitSupplement = broadcastService.filterBroadcastListToVisibleContentsForChannel(broadcastsLimitSupplement, currentUser.getImessageId());
            broadcastsLimitFiltered.addAll(broadcastsLimitSupplement);
        }
        if(broadcastsLimitFiltered.isEmpty()) return new PaginatedOperationData<>(-102, "没有内容", null, false);
        broadcastsLimitFiltered.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        broadcastsLimitFiltered.forEach(broadcast -> {
            List<BroadcastMedia> broadcastMedias = broadcastService.findBroadcastMedias(broadcast.getBroadcastId());
            if(!broadcastMedias.isEmpty()) broadcast.setBroadcastMedias(broadcastMedias);
        });
        boolean hasMore = false;
        if(!broadcastsLimitFiltered.isEmpty()) {
            List<Broadcast> broadcastsLimit1 = broadcastService.findBroadcastsLimit(broadcastsLimit.getLast().getBroadcastId(), 50, channelIds, currentUser.getImessageId(), desc);
            List<Broadcast> broadcastsLimit1Filter = broadcastService.filterBroadcastListToVisibleContentsForChannel(broadcastsLimit1, currentUser.getImessageId());
            if(!broadcastsLimit1.isEmpty()) {
                if (!broadcastsLimit1Filter.isEmpty()) {
                    hasMore = true;
                } else {
                    while (broadcastService.getBroadcastPosition(broadcastsLimit1.getLast().getBroadcastId()) > 0) {
                        List<Broadcast> broadcastsLimitSupplement = broadcastService.findBroadcastsLimit(broadcastsLimit1.getLast().getBroadcastId(), 50, channelIds, currentUser.getImessageId(), desc);
                        if(broadcastsLimitSupplement.isEmpty()) break;
                        broadcastsLimit1.addAll(broadcastsLimitSupplement);
                        broadcastsLimitSupplement = broadcastService.filterBroadcastListToVisibleContentsForChannel(broadcastsLimitSupplement, currentUser.getImessageId());
                        if (!broadcastsLimitSupplement.isEmpty()) {
                            hasMore = true;
                            break;
                        }
                    }
                }
            }
        }
        return PaginatedOperationData.paginatedSuccess(broadcastsLimitFiltered, hasMore);
    }

    @PostMapping("edit")
    @Transactional
    public OperationData editBroadcast(@RequestPart("body") @Valid EditBroadcastPostBody postBody,
                                       @RequestPart(value = "add_medias", required = false) List<MultipartFile> addMedias,
                                       HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(postBody.getBroadcastId(), currentUser.getImessageId());
        if(broadcast == null || !broadcast.getImessageId().equals(currentUser.getImessageId())){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-101, "广播不存在");
        }
        boolean textNotChange = Objects.equals(broadcast.getText(), postBody.getNewText());
        List<BroadcastMedia> toDeleteBroadcastMedias = new ArrayList<>();
        List<String> toDeleteBroadcastMediaIds = new ArrayList<>();
        broadcast.getBroadcastMedias().forEach(broadcastMedia -> {
            if (!postBody.getLeftMedias().containsKey(broadcastMedia.getMediaId())) {
                toDeleteBroadcastMedias.add(broadcastMedia);
                toDeleteBroadcastMediaIds.add(broadcastMedia.getMediaId());
            }
        });
        boolean noAddMedias = addMedias == null || addMedias.isEmpty();
        boolean leftMediasPositionNotChange = true;
        OUTER: for (BroadcastMedia broadcastMedia : broadcast.getBroadcastMedias()) {
            for (Map.Entry<String, Integer> stringIntegerEntry : postBody.getLeftMedias().entrySet()) {
                if(broadcastMedia.getIndex() == stringIntegerEntry.getValue()){
                    if(!broadcastMedia.getMediaId().equals(stringIntegerEntry.getKey())){
                        leftMediasPositionNotChange = false;
                        break OUTER;
                    }
                }
            }
        }
        if(textNotChange && toDeleteBroadcastMedias.isEmpty() && noAddMedias && leftMediasPositionNotChange){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-102, "数据未修改");
        }
        if(addMedias != null && (
                (postBody.getAddMediaTypes() != null && postBody.getAddMediaTypes().size() != addMedias.size())
                || (postBody.getAddMediaExtensions() != null && postBody.getAddMediaExtensions().size() != addMedias.size())
                || (postBody.getAddMediaIndexes() != null && postBody.getAddMediaIndexes().size() != addMedias.size())
        )){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new OperationData(-103, "广播媒体不合法");
        }
        for (Integer mediaType : postBody.getAddMediaTypes()) {
            if(!(mediaType.equals(BroadcastMedia.TYPE_IMAGE) || mediaType.equals(BroadcastMedia.TYPE_VIDEO))){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new OperationData(-104, "广播媒体不合法");
            }
        }
        if(addMedias != null){
            int imageCount = 0;
            int videoCount = 0;
            for (BroadcastMedia broadcastMedia : broadcast.getBroadcastMedias()) {
                if(broadcastMedia.getType() == BroadcastMedia.TYPE_IMAGE) {
                    imageCount ++;
                }else if(broadcastMedia.getType() == BroadcastMedia.TYPE_VIDEO){
                    videoCount ++;
                }
            }
            for (Integer mediaType : postBody.getAddMediaTypes()) {
                if(mediaType == BroadcastMedia.TYPE_IMAGE) {
                    imageCount ++;
                }else if(mediaType == BroadcastMedia.TYPE_VIDEO){
                    videoCount ++;
                }
            }
            for (BroadcastMedia toDeleteBroadcastMedia : toDeleteBroadcastMedias) {
                if(toDeleteBroadcastMedia.getType() == BroadcastMedia.TYPE_IMAGE) {
                    imageCount --;
                }else if(toDeleteBroadcastMedia.getType() == BroadcastMedia.TYPE_VIDEO){
                    videoCount --;
                }
            }
            if(imageCount > Constants.MAX_BROADCAST_IMAGE_COUNT || videoCount > Constants.MAX_BROADCAST_VIDEO_COUNT){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new OperationData(-105, "广播图片数量不能超过 " + Constants.MAX_BROADCAST_IMAGE_COUNT
                        + ", 视频数量不能超过 " + Constants.MAX_BROADCAST_VIDEO_COUNT);
            }
            for (MultipartFile media : addMedias) {
                if(media.getSize() > Constants.MAX_BROADCAST_VIDEO_FILE_SIZE_BYTE){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new OperationData(-106, "广播视频文件不能超过 " + Constants.MAX_BROADCAST_VIDEO_FILE_SIZE_BYTE + " Bytes");
                }
            }
        }

        if(!textNotChange){
            if(!broadcastService.updateBroadcastText(postBody.getBroadcastId(), postBody.getNewText())){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationData.failure();
            }
        }
        if(!toDeleteBroadcastMedias.isEmpty()){
            if(!broadcastService.deleteBroadcastMedias(toDeleteBroadcastMediaIds)){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationData.failure();
            }
        }
        if(!noAddMedias){
            try {
                for (int i = 0; i < addMedias.size(); i++) {
                    MultipartFile media = addMedias.get(i);
                    Size size = null;
                    Long videoDuration = null;
                    if(postBody.getAddMediaTypes().get(i).equals(BroadcastMedia.TYPE_IMAGE)){
                        size = MediaUtil.getImageSize(media.getInputStream());
                    }else if(postBody.getAddMediaTypes().get(i).equals(BroadcastMedia.TYPE_VIDEO)){
                        VideoInfo videoInfo = MediaUtil.getVideoInfo(media.getInputStream(), postBody.getAddMediaExtensions().get(i));
                        size = videoInfo.getSize();
                        videoDuration = videoInfo.getDuration();
                    }
                    BroadcastMedia broadcastMedia = new BroadcastMedia(UUID.randomUUID().toString(), postBody.getBroadcastId(),
                            media.getBytes(), postBody.getAddMediaTypes().get(i), postBody.getAddMediaExtensions().get(i),
                            postBody.getAddMediaIndexes().get(i), size, videoDuration);
                    boolean success = broadcastService.insertBroadcastMedia(broadcastMedia);
                    if (!success) {
                        throw new Exception("新增广播媒体插入失败 > index: " + i);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return OperationData.failure();
            }
        }
        if(postBody.getLeftMedias() != null) {
            for (Map.Entry<String, Integer> entry : postBody.getLeftMedias().entrySet()) {
                if (!broadcastService.updateBroadcastMediaIndex(entry.getKey(), entry.getValue())) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return OperationData.failure();
                }
            }
        }

        if(!broadcastService.updateBroadcastLastEditTime(new Date(), broadcast.getBroadcastId())){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return OperationData.failure();
        }

        return OperationData.success(broadcastService.findBroadcast(postBody.getBroadcastId(), currentUser.getImessageId()));
    }

    @PostMapping("like/{broadcastId}")
    public OperationData likeBroadcast(@PathVariable("broadcastId") String broadcastId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(broadcastId, currentUser.getImessageId());
        if(broadcast == null){
            return new OperationData(-101, "广播不存在");
        }
        if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
            return new OperationData(-102, "广播不可见");
        }
        if(broadcastService.findBroadcastLike(broadcastId, currentUser.getImessageId()) != null){
            return new OperationData(-103, "已经喜欢了");
        }
        String likeId = UUID.randomUUID().toString();
        boolean success = broadcastService.insertBroadcastLike(likeId, broadcastId, currentUser.getImessageId(), new Date());
        if(!success){
            return OperationData.failure();
        }
        redisOperationService.BROADCAST.saveNewBroadcastLike(broadcast.getImessageId(), likeId);
        simpMessagingTemplate.convertAndSendToUser(broadcast.getImessageId(), StompDestinations.BROADCASTS_LIKES_UPDATE, "");
        return OperationData.success(broadcastService.findBroadcast(broadcastId, currentUser.getImessageId()));
    }

    @PostMapping("like/cancel/{broadcastId}")
    public OperationData cancelLikeBroadcast(@PathVariable("broadcastId") String broadcastId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(broadcastId, currentUser.getImessageId());
        if(broadcast == null){
            return new OperationData(-101, "广播不存在");
        }
        if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
            return new OperationData(-102, "广播不可见");
        }
        BroadcastLike broadcastLike = broadcastService.findBroadcastLike(broadcastId, currentUser.getImessageId());
        if(broadcastLike == null){
            return new OperationData(-103, "还没有喜欢");
        }
        boolean success = broadcastService.deleteBroadcastLike(broadcastId, currentUser.getImessageId());
        if(!success){
            return OperationData.failure();
        }
        return OperationData.success(broadcastService.findBroadcast(broadcastId, currentUser.getImessageId()));
    }

    @GetMapping("like/to_self/news_count")
    public OperationData getLikeNewsCount(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        int broadcastLikeNewsCount = redisOperationService.BROADCAST.getBroadcastLikeNewsCount(currentUser.getImessageId());
        return OperationData.success(broadcastLikeNewsCount);
    }

    @GetMapping("like/to_self/limit")
    public PaginatedOperationData<BroadcastLike> getLikesOfSelfBroadcastsLimit(@RequestParam(value = "last_like_id", required = false) String lastLikeId, @RequestParam("ps") int ps, HttpSession session){
        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        User currentUser = sessionService.getUserOfSession(session);
        List<BroadcastLike> likesOfChannelBroadcasts = broadcastService.findLikesOfChannelBroadcasts(currentUser.getImessageId(), lastLikeId, ps);
        if(likesOfChannelBroadcasts.isEmpty()) return new PaginatedOperationData<>(-102, "没有内容", null, false);
        boolean hasMore = !broadcastService.findLikesOfChannelBroadcasts(currentUser.getImessageId(), likesOfChannelBroadcasts.getLast().getLikeId(), 1).isEmpty();
        return PaginatedOperationData.paginatedSuccess(likesOfChannelBroadcasts, hasMore);
    }

    @GetMapping("{broadcastId}")
    public OperationData getBroadcastById(@PathVariable("broadcastId") String broadcastId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(broadcastId, currentUser.getImessageId());
        if(broadcast == null){
            return new OperationData(-101, "广播不存在");
        }
        if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
            return new OperationData(-102, "广播不可见");
        }
        return OperationData.success(broadcast);
    }

    @PostMapping("like/to_self/to_old")
    public OperationStatus makeLikesToOld(@Valid @RequestBody MakeBroadcastLikesToOldPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);

        List<String> toRemoveLikeIds = new ArrayList<>();
        for (String likeId : postBody.getLikeIds()) {
            BroadcastLike broadcastLike = broadcastService.findBroadcastLikeById(likeId, true);
            Broadcast broadcast = broadcastService.findBroadcast(broadcastLike.getBroadcastId(), currentUser.getImessageId());
            if (!broadcast.getImessageId().equals(currentUser.getImessageId())) {
                continue;
            }
            toRemoveLikeIds.add(likeId);
        }

        toRemoveLikeIds.forEach(toRemoveLikeId -> {
            redisOperationService.BROADCAST.deleteNewBroadcastLike(currentUser.getImessageId(), toRemoveLikeId);
        });

        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.BROADCASTS_LIKES_UPDATE, "");
        return OperationStatus.success();
    }

    @GetMapping("like/limit")
    public PaginatedOperationData<BroadcastLike> getLikesLimit(@RequestParam(value = "last_like_id", required = false) String lastLikeId, @RequestParam("ps") int ps, @RequestParam("broadcast_id") String broadcastId, HttpSession session){
        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(broadcastId, currentUser.getImessageId());
        if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
            return new PaginatedOperationData<>(-102, "广播不可见", null, false);
        }

        List<BroadcastLike> likesOfBroadcast = broadcastService.findLikesOfBroadcast(broadcastId, lastLikeId, ps, false);
        if(likesOfBroadcast.isEmpty()) return new PaginatedOperationData<>(-103, "没有内容", null, false);
        boolean hasMore = !broadcastService.findLikesOfBroadcast(broadcastId, likesOfBroadcast.getLast().getLikeId(), 1, false).isEmpty();
        return PaginatedOperationData.paginatedSuccess(likesOfBroadcast, hasMore);
    }

    @PostMapping("comment")
    public OperationData commentBroadcast(@Valid @RequestBody CommentBroadcastPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(postBody.getBroadcastId(), currentUser.getImessageId());
        if(broadcast == null){
            return new OperationData(-101, "广播不存在");
        }
        if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
            return new OperationData(-102, "广播不可见");
        }
        String commentId = UUID.randomUUID().toString();
        boolean success = broadcastService.insertBroadcastComment(commentId, postBody.getBroadcastId(), currentUser.getImessageId(), postBody.getText(), postBody.getToCommentId(), new Date());
        if(!success){
            return OperationData.failure();
        }
        redisOperationService.BROADCAST.saveNewBroadcastComment(broadcast.getImessageId(), commentId);
        simpMessagingTemplate.convertAndSendToUser(broadcast.getImessageId(), StompDestinations.BROADCASTS_COMMENTS_UPDATE, "");
        if(postBody.getToCommentId() != null) {
            BroadcastComment toComment = broadcastService.findBroadcastCommentById(postBody.getToCommentId(), true);
            redisOperationService.BROADCAST.saveNewBroadcastReply(toComment.getFromId(), commentId);
            simpMessagingTemplate.convertAndSendToUser(toComment.getFromId(), StompDestinations.BROADCASTS_REPLIES_UPDATE, "");
        }
        return OperationData.success(broadcastService.findBroadcast(postBody.getBroadcastId(), currentUser.getImessageId()));
    }

    @GetMapping("comment/limit")
    public PaginatedOperationData<BroadcastComment> getCommentsLimit(@RequestParam(value = "last_comment_id", required = false) String lastCommentId, @RequestParam("ps") int ps, @RequestParam("broadcast_id") String broadcastId, HttpSession session){
        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        User currentUser = sessionService.getUserOfSession(session);
        Broadcast broadcast = broadcastService.findBroadcast(broadcastId, currentUser.getImessageId());
        if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
            return new PaginatedOperationData<>(-102, "广播不可见", null, false);
        }

        List<BroadcastComment> commentsOfBroadcast = broadcastService.findCommentsOfBroadcast(broadcastId, lastCommentId, ps, false);
        if(commentsOfBroadcast.isEmpty()) return new PaginatedOperationData<>(-103, "没有内容", null, false);
        boolean hasMore = !broadcastService.findCommentsOfBroadcast(broadcastId, commentsOfBroadcast.getLast().getCommentId(), 1, false).isEmpty();
        return PaginatedOperationData.paginatedSuccess(commentsOfBroadcast, hasMore);
    }

    @GetMapping("comment/to_self/news_count")
    public OperationData getCommentNewsCount(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        int broadcastCommentNewsCount = redisOperationService.BROADCAST.getBroadcastCommentNewsCount(currentUser.getImessageId());
        return OperationData.success(broadcastCommentNewsCount);
    }

    @GetMapping("comment/to_self/limit")
    public PaginatedOperationData<BroadcastComment> getCommentsOfSelfBroadcastsLimit(@RequestParam(value = "last_comment_id", required = false) String lastCommentId, @RequestParam("ps") int ps, HttpSession session){
        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        User currentUser = sessionService.getUserOfSession(session);
        List<BroadcastComment> commentsOfChannelBroadcasts = broadcastService.findCommentsOfChannelBroadcasts(currentUser.getImessageId(), lastCommentId, ps);
        if(commentsOfChannelBroadcasts.isEmpty()) return new PaginatedOperationData<>(-102, "没有内容", null, false);
        boolean hasMore = !broadcastService.findCommentsOfChannelBroadcasts(currentUser.getImessageId(), commentsOfChannelBroadcasts.getLast().getCommentId(), 1).isEmpty();
        return PaginatedOperationData.paginatedSuccess(commentsOfChannelBroadcasts, hasMore);
    }

    @PostMapping("comment/to_self/to_old")
    public OperationStatus makeCommentsToOld(@Valid @RequestBody MakeBroadcastCommentsToOldPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);

        List<String> toRemoveCommentIds = new ArrayList<>();
        for (String commentId : postBody.getCommentIds()) {
            BroadcastComment broadcastComment = broadcastService.findBroadcastCommentById(commentId, true);
            Broadcast broadcast = broadcastService.findBroadcast(broadcastComment.getBroadcastId(), currentUser.getImessageId());
            if (!broadcast.getImessageId().equals(currentUser.getImessageId())) {
                continue;
            }
            toRemoveCommentIds.add(commentId);
        }

        toRemoveCommentIds.forEach(toRemoveCommentId -> {
            redisOperationService.BROADCAST.deleteNewBroadcastComment(currentUser.getImessageId(), toRemoveCommentId);
        });

        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.BROADCASTS_COMMENTS_UPDATE, "");
        return OperationStatus.success();
    }

    @PostMapping("comment/delete/{commentId}")
    public OperationData deleteBroadcastComment(@PathVariable("commentId") String commentId, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        BroadcastComment broadcastComment = broadcastService.findBroadcastCommentById(commentId, false);
        if(broadcastComment == null){
            return new OperationData(-101, "评论不存在");
        }
        if(broadcastComment.getFromId().equals(currentUser.getImessageId())){
            Broadcast broadcast = broadcastService.findBroadcast(broadcastComment.getBroadcastId(), currentUser.getImessageId());
            if(broadcast == null){
                return new OperationData(-102, "广播不存在");
            }
            if(!broadcastService.determineBroadcastVisibility(broadcast, currentUser.getImessageId())){
                return new OperationData(-103, "广播不可见");
            }
            if(broadcastService.deleteBroadcastComment(commentId, currentUser.getImessageId())){
                return OperationData.success(broadcastService.findBroadcast(broadcast.getBroadcastId(), currentUser.getImessageId()));
            }
        }
        return OperationData.failure();
    }

    @GetMapping("reply_comment/to_self/news_count")
    public OperationData getReplyCommentNewsCount(HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);
        int broadcastReplyNewsCount = redisOperationService.BROADCAST.getBroadcastReplyNewsCount(currentUser.getImessageId());
        return OperationData.success(broadcastReplyNewsCount);
    }

    @GetMapping("reply_comment/to_self/limit")
    public PaginatedOperationData<BroadcastComment> getReplyCommentsOfSelfBroadcastsLimit(@RequestParam(value = "last_reply_comment_id", required = false) String lastReplyCommentId, @RequestParam("ps") int ps, HttpSession session){
        if (ps < 1 || ps > 50) {
            return new PaginatedOperationData<>(-101, "每页最多 50 个项目，最少 1 个项目", null, false);
        }

        User currentUser = sessionService.getUserOfSession(session);
        List<BroadcastComment> replyCommentsOfChannelBroadcast = broadcastService.findReplyCommentsOfChannelBroadcast(currentUser.getImessageId(), lastReplyCommentId, ps);
        if(replyCommentsOfChannelBroadcast.isEmpty()) return new PaginatedOperationData<>(-102, "没有内容", null, false);
        boolean hasMore = !broadcastService.findReplyCommentsOfChannelBroadcast(currentUser.getImessageId(), replyCommentsOfChannelBroadcast.getLast().getCommentId(), 1).isEmpty();
        return PaginatedOperationData.paginatedSuccess(replyCommentsOfChannelBroadcast, hasMore);
    }

    @PostMapping("reply_comment/to_self/to_old")
    public OperationStatus makeReplyCommentsToOld(@Valid @RequestBody MakeBroadcastReplyCommentsToOldPostBody postBody, HttpSession session){
        User currentUser = sessionService.getUserOfSession(session);

        List<String> toRemoveReplyCommentIds = new ArrayList<>();
        for (String replyCommentId : postBody.getReplyCommentIds()) {
            BroadcastComment broadcastComment = broadcastService.findBroadcastCommentById(replyCommentId, true);
            if (!broadcastComment.getFromId().equals(currentUser.getImessageId())) {
                continue;
            }
            toRemoveReplyCommentIds.add(replyCommentId);
        }

        toRemoveReplyCommentIds.forEach(toRemoveReplyCommentId -> {
            redisOperationService.BROADCAST.deleteNewBroadcastReply(currentUser.getImessageId(), toRemoveReplyCommentId);
        });

        simpMessagingTemplate.convertAndSendToUser(currentUser.getImessageId(), StompDestinations.BROADCASTS_REPLIES_UPDATE, "");
        return OperationStatus.success();
    }
}

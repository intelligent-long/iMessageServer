package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.*;
import com.longx.intelligent.app.imessage.server.util.Base64Util;
import com.longx.intelligent.app.imessage.server.util.JsonUtil;
import com.longx.intelligent.app.imessage.server.util.RedisOperator;
import com.longx.intelligent.app.imessage.server.value.Constants;
import com.longx.intelligent.app.imessage.server.value.RedisKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by LONG on 2024/3/30 at 12:59 AM.
 */
@Service
public class RedisOperationService {
    @Autowired
    private RedisOperator redisOperator;
    @Autowired
    private UserService userService;
    @Autowired
    @Lazy
    private ChannelService channelService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    @Lazy
    private GroupChannelService groupChannelService;

    public final Auth AUTH = new Auth();
    public final VerifyCode VERIFY_CODE = new VerifyCode();
    public final ChannelAddition CHANNEL_ADDITION = new ChannelAddition();
    public final Chat CHAT = new Chat();
    public final Broadcast BROADCAST = new Broadcast();
    public final GroupChannelAddition GROUP_CHANNEL_ADDITION = new GroupChannelAddition();
    public final GroupChannelNotification GROUP_CHANNEL_NOTIFICATION = new GroupChannelNotification();
    public final GroupChat GROUP_CHAT = new GroupChat();

    public class Auth {
        public void incrementLoginFailureTimes(String imessageId){
            String key = RedisKeys.Auth.failureTimes(imessageId);
            if(redisOperator.exists(key)){
                redisOperator.increment(key);
            }else {
                redisOperator.setWithExpiration(key, 1, RedisKeys.Auth.EXPIRE_HOURS_FAILURE_TIMES, TimeUnit.HOURS);
            }
        }

        public void removeLoginFailureTimes(String imessageId){
            String key = RedisKeys.Auth.failureTimes(imessageId);
            redisOperator.delete(key);
        }

        public int getLoginFailureTimes(String imessageId){
            String key = RedisKeys.Auth.failureTimes(imessageId);
            if(!redisOperator.exists(key)) return -1;
            return Integer.parseInt(String.valueOf(redisOperator.get(key)));
        }

        public void recordOfflineDetail(String sessionId, OfflineDetail offlineDetail){
            String key = RedisKeys.Auth.offlineDetail(sessionId);
            redisOperator.set(key, JsonUtil.toJson(offlineDetail));
        }

        public OfflineDetail getOfflineDetail(String sessionId){
            String key = RedisKeys.Auth.offlineDetail(sessionId);
            String json = (String) redisOperator.get(key);
            if(json == null) return null;
            return JsonUtil.toObject(json, OfflineDetail.class);
        }

        public void removeOfflineDetail(String sessionId){
            String key = RedisKeys.Auth.offlineDetail(sessionId);
            redisOperator.delete(key);
        }
    }

    public class VerifyCode{
        public boolean checkVerifyCodeAvailability(String email){
            String key = RedisKeys.VerifyCode.sendLimit(email);
            return !redisOperator.exists(key);
        }

        public void enterVerifyCodeValidityPeriod(String email, String code){
            String verifyCodeKey = RedisKeys.VerifyCode.verifyCode(email);
            String verifyCodeSendLimitKey = RedisKeys.VerifyCode.sendLimit(email);
            String verifyCodeFailureTimesKey = RedisKeys.VerifyCode.failureTimes(email);
            redisOperator.setWithExpiration(verifyCodeKey, code, RedisKeys.VerifyCode.EXPIRE_MINUTES_VERIFY_CODE, TimeUnit.MINUTES);
            redisOperator.setWithExpiration(verifyCodeSendLimitKey, null, RedisKeys.VerifyCode.EXPIRE_MINUTES_SEND_LIMIT, TimeUnit.MINUTES);
            redisOperator.setWithExpiration(verifyCodeFailureTimesKey, 0, RedisKeys.VerifyCode.EXPIRE_MINUTES_VERIFY_CODE, TimeUnit.MINUTES);
        }

        public void finishVerification(String email){
            String verifyCodeKey = RedisKeys.VerifyCode.verifyCode(email);
            String verifyCodeSendLimitKey = RedisKeys.VerifyCode.sendLimit(email);
            String verifyCodeFailureTimesKey = RedisKeys.VerifyCode.failureTimes(email);
            redisOperator.delete(verifyCodeKey);
            redisOperator.delete(verifyCodeSendLimitKey);
            redisOperator.delete(verifyCodeFailureTimesKey);
        }

        public boolean checkMaxVerifyFailureTimesReached(String email){
            String key = RedisKeys.VerifyCode.failureTimes(email);
            return redisOperator.exists(key) && (int)redisOperator.get(key) > Constants.VERIFY_CODE_MAX_FAILURE_TIMES;
        }

        public boolean checkVerifyCodeCorrect(String email, String code){
            String key = RedisKeys.VerifyCode.verifyCode(email);
            String codeInRedis = (String) redisOperator.get(key);
            return codeInRedis != null && codeInRedis.equals(code);
        }

        public void incrementVerifyCodeFailureTimes(String email){
            String key = RedisKeys.VerifyCode.failureTimes(email);
            if(redisOperator.exists(key)){
                redisOperator.increment(key);
            }else {
                redisOperator.setWithExpiration(key, 1, RedisKeys.VerifyCode.EXPIRE_MINUTES_VERIFY_CODE, TimeUnit.MINUTES);
            }
        }
    }

    public class ChannelAddition {

        public void saveRequester(com.longx.intelligent.app.imessage.server.data.ChannelAddition channelAddition) {
            String key = RedisKeys.ChannelAddition.requester(channelAddition.requesterChannel().getImessageId(),
                    channelAddition.responderChannel().getImessageId(), channelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.UUID, channelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.MESSAGE, channelAddition.message());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.NOTE, channelAddition.note());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.NEW_TAG_NAMES, channelAddition.newTagNames());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.TO_ADD_TAG_IDS, channelAddition.toAddTagIds());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.REQUEST_TIME, channelAddition.requestTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME, channelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED, channelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED, channelAddition.isViewed());
            redisOperator.expireForHash(key, Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public void saveResponder(com.longx.intelligent.app.imessage.server.data.ChannelAddition channelAddition) {
            String key = RedisKeys.ChannelAddition.responder(channelAddition.responderChannel().getImessageId(),
                    channelAddition.requesterChannel().getImessageId(), channelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.UUID, channelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.MESSAGE, channelAddition.message());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.NOTE, channelAddition.note());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.NEW_TAG_NAMES, channelAddition.newTagNames());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.TO_ADD_TAG_IDS, channelAddition.toAddTagIds());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.REQUEST_TIME, channelAddition.requestTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.RESPOND_TIME, channelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_ACCEPTED, channelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED, channelAddition.isViewed());
            redisOperator.expireForHash(key, Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public void acceptChangeRequester(com.longx.intelligent.app.imessage.server.data.ChannelAddition channelAddition){
            String key = RedisKeys.ChannelAddition.requester(channelAddition.requesterChannel().getImessageId(),
                    channelAddition.responderChannel().getImessageId(), channelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME, channelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED, channelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED, channelAddition.isViewed());
        }

        public void acceptChangeResponder(com.longx.intelligent.app.imessage.server.data.ChannelAddition channelAddition){
            String key = RedisKeys.ChannelAddition.responder(channelAddition.responderChannel().getImessageId(),
                    channelAddition.requesterChannel().getImessageId(), channelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.RESPOND_TIME, channelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_ACCEPTED, channelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED, channelAddition.isViewed());
        }

        public boolean isInAdding(String requesterImessageId, String responderImessageId){
            String requesterKeyPrefix = RedisKeys.ChannelAddition.requesterPrefix(requesterImessageId, responderImessageId);
            String responderKeyPrefix = RedisKeys.ChannelAddition.responderPrefix(responderImessageId, requesterImessageId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");
            Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");
            boolean booleanPart1 = false;
            for (String requesterKey : requesterKeys) {
                if(redisOperator.exists(requesterKey) && !isExpired(requesterKey)
                        && (Objects.equals(redisOperator.hGet(requesterKey, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED), Boolean.FALSE)
                            || (Objects.equals(redisOperator.hGet(requesterKey, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED), null)))){
                    booleanPart1 = true;
                    break;
                }
            }
            boolean booleanPart2 = false;
            for (String responderKey : responderKeys) {
                if(redisOperator.exists(responderKey) && !isExpired(responderKey)){
                    booleanPart2 = true;
                    break;
                }
            }
            return booleanPart1 && booleanPart2;
        }

        public boolean isExpired(String key){
            return !(redisOperator.getExpire(key, TimeUnit.DAYS) > Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY - Constants.CHANNEL_ADDITION_ADDITION_DURATION_DAY);
        }

        public ChannelAdditionNotViewedCount getChannelAdditionNotViewedCount(String currentUserImessageId){
            AtomicInteger resultRequester = new AtomicInteger();
            AtomicInteger resultResponder = new AtomicInteger();
            AtomicInteger resultNotificationRequest = new AtomicInteger();
            AtomicInteger resultNotificationRespond = new AtomicInteger();
            String requesterKeyPrefix = RedisKeys.ChannelAddition.requesterPrefix(currentUserImessageId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");
            requesterKeys.forEach(key -> {
                boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED);
                if(!isViewed) {
                    resultRequester.getAndIncrement();
                    Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME);
                    if (respondTimeLong != null) {
                        resultNotificationRespond.getAndIncrement();
                    }
                }
            });
            String responderKeyPrefix = RedisKeys.ChannelAddition.responderPrefix(currentUserImessageId);
            Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");
            responderKeys.forEach(key -> {
                boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED);
                if(!isViewed) {
                    resultResponder.getAndIncrement();
                    Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME);
                    if (respondTimeLong == null) {
                        resultNotificationRequest.getAndIncrement();
                    }
                }
            });
            return new ChannelAdditionNotViewedCount(resultRequester.get(), resultResponder.get(), resultNotificationRequest.get(), resultNotificationRespond.get());
        }

        public List<com.longx.intelligent.app.imessage.server.data.ChannelAddition> getAllChannelAddition(String imessageId, HttpSession session) {
            List<com.longx.intelligent.app.imessage.server.data.ChannelAddition> results = new ArrayList<>();
            String requesterKeyPrefix = RedisKeys.ChannelAddition.requesterPrefix(imessageId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");

            Map<String, Map<String, Object>> requesterDataMap = batchGetRedisData(requesterKeys);

            requesterKeys.parallelStream().forEach(key -> {
                String replace = key.replace(requesterKeyPrefix, "");
                String responderImessageId = replace.substring(0, replace.indexOf(':'));
                Map<String, Object> dataMap = requesterDataMap.get(key);

                String uuid = (String) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.UUID);
                String message = (String) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.MESSAGE);
                String note = (String) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.NOTE);
                List<String> newTagNames = (List<String>) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.NEW_TAG_NAMES);
                List<String> toAddTagIds = (List<String>) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.TO_ADD_TAG_IDS);
                Date requestTime = new Date((Long) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.REQUEST_TIME));
                Long respondTimeLong = (Long) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME);
                Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                boolean isAccepted = (boolean) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED);
                boolean isViewed = (boolean) dataMap.get(RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED);

                Channel requesterChannelInfo = channelService.findChannelByImessageId(imessageId, session);
                Channel responderChannelInfo = channelService.findChannelByImessageId(responderImessageId, session);
                boolean isExpired = isExpired(key);

                results.add(new com.longx.intelligent.app.imessage.server.data.ChannelAddition(uuid, requesterChannelInfo,
                        responderChannelInfo, message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired));
            });

            String responderKeyPrefix = RedisKeys.ChannelAddition.responderPrefix(imessageId);
            Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");

            Map<String, Map<String, Object>> responderDataMap = batchGetRedisData(responderKeys);

            responderKeys.parallelStream().forEach(key -> {
                String replace = key.replace(responderKeyPrefix, "");
                String requesterImessageId = replace.substring(0, replace.indexOf(':'));
                Map<String, Object> dataMap = responderDataMap.get(key);

                String uuid = (String) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.UUID);
                String message = (String) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.MESSAGE);
                String note = (String) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.NOTE);
                List<String> newTagNames = (List<String>) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.NEW_TAG_NAMES);
                List<String> toAddTagIds = (List<String>) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.TO_ADD_TAG_IDS);
                Date requestTime = new Date((Long) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.REQUEST_TIME));
                Long respondTimeLong = (Long) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.RESPOND_TIME);
                Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                boolean isAccepted = (boolean) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.IS_ACCEPTED);
                boolean isViewed = (boolean) dataMap.get(RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED);

                Channel requesterChannelInfo = channelService.findChannelByImessageId(requesterImessageId, session);
                Channel responderChannelInfo = channelService.findChannelByImessageId(imessageId, session);
                boolean isExpired = isExpired(key);

                results.add(new com.longx.intelligent.app.imessage.server.data.ChannelAddition(uuid, requesterChannelInfo,
                        responderChannelInfo, message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired));
            });

            return results;
        }

        private Map<String, Map<String, Object>> batchGetRedisData(Set<String> keys) {
            Map<String, Map<String, Object>> dataMap = new HashMap<>();
            for (String key : keys) {
                Map<String, Object> map = new HashMap<>();
                map.put(RedisKeys.ChannelAddition.RequesterHashKey.UUID, redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.UUID));
                map.put(RedisKeys.ChannelAddition.RequesterHashKey.MESSAGE, redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.MESSAGE));
                map.put(RedisKeys.ChannelAddition.RequesterHashKey.REQUEST_TIME, redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.REQUEST_TIME));
                map.put(RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME, redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME));
                map.put(RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED, redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED));
                map.put(RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED, redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED));
                dataMap.put(key, map);
            }
            return dataMap;
        }


        public com.longx.intelligent.app.imessage.server.data.ChannelAddition getRequesterChannelAdditionByUuid(String uuid, HttpSession session){
            Set<String> keys = redisOperator.stringKeys("*" + uuid + "*");
            String key = null;
            for (String k : keys) {
                if(k.contains("requester")){
                    key = k;
                    break;
                }
            }
            if(key == null) return null;
            String[] split = key.split(":");
            String requesterImessageId = split[2];
            String responderImessageId = split[3];
            String message = (String) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.MESSAGE);
            String note = (String) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.NOTE);
            List<String> newTagNames = (List<String>) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.NEW_TAG_NAMES);
            List<String> toAddTagIds = (List<String>) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.TO_ADD_TAG_IDS);
            Date requestTime = new Date((Long) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.REQUEST_TIME));
            Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.RESPOND_TIME);
            Date respondTime = respondTimeLong == null ? null: new Date(respondTimeLong);
            boolean isAccepted = (boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED);
            boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED);
            Channel requesterChannelInfo = channelService.findChannelByImessageId(requesterImessageId, session);
            Channel responderChannelInfo = channelService.findChannelByImessageId(responderImessageId, session);
            boolean isExpired = isExpired(key);
            return new com.longx.intelligent.app.imessage.server.data.ChannelAddition(uuid, requesterChannelInfo, responderChannelInfo,
                    message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired);
        }

        public com.longx.intelligent.app.imessage.server.data.ChannelAddition getResponderChannelAdditionByUuid(String uuid, HttpSession session){
            Set<String> keys = redisOperator.keys("*" + uuid +"*");
            String key = null;
            for (String k : keys) {
                if(k.contains("responder")){
                    key = k;
                    break;
                }
            }
            if(key == null) return null;
            String[] split = key.split(":");
            String responderImessageId = split[2];
            String requesterImessageId = split[3];
            String message = (String) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.MESSAGE);
            String note = (String) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.NOTE);
            List<String> newTagNames = (List<String>) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.NEW_TAG_NAMES);
            List<String> toAddTagIds = (List<String>) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.TO_ADD_TAG_IDS);
            Date requestTime = new Date((Long) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.REQUEST_TIME));
            Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.RESPOND_TIME);
            Date respondTime = respondTimeLong == null ? null: new Date(respondTimeLong);
            boolean isAccepted = (boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_ACCEPTED);
            boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED);
            Channel requesterChannelInfo = channelService.findChannelByImessageId(requesterImessageId, session);
            Channel responderChannelInfo = channelService.findChannelByImessageId(responderImessageId, session);
            boolean isExpired = isExpired(key);
            return new com.longx.intelligent.app.imessage.server.data.ChannelAddition(uuid, requesterChannelInfo, responderChannelInfo,
                    message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired);
        }

        public void setAllToViewed(String imessageId){
            String requesterKeyPrefix = RedisKeys.ChannelAddition.requesterPrefix(imessageId);
            String responderKeyPrefix = RedisKeys.ChannelAddition.responderPrefix(imessageId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");
            requesterKeys.forEach(key -> {
                redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED, true);
            });
            Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");
            responderKeys.forEach(key -> {
                redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED, true);
            });
        }

        public boolean setOneToViewed(String currentUserImessageId, String uuid){
            Set<String> keys = redisOperator.keys("*" + uuid + "*");
            AtomicBoolean failed = new AtomicBoolean(false);
            AtomicBoolean allNotMatch = new AtomicBoolean(true);
            keys.forEach(key -> {
                String[] parts = key.split(":");
                String imessageId1 = parts[2];
                if(imessageId1.equals(currentUserImessageId)){
                    allNotMatch.set(false);
                    Boolean isViewed = true;
                    if(key.contains("requester")) {
                        isViewed = (Boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED);
                    }else if(key.contains("responder")){
                        isViewed = (Boolean) redisOperator.hGet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED);
                    }
                    if(isViewed){
                        failed.set(true);
                    }
                    if(key.contains("requester")) {
                        redisOperator.hSet(key, RedisKeys.ChannelAddition.RequesterHashKey.IS_VIEWED, true);
                    }else if(key.contains("responder")){
                        redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED, true);
                    }
                }
            });
            if(allNotMatch.get()){
                failed.set(true);
            }
            return !failed.get();
        }
    }

    public class Chat{
        public void saveChatMessage(ChatMessage chatMessage){
            String messageRedisKey = RedisKeys.Chat.getChatMessage(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getUuid());
            redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.TYPE, String.valueOf(chatMessage.getType()));
            redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.UUID, chatMessage.getUuid());
            redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.FROM, chatMessage.getFrom());
            redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.TO, chatMessage.getTo());
            redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.TIME, chatMessage.getTime());
            switch (chatMessage.getType()) {
                case ChatMessage.TYPE_TEXT -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.TEXT, chatMessage.getText());
                }
                case ChatMessage.TYPE_VOICE -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.VOICE_ID, chatMessage.getVoiceId());
                }
                case ChatMessage.TYPE_IMAGE -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.FILE_NAME, chatMessage.getFileName());
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.IMAGE_ID, chatMessage.getImageId());
                }
                case ChatMessage.TYPE_VIDEO -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.FILE_NAME, chatMessage.getFileName());
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.VIDEO_ID, chatMessage.getVideoId());
                }
                case ChatMessage.TYPE_FILE -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.FILE_NAME, chatMessage.getFileName());
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.FILE_ID, chatMessage.getFileId());
                }
                case ChatMessage.TYPE_NOTICE -> {

                }
                case ChatMessage.TYPE_UNSEND -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.UNSEND_MESSAGE_UUID, chatMessage.getUnsendMessageUuid());
                }
                case ChatMessage.TYPE_MESSAGE_EXPIRED -> {
                    redisOperator.hSet(messageRedisKey, RedisKeys.Chat.ChatMessageHashKey.EXPIRED_MESSAGE_COUNT, chatMessage.getExpiredMessageCount());
                    return;
                }
            }
            redisOperator.expireForHash(messageRedisKey, Constants.CHAT_MESSAGE_EXPIRATION_TIME_DAY, TimeUnit.DAYS);
        }

        public void saveChatMessageImage(ChatMessage chatMessage, byte[] image){
            String chatMessageImageKey = RedisKeys.Chat.getChatMessageImage(chatMessage.getImageId());
            redisOperator.hSet(chatMessageImageKey, RedisKeys.Chat.ChatMessageImageHashKey.IMAGE, Base64Util.encodeToString(image));
            redisOperator.hSet(chatMessageImageKey, RedisKeys.Chat.ChatMessageImageHashKey.IMAGE_ID, chatMessage.getImageId());
            redisOperator.hSet(chatMessageImageKey, RedisKeys.Chat.ChatMessageImageHashKey.IMAGE_FILE_NAME, chatMessage.getFileName());
        }

        public void saveChatMessageFile(ChatMessage chatMessage, byte[] file){
            String chatMessageFileKey = RedisKeys.Chat.getChatMessageFile(chatMessage.getFileId());
            redisOperator.hSet(chatMessageFileKey, RedisKeys.Chat.ChatMessageFileHashKey.FILE, Base64Util.encodeToString(file));
            redisOperator.hSet(chatMessageFileKey, RedisKeys.Chat.ChatMessageFileHashKey.FILE_ID, chatMessage.getFileId());
            redisOperator.hSet(chatMessageFileKey, RedisKeys.Chat.ChatMessageFileHashKey.FILE_FILE_NAME, chatMessage.getFileName());
        }

        public void saveChatMessageVideo(ChatMessage chatMessage, byte[] video){
            String chatMessageVideoKey = RedisKeys.Chat.getChatMessageVideo(chatMessage.getVideoId());
            redisOperator.hSet(chatMessageVideoKey, RedisKeys.Chat.ChatMessageVideoHashKey.VIDEO, Base64Util.encodeToString(video));
            redisOperator.hSet(chatMessageVideoKey, RedisKeys.Chat.ChatMessageVideoHashKey.VIDEO_ID, chatMessage.getVideoId());
            redisOperator.hSet(chatMessageVideoKey, RedisKeys.Chat.ChatMessageVideoHashKey.VIDEO_FILE_NAME, chatMessage.getFileName());
        }

        public void saveChatMessageVoice(ChatMessage chatMessage, byte[] voice){
            String chatMessageVoiceKey = RedisKeys.Chat.getChatMessageVoice(chatMessage.getVoiceId());
            redisOperator.hSet(chatMessageVoiceKey, RedisKeys.Chat.ChatMessageVoiceHashKey.VOICE_ID, chatMessage.getVoiceId());
            redisOperator.hSet(chatMessageVoiceKey, RedisKeys.Chat.ChatMessageVoiceHashKey.VOICE, Base64Util.encodeToString(voice));
        }

        public void deleteChatMessage(String receiver, String from, String uuid){
            String messageRedisKey = RedisKeys.Chat.getChatMessage(receiver, from, uuid);
            doOtherWhenDeleteMessage(messageRedisKey);
            redisOperator.delete(messageRedisKey);
        }

        public ChatMessage getChatMessage(String receiver, String from, String uuid){
            String key = RedisKeys.Chat.getChatMessage(receiver, from, uuid);
            int type = Integer.parseInt(redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TYPE).toString());
            String uuidGet = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.UUID);
            String fromFound = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FROM);
            String to = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TO);
            Date time = new Date((Long) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TIME));
            String text = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TEXT);
            String extension = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FILE_NAME);
            String imageId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.IMAGE_ID);
            String fileId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FILE_ID);
            String videoId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.VIDEO_ID);
            String voiceId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.VOICE_ID);
            String unsendMessageUuid = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.UNSEND_MESSAGE_UUID);
            Integer expiredMessageCount = (Integer) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.EXPIRED_MESSAGE_COUNT);
            return new ChatMessage(type, uuidGet, fromFound, to, time, text, extension, imageId, fileId, videoId, voiceId, unsendMessageUuid, expiredMessageCount);
        }

        public List<ChatMessage> getAllChatMessage(String currentUserImessageId){
            List<ChatMessage> result = new ArrayList<>();
            String messagePattern = RedisKeys.Chat.getChatMessagePatternReceiver(currentUserImessageId);
            Set<String> keys = redisOperator.keys(messagePattern);
            keys.forEach(key -> {
                int type = Integer.parseInt(redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TYPE).toString());
                String uuid = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.UUID);
                String from = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FROM);
                String to = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TO);
                Date time = new Date((Long) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TIME));
                String text = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TEXT);
                String extension = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FILE_NAME);
                String imageId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.IMAGE_ID);
                String fileId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FILE_ID);
                String videoId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.VIDEO_ID);
                String voiceId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.VOICE_ID);
                String unsendMessageUuid = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.UNSEND_MESSAGE_UUID);
                Integer expiredMessageCount = (Integer) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.EXPIRED_MESSAGE_COUNT);
                result.add(new ChatMessage(type, uuid, from, to, time, text, extension, imageId, fileId, videoId, voiceId, unsendMessageUuid, expiredMessageCount));
            });
            return result;
        }

        public Object[] getChatMessageImage(String imageId){
            String key = RedisKeys.Chat.getChatMessageImage(imageId);
            byte[] image = Base64Util.decodeFromString((String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageImageHashKey.IMAGE));
            String imageIdRedis = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageImageHashKey.IMAGE_ID);
            String imageFileName = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageImageHashKey.IMAGE_FILE_NAME);
            return new Object[]{image, imageIdRedis, imageFileName};
        }

        public Object[] getChatMessageFile(String fileId){
            String key = RedisKeys.Chat.getChatMessageFile(fileId);
            byte[] file = Base64Util.decodeFromString((String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageFileHashKey.FILE));
            String fileIdRedis = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageFileHashKey.FILE_ID);
            String fileFileName = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageFileHashKey.FILE_FILE_NAME);
            return new Object[]{file, fileIdRedis, fileFileName};
        }

        public Object[] getChatMessageVideo(String videoId){
            String key = RedisKeys.Chat.getChatMessageVideo(videoId);
            byte[] video = Base64Util.decodeFromString((String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageVideoHashKey.VIDEO));
            String videoIdRedis = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageVideoHashKey.VIDEO_ID);
            String videoFileName = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageVideoHashKey.VIDEO_FILE_NAME);
            return new Object[]{video, videoIdRedis, videoFileName};
        }

        public Object[] getChatMessageVoice(String voiceId){
            String key = RedisKeys.Chat.getChatMessageVoice(voiceId);
            byte[] voice = Base64Util.decodeFromString((String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageVoiceHashKey.VOICE));
            String voiceIdRedis = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageVoiceHashKey.VOICE_ID);
            return new Object[]{voice, voiceIdRedis};
        }

        public MessageViewed viewMessage(String currentUserImessageId, String messageUuid){
            String messagePatternUuid= RedisKeys.Chat.getChatMessagePatternUuid(currentUserImessageId, messageUuid);
            String messageKey = redisOperator.keys(messagePatternUuid).iterator().next();
            doOtherWhenDeleteMessage(messageKey);
            String other = null;
            String fromFound = (String) redisOperator.hGet(messageKey, RedisKeys.Chat.ChatMessageHashKey.FROM);
            String to = (String) redisOperator.hGet(messageKey, RedisKeys.Chat.ChatMessageHashKey.TO);
            if(currentUserImessageId.equals(fromFound)){
                other = to;
            }else if(currentUserImessageId.equals(to)){
                other = fromFound;
            }
            redisOperator.delete(messageKey);
            String messagePatternReceiver = RedisKeys.Chat.getChatMessagePatternReceiver(currentUserImessageId);
            int totalNotViewedCount = redisOperator.keys(messagePatternReceiver).size();
            String messagePatternReceiverFrom = RedisKeys.Chat.getChatMessagePatternReceiverFrom(currentUserImessageId, fromFound);
            int currentNotViewedCount = redisOperator.keys(messagePatternReceiverFrom).size();
            return new MessageViewed(totalNotViewedCount, currentNotViewedCount, messageUuid, other);
        }

        public void viewAllMessage(String currentUserImessageId, String other){
            String messagePattern = RedisKeys.Chat.getChatMessagePatternReceiver(currentUserImessageId);
            Set<String> keys = redisOperator.keys(messagePattern);
            for (String key : keys) {
                String thisOther = null;
                String from = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FROM);
                String to = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TO);
                if(currentUserImessageId.equals(from)){
                    thisOther = to;
                }else if(currentUserImessageId.equals(to)){
                    thisOther = from;
                }
                if(thisOther.equals(other)) {
                        doOtherWhenDeleteMessage(key);
                        redisOperator.delete(key);
                }
            }
        }

        private boolean doOtherWhenDeleteMessage(String key) {
            Object typeStr = redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.TYPE);
            if(typeStr == null) return false;
            int type = Integer.parseInt(typeStr.toString());
            switch (type){
                case ChatMessage.TYPE_IMAGE -> {
                    String imageId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.IMAGE_ID);
                    String chatMessageImageKey = RedisKeys.Chat.getChatMessageImage(imageId);
                    redisOperator.delete(chatMessageImageKey);
                }
                case ChatMessage.TYPE_VIDEO -> {
                    String videoId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.VIDEO_ID);
                    String chatMessageVideoKey = RedisKeys.Chat.getChatMessageVideo(videoId);
                    redisOperator.delete(chatMessageVideoKey);
                }
                case ChatMessage.TYPE_FILE -> {
                    String fileId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.FILE_ID);
                    String chatMessageFileKey = RedisKeys.Chat.getChatMessageFile(fileId);
                    redisOperator.delete(chatMessageFileKey);
                }
                case ChatMessage.TYPE_VOICE -> {
                    String voiceId = (String) redisOperator.hGet(key, RedisKeys.Chat.ChatMessageHashKey.VOICE_ID);
                    String chatMessageVoiceKey = RedisKeys.Chat.getChatMessageVoice(voiceId);
                    redisOperator.delete(chatMessageVoiceKey);
                }
            }
            return true;
        }
    }

    public class GroupChat{
        public void saveGroupChatMessage(GroupChatMessage groupChatMessage, List<String> pendingChannelIds){
            String groupChatMessageRedisKey = RedisKeys.GroupChat.getGroupChatMessage(groupChatMessage.getTo(), groupChatMessage.getUuid());
            redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.TYPE, String.valueOf(groupChatMessage.getType()));
            redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.UUID, groupChatMessage.getUuid());
            redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.FROM, groupChatMessage.getFrom());
            redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.TO, groupChatMessage.getTo());
            redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.TIME, groupChatMessage.getTime());
            switch (groupChatMessage.getType()) {
                case GroupChatMessage.TYPE_TEXT -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.TEXT, groupChatMessage.getText());
                }
                case GroupChatMessage.TYPE_VOICE -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.VOICE_ID, groupChatMessage.getVoiceId());
                }
                case GroupChatMessage.TYPE_IMAGE -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_NAME, groupChatMessage.getFileName());
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.IMAGE_ID, groupChatMessage.getImageId());
                }
                case GroupChatMessage.TYPE_VIDEO -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_NAME, groupChatMessage.getFileName());
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.VIDEO_ID, groupChatMessage.getVideoId());
                }
                case GroupChatMessage.TYPE_FILE -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_NAME, groupChatMessage.getFileName());
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_ID, groupChatMessage.getFileId());
                }
                case GroupChatMessage.TYPE_NOTICE -> {

                }
                case GroupChatMessage.TYPE_UNSEND -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.UNSEND_MESSAGE_UUID, groupChatMessage.getUnsendMessageUuid());
                }
                case GroupChatMessage.TYPE_MESSAGE_EXPIRED -> {
                    redisOperator.hSet(groupChatMessageRedisKey, RedisKeys.GroupChat.GroupChatMessageHashKey.EXPIRED_MESSAGE_COUNT, groupChatMessage.getExpiredMessageCount());
                    return;
                }
            }
            redisOperator.expireForHash(groupChatMessageRedisKey, Constants.CHAT_MESSAGE_EXPIRATION_TIME_DAY, TimeUnit.DAYS);
            String setKey = RedisKeys.GroupChat.getGroupChatMessagePendingChannels(groupChatMessage.getTo(), groupChatMessage.getUuid());
            redisOperator.sAdd(setKey, pendingChannelIds.toArray());
            redisOperator.expireForSet(setKey, Constants.CHAT_MESSAGE_EXPIRATION_TIME_DAY, TimeUnit.DAYS);
        }

        public GroupMessageViewed viewMessage(String messageUuid, String currentUserImessageId){
            String groupChatMessagePendingChannelsPattern = RedisKeys.GroupChat.getGroupChatMessagePendingChannelsPatternMessage(messageUuid);
            AtomicReference<String> from = new AtomicReference<>();
            AtomicReference<String> to = new AtomicReference<>();
            AtomicInteger notViewedCount = new AtomicInteger();
            redisOperator.keys(groupChatMessagePendingChannelsPattern).forEach(key -> {
                redisOperator.sRemove(key, currentUserImessageId);
                String to1 = RedisKeys.GroupChat.parseToFromPendingChannelsKey(key);
                String uuid = RedisKeys.GroupChat.parseUuidFromPendingChannelsKey(key);
                String groupChatMessageKey = RedisKeys.GroupChat.getGroupChatMessage(to1, uuid);
                from.set((String) redisOperator.hGet(groupChatMessageKey, RedisKeys.Chat.ChatMessageHashKey.FROM));
                to.set((String) redisOperator.hGet(groupChatMessageKey, RedisKeys.Chat.ChatMessageHashKey.TO));
                if(redisOperator.sMembers(key).isEmpty()){
                    doOtherWhenDeleteMessage(groupChatMessageKey);
                    redisOperator.delete(groupChatMessageKey);
                    redisOperator.delete(key);
                }
                groupChannelService.findAllAssociatedGroupChannels(currentUserImessageId).forEach(groupChannel -> {
                    String messagePrefix = RedisKeys.GroupChat.getGroupChatMessagePrefix(groupChannel.getGroupChannelId());
                    notViewedCount.addAndGet(redisOperator.keys(messagePrefix + "*").size());
                });
                System.err.println("notViewedCount" + notViewedCount);
            });
            return new GroupMessageViewed(notViewedCount.get(), messageUuid, to.get(), from.get());
        }

        public void viewAllMessage(String groupChannelId, String currentUserImessageId){
            String groupChatMessagePattern = RedisKeys.GroupChat.getGroupChatMessagePendingChannelsPatternTo(groupChannelId);
            redisOperator.keys(groupChatMessagePattern).forEach(key -> {
                redisOperator.sRemove(key, currentUserImessageId);
                if(redisOperator.sMembers(key).isEmpty()){
                    String to1 = RedisKeys.GroupChat.parseToFromPendingChannelsKey(key);
                    String uuid = RedisKeys.GroupChat.parseUuidFromPendingChannelsKey(key);
                    String groupChatMessageKey = RedisKeys.GroupChat.getGroupChatMessage(to1, uuid);
                    doOtherWhenDeleteMessage(groupChatMessageKey);
                    redisOperator.delete(groupChatMessageKey);
                    redisOperator.delete(key);
                }
            });
        }

        public void saveGroupChatMessageImage(GroupChatMessage groupChatMessage, byte[] image){
            String chatMessageImageKey = RedisKeys.GroupChat.getGroupChatMessageImage(groupChatMessage.getImageId());
            redisOperator.hSet(chatMessageImageKey, RedisKeys.GroupChat.GroupChatMessageImageHashKey.IMAGE, Base64Util.encodeToString(image));
            redisOperator.hSet(chatMessageImageKey, RedisKeys.GroupChat.GroupChatMessageImageHashKey.IMAGE_ID, groupChatMessage.getImageId());
            redisOperator.hSet(chatMessageImageKey, RedisKeys.GroupChat.GroupChatMessageImageHashKey.IMAGE_FILE_NAME, groupChatMessage.getFileName());
        }

        public void saveGroupChatMessageFile(GroupChatMessage groupChatMessage, byte[] file){
            String chatMessageFileKey = RedisKeys.GroupChat.getGroupChatMessageFile(groupChatMessage.getFileId());
            redisOperator.hSet(chatMessageFileKey, RedisKeys.GroupChat.GroupChatMessageFileHashKey.FILE, Base64Util.encodeToString(file));
            redisOperator.hSet(chatMessageFileKey, RedisKeys.GroupChat.GroupChatMessageFileHashKey.FILE_ID, groupChatMessage.getFileId());
            redisOperator.hSet(chatMessageFileKey, RedisKeys.GroupChat.GroupChatMessageFileHashKey.FILE_FILE_NAME, groupChatMessage.getFileName());
        }

        public void saveGroupChatMessageVideo(GroupChatMessage groupChatMessage, byte[] video){
            String chatMessageVideoKey = RedisKeys.GroupChat.getGroupChatMessageVideo(groupChatMessage.getVideoId());
            redisOperator.hSet(chatMessageVideoKey, RedisKeys.GroupChat.GroupChatMessageVideoHashKey.VIDEO, Base64Util.encodeToString(video));
            redisOperator.hSet(chatMessageVideoKey, RedisKeys.GroupChat.GroupChatMessageVideoHashKey.VIDEO_ID, groupChatMessage.getVideoId());
            redisOperator.hSet(chatMessageVideoKey, RedisKeys.GroupChat.GroupChatMessageVideoHashKey.VIDEO_FILE_NAME, groupChatMessage.getFileName());
        }

        public void saveGroupChatMessageVoice(GroupChatMessage groupChatMessage, byte[] voice){
            String chatMessageVoiceKey = RedisKeys.GroupChat.getGroupChatMessageVoice(groupChatMessage.getVoiceId());
            redisOperator.hSet(chatMessageVoiceKey, RedisKeys.GroupChat.GroupChatMessageVoiceHashKey.VOICE_ID, groupChatMessage.getVoiceId());
            redisOperator.hSet(chatMessageVoiceKey, RedisKeys.GroupChat.GroupChatMessageVoiceHashKey.VOICE, Base64Util.encodeToString(voice));
        }

        public void deleteGroupChatMessage(String receiverChannel, String uuid){
            String messageRedisKey = RedisKeys.GroupChat.getGroupChatMessage(receiverChannel, uuid);
            String groupChatMessagePattern = RedisKeys.GroupChat.getGroupChatMessagePendingChannels(receiverChannel, uuid);
            doOtherWhenDeleteMessage(messageRedisKey);
            redisOperator.delete(messageRedisKey);
            redisOperator.delete(groupChatMessagePattern);
        }

        private boolean doOtherWhenDeleteMessage(String key) {
            Object typeStr = redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.TYPE);
            if(typeStr == null) return false;
            int type = Integer.parseInt(typeStr.toString());
            switch (type){
                case GroupChatMessage.TYPE_IMAGE -> {
                    String imageId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.IMAGE_ID);
                    String chatMessageImageKey = RedisKeys.GroupChat.getGroupChatMessageImage(imageId);
                    redisOperator.delete(chatMessageImageKey);
                }
                case GroupChatMessage.TYPE_VIDEO -> {
                    String videoId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.VIDEO_ID);
                    String chatMessageVideoKey = RedisKeys.GroupChat.getGroupChatMessageVideo(videoId);
                    redisOperator.delete(chatMessageVideoKey);
                }
                case GroupChatMessage.TYPE_FILE -> {
                    String fileId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_ID);
                    String chatMessageFileKey = RedisKeys.GroupChat.getGroupChatMessageFile(fileId);
                    redisOperator.delete(chatMessageFileKey);
                }
                case GroupChatMessage.TYPE_VOICE -> {
                    String voiceId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.VOICE_ID);
                    String chatMessageVoiceKey = RedisKeys.GroupChat.getGroupChatMessageVoice(voiceId);
                    redisOperator.delete(chatMessageVoiceKey);
                }
            }
            return true;
        }

        public List<GroupChatMessage> getAllGroupChatMessage(String to){
            List<GroupChatMessage> result = new ArrayList<>();
            String groupChatMessagePrefix = RedisKeys.GroupChat.getGroupChatMessagePrefix(to);
            Set<String> keys = redisOperator.keys(groupChatMessagePrefix + "*");
            keys.forEach(key -> {
                int type = Integer.parseInt(redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.TYPE).toString());
                String uuid = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.UUID);
                String from = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.FROM);
                String toFound = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.TO);
                Date time = new Date((Long) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.TIME));
                String text = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.TEXT);
                String extension = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_NAME);
                String imageId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.IMAGE_ID);
                String fileId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.FILE_ID);
                String videoId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.VIDEO_ID);
                String voiceId = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.VOICE_ID);
                String unsendMessageUuid = (String) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.UNSEND_MESSAGE_UUID);
                Integer expiredMessageCount = (Integer) redisOperator.hGet(key, RedisKeys.GroupChat.GroupChatMessageHashKey.EXPIRED_MESSAGE_COUNT);
                result.add(new GroupChatMessage(type, uuid, from, toFound, time, text, extension, imageId, fileId, videoId, voiceId, unsendMessageUuid, expiredMessageCount));
            });
            return result;
        }

        public Set<String> getAllPendingChannelIds(GroupChatMessage groupChatMessage){
            String setKey = RedisKeys.GroupChat.getGroupChatMessagePendingChannels(groupChatMessage.getTo(), groupChatMessage.getUuid());
            return redisOperator.sMembers(setKey).stream()
                    .filter(o -> o instanceof String)
                    .map(o -> (String) o)
                    .collect(Collectors.toSet());
        }
    }

    public class Broadcast{
        public void saveNewBroadcastLike(String to, String likeId){
            String key = RedisKeys.Broadcast.newBroadcastLike(to, likeId);
            redisOperator.set(key, "");
        }

        public void deleteNewBroadcastLike(String to, String likeId){
            String key = RedisKeys.Broadcast.newBroadcastLike(to, likeId);
            redisOperator.delete(key);
        }

        public int getBroadcastLikeNewsCount(String to){
            String key = RedisKeys.Broadcast.newBroadcastLike(to, "*");
            return redisOperator.keys(key).size();
        }

        public boolean hasNewBroadcastLike(String to, String likeId){
            String key = RedisKeys.Broadcast.newBroadcastLike(to, likeId);
            return redisOperator.exists(key);
        }

        public void saveNewBroadcastComment(String to, String commentId){
            String key = RedisKeys.Broadcast.newBroadcastComment(to, commentId);
            redisOperator.set(key, "");
        }

        public void saveNewBroadcastReply(String to, String commentId){
            String key = RedisKeys.Broadcast.newBroadcastReply(to, commentId);
            redisOperator.set(key, "");
        }

        public int getBroadcastCommentNewsCount(String to){
            String key = RedisKeys.Broadcast.newBroadcastComment(to, "*");
            return redisOperator.keys(key).size();
        }

        public boolean hasNewBroadcastComment(String to, String likeId){
            String key = RedisKeys.Broadcast.newBroadcastComment(to, likeId);
            return redisOperator.exists(key);
        }

        public void deleteNewBroadcastComment(String to, String commentId){
            String key = RedisKeys.Broadcast.newBroadcastComment(to, commentId);
            redisOperator.delete(key);
        }

        public int getBroadcastReplyNewsCount(String to){
            String key = RedisKeys.Broadcast.newBroadcastReply(to, "*");
            return redisOperator.keys(key).size();
        }

        public boolean hasNewBroadcastReply(String to, String likeId){
            String key = RedisKeys.Broadcast.newBroadcastReply(to, likeId);
            return redisOperator.exists(key);
        }

        public void deleteNewBroadcastReply(String to, String commentId){
            String key = RedisKeys.Broadcast.newBroadcastReply(to, commentId);
            redisOperator.delete(key);
        }
    }

    public class GroupChannelAddition {
        public void saveRequester(com.longx.intelligent.app.imessage.server.data.GroupChannelAddition groupChannelAddition) {
            String key = RedisKeys.GroupChannelAddition.requester(groupChannelAddition.requesterChannel().getImessageId(),
                    groupChannelAddition.responderGroupChannel().getGroupChannelId(), groupChannelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.UUID, groupChannelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE, groupChannelAddition.message());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE, groupChannelAddition.note());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES, groupChannelAddition.newTagNames());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS, groupChannelAddition.toAddTagIds());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME, groupChannelAddition.requestTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME, groupChannelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED, groupChannelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED, groupChannelAddition.isViewed());
            if(groupChannelAddition.inviteUuid() != null) {
                redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID, groupChannelAddition.inviteUuid());
            }
            redisOperator.expireForHash(key, Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public void saveResponder(com.longx.intelligent.app.imessage.server.data.GroupChannelAddition groupChannelAddition) {
            String key = RedisKeys.GroupChannelAddition.responder(groupChannelAddition.responderGroupChannel().getGroupChannelId(),
                    groupChannelAddition.requesterChannel().getImessageId(), groupChannelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.UUID, groupChannelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE, groupChannelAddition.message());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE, groupChannelAddition.note());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES, groupChannelAddition.newTagNames());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS, groupChannelAddition.toAddTagIds());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME, groupChannelAddition.requestTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME, groupChannelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED, groupChannelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED, groupChannelAddition.isViewed());
            if(groupChannelAddition.inviteUuid() != null) {
                redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID, groupChannelAddition.inviteUuid());
            }
            redisOperator.expireForHash(key, Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public boolean isInAdding(String requesterImessageId, String responderGroupChannelId){
            String requesterKeyPrefix = RedisKeys.GroupChannelAddition.requesterPrefix(requesterImessageId, responderGroupChannelId);
            String responderKeyPrefix = RedisKeys.GroupChannelAddition.responderPrefix(responderGroupChannelId, requesterImessageId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");
            Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");
            boolean booleanPart1 = false;
            for (String requesterKey : requesterKeys) {
                if(redisOperator.exists(requesterKey) && !isExpired(requesterKey)
                        && (Objects.equals(redisOperator.hGet(requesterKey, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED), Boolean.FALSE)
                        || (Objects.equals(redisOperator.hGet(requesterKey, RedisKeys.ChannelAddition.RequesterHashKey.IS_ACCEPTED), null)))){
                    booleanPart1 = true;
                    break;
                }
            }
            boolean booleanPart2 = false;
            for (String responderKey : responderKeys) {
                if(redisOperator.exists(responderKey) && !isExpired(responderKey)){
                    booleanPart2 = true;
                    break;
                }
            }
            return booleanPart1 && booleanPart2;
        }

        public boolean isExpired(String key){
            return !(redisOperator.getExpire(key, TimeUnit.DAYS) > Constants.GROUP_CHANNEL_ADDITION_RECORD_DURATION_DAY - Constants.GROUP_CHANNEL_ADDITION_ADDITION_DURATION_DAY);
        }

        public GroupChannelAdditionNotViewedCount getGroupChannelAdditionNotViewedCount(String currentUserImessageId){
            AtomicInteger resultRequester = new AtomicInteger();
            AtomicInteger resultResponder = new AtomicInteger();
            AtomicInteger resultSelfNotificationRequest = new AtomicInteger();
            AtomicInteger resultSelfNotificationRespond = new AtomicInteger();
            AtomicInteger resultOtherNotificationRequest = new AtomicInteger();
            AtomicInteger resultOtherNotificationRespond = new AtomicInteger();
            AtomicInteger resultInviter = new AtomicInteger();
            AtomicInteger resultInvitee = new AtomicInteger();
            AtomicInteger resultNotificationInviter = new AtomicInteger();
            AtomicInteger resultNotificationInvitee = new AtomicInteger();
            String requesterKeyPrefix = RedisKeys.GroupChannelAddition.requesterPrefix(currentUserImessageId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");
            requesterKeys.forEach(key -> {
                boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
                if(!isViewed) {
                    resultRequester.getAndIncrement();
                    Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME);
                    if (respondTimeLong != null) {
                        resultOtherNotificationRespond.getAndIncrement();
                    }else {
                        resultSelfNotificationRequest.getAndIncrement();
                    }
                }
            });
            List<GroupChannel> allGroupChannels = groupChannelService.findAllOwnerGroupChannels(currentUserImessageId);
            String[] groupChannelIds = new String[allGroupChannels.size()];
            for (int i = 0; i < allGroupChannels.size(); i++) {
                groupChannelIds[i] = allGroupChannels.get(i).getGroupChannelId();
            }
            for (String groupChannelId : groupChannelIds) {
                String responderKeyPrefix = RedisKeys.GroupChannelAddition.responderPrefix(groupChannelId);
                Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");
                responderKeys.forEach(key -> {
                    boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
                    if (!isViewed) {
                        resultResponder.getAndIncrement();
                        Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME);
                        if (respondTimeLong != null) {
                            resultSelfNotificationRespond.getAndIncrement();
                        }else {
                            resultOtherNotificationRequest.getAndIncrement();
                        }
                    }
                });
            }
            String inviterKeyPrefix = RedisKeys.GroupChannelAddition.inviterPrefix(currentUserImessageId);
            Set<String> inviterKeys = redisOperator.keys(inviterKeyPrefix + "*");
            inviterKeys.forEach(key -> {
                boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED);
                if(!isViewed) {
                    resultInviter.getAndIncrement();
                    Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME);
                    if (respondTimeLong != null) {
                        resultNotificationInvitee.getAndIncrement();
                    }else {
                        resultNotificationInviter.getAndIncrement();
                    }
                }
            });
            String inviteeKeyPrefix = RedisKeys.GroupChannelAddition.inviteePrefix(currentUserImessageId);
            Set<String> inviteeKeys = redisOperator.keys(inviteeKeyPrefix + "*");
            inviteeKeys.forEach(key -> {
                boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED);
                if(!isViewed) {
                    resultInvitee.getAndIncrement();
                    Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME);
                    if (respondTimeLong != null) {
                        resultNotificationInvitee.getAndIncrement();
                    }else {
                        resultNotificationInviter.getAndIncrement();
                    }
                }
            });
            return new GroupChannelAdditionNotViewedCount(resultRequester.get(), resultResponder.get(), resultSelfNotificationRequest.get(),
                    resultSelfNotificationRespond.get(), resultOtherNotificationRequest.get(), resultOtherNotificationRespond.get(),
                    resultInviter.get(), resultInvitee.get(), resultNotificationInviter.get(), resultNotificationInvitee.get());
        }

        public List<com.longx.intelligent.app.imessage.server.data.GroupChannelAddition> getAllGroupChannelAddition(String currentUserId, HttpSession session) {
            List<com.longx.intelligent.app.imessage.server.data.GroupChannelAddition> results = new ArrayList<>();
            User currentUser = sessionService.getUserOfSession(session);
            String requesterKeyPrefix = RedisKeys.GroupChannelAddition.requesterPrefix(currentUserId);
            Set<String> requesterKeys = redisOperator.keys(requesterKeyPrefix + "*");

            Map<String, Map<String, Object>> requesterDataMap = batchGetAdditionRedisData(requesterKeys);

            requesterKeys.parallelStream().forEach(key -> {
                String replace = key.replace(requesterKeyPrefix, "");
                String groupChannelId = replace.substring(0, replace.indexOf(':'));
                Map<String, Object> dataMap = requesterDataMap.get(key);

                String uuid = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.UUID);
                String message = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE);
                String note = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE);
                List<String> newTagNames = (List<String>) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES);
                List<String> toAddTagIds = (List<String>) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS);
                Date requestTime = new Date((Long) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME));
                Long respondTimeLong = (Long) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME);
                Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                boolean isAccepted = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED);
                boolean isViewed = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
                String inviteUuid = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID);

                Channel requesterChannelInfo = channelService.findChannelByImessageId(currentUserId, session);
                GroupChannel responderChannelInfo = groupChannelService.findGroupChannelById(groupChannelId, currentUser.getImessageId());
                boolean isExpired = isExpired(key);

                results.add(com.longx.intelligent.app.imessage.server.data.GroupChannelAddition.create(uuid, requesterChannelInfo, responderChannelInfo,
                        message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired, inviteUuid));
            });

            List<GroupChannel> allOwnerGroupChannels = groupChannelService.findAllOwnerGroupChannels(currentUserId);
            allOwnerGroupChannels.forEach(groupChannel -> {
                String responderKeyPrefix = RedisKeys.GroupChannelAddition.responderPrefix(groupChannel.getGroupChannelId());
                Set<String> responderKeys = redisOperator.keys(responderKeyPrefix + "*");

                Map<String, Map<String, Object>> responderDataMap = batchGetAdditionRedisData(responderKeys);

                responderKeys.parallelStream().forEach(key -> {
                    String replace = key.replace(responderKeyPrefix, "");
                    String channelId = replace.substring(0, replace.indexOf(':'));
                    Map<String, Object> dataMap = responderDataMap.get(key);

                    String uuid = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.UUID);
                    String message = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE);
                    String note = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE);
                    List<String> newTagNames = (List<String>) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES);
                    List<String> toAddTagIds = (List<String>) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS);
                    Date requestTime = new Date((Long) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME));
                    Long respondTimeLong = (Long) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME);
                    Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                    boolean isAccepted = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED);
                    boolean isViewed = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
                    String inviteUuid = (String) dataMap.get(RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID);

                    Channel requesterChannelInfo = channelService.findChannelByImessageId(channelId, currentUserId);
                    boolean isExpired = isExpired(key);

                    results.add(com.longx.intelligent.app.imessage.server.data.GroupChannelAddition.create(uuid, requesterChannelInfo, groupChannel,
                            message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired, inviteUuid));
                });
            });

            return results;
        }

        public List<GroupChannelInvitation> getAllGroupChannelInvitation(String imessageId, HttpSession session) {
            List<GroupChannelInvitation> results = new ArrayList<>();
            User currentUser = sessionService.getUserOfSession(session);
            String inviterKeyPrefix = RedisKeys.GroupChannelAddition.inviterPrefix(imessageId);
            Set<String> inviterKeys = redisOperator.keys(inviterKeyPrefix + "*");

            Map<String, Map<String, Object>> inviterDataMap = batchGetInvitationRedisData(inviterKeys);

            inviterKeys.parallelStream().forEach(key -> {
                String replace = key.replace(inviterKeyPrefix, "");
                String invitee = replace.substring(0, replace.indexOf(':'));
                String invitedTo = replace.split(":")[1];
                Map<String, Object> dataMap = inviterDataMap.get(key);

                String uuid = (String) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.UUID);
                String message = (String) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE);
                Date requestTime = new Date((Long) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME));
                Long respondTimeLong = (Long) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME);
                Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                boolean isAccepted = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED);
                boolean isViewed = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED);

                Channel inviterChannelInfo = channelService.findChannelByImessageId(imessageId, session);
                Channel inviteeChannelInfo = channelService.findChannelByImessageId(invitee, session);
                GroupChannel invitedToChannelInfo = groupChannelService.findGroupChannelById(invitedTo, currentUser.getImessageId());
                boolean isExpired = isExpired(key);

                results.add(GroupChannelInvitation.create(uuid, inviterChannelInfo, inviteeChannelInfo,
                        invitedToChannelInfo, message, requestTime, respondTime, isAccepted, isViewed, isExpired, GroupChannelInvitation.Type.INVITER));
            });

            String inviteeKeyPrefix = RedisKeys.GroupChannelAddition.inviteePrefix(imessageId);
            Set<String> inviteeKeys = redisOperator.keys(inviteeKeyPrefix + "*");

            Map<String, Map<String, Object>> inviteeDataMap = batchGetInvitationRedisData(inviteeKeys);

            inviteeKeys.parallelStream().forEach(key -> {
                String replace = key.replace(inviteeKeyPrefix, "");
                String inviter = replace.substring(0, replace.indexOf(':'));
                String invitedTo = replace.split(":")[1];
                Map<String, Object> dataMap = inviteeDataMap.get(key);

                String uuid = (String) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.UUID);
                String message = (String) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE);
                Date requestTime = new Date((Long) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME));
                Long respondTimeLong = (Long) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME);
                Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                boolean isAccepted = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED);
                boolean isViewed = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED);

                Channel inviteeChannelInfo = channelService.findChannelByImessageId(imessageId, session);
                Channel inviterChannelInfo = channelService.findChannelByImessageId(inviter, session);
                GroupChannel invitedToChannelInfo = groupChannelService.findGroupChannelById(invitedTo, currentUser.getImessageId());
                boolean isExpired = isExpired(key);

                results.add(GroupChannelInvitation.create(uuid, inviterChannelInfo, inviteeChannelInfo,
                        invitedToChannelInfo, message, requestTime, respondTime, isAccepted, isViewed, isExpired, GroupChannelInvitation.Type.INVITEE));
            });

            return results;
        }

        public List<GroupChannelInvitation> getGroupChannelInvitationByUuid(String uuid, HttpSession session){
            List<GroupChannelInvitation> results = new ArrayList<>();
            User currentUser = sessionService.getUserOfSession(session);
            Set<String> keys = redisOperator.stringKeys("*" + uuid + "*");
            Map<String, Map<String, Object>> dataMaps = batchGetInvitationRedisData(keys);
            keys.forEach(key -> {
                String[] split = key.split(":");
                String inviter = null;
                String invitee = null;
                String invitedTo = null;
                GroupChannelInvitation.Type type = null;
                if(key.contains("inviter")){
                    inviter = split[2];
                    invitee = split[3];
                    invitedTo = split[4];
                    type = GroupChannelInvitation.Type.INVITER;
                }else if(key.contains("invitee")){
                    inviter = split[3];
                    invitee = split[2];
                    invitedTo = split[4];
                    type = GroupChannelInvitation.Type.INVITEE;
                }
                Map<String, Object> dataMap = dataMaps.get(key);
                String message = (String) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE);
                Date requestTime = new Date((Long) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME));
                Long respondTimeLong = (Long) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME);
                Date respondTime = respondTimeLong == null ? null : new Date(respondTimeLong);
                boolean isAccepted = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED);
                boolean isViewed = (boolean) dataMap.get(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED);
                Channel inviterChannelInfo = channelService.findChannelByImessageId(inviter, session);
                Channel inviteeChannelInfo = channelService.findChannelByImessageId(invitee, session);
                GroupChannel invitedToChannelInfo = groupChannelService.findGroupChannelById(invitedTo, currentUser.getImessageId());
                boolean isExpired = isExpired(key);
                results.add(GroupChannelInvitation.create(uuid, inviterChannelInfo, inviteeChannelInfo,
                        invitedToChannelInfo, message, requestTime, respondTime, isAccepted, isViewed, isExpired, type));
            });
            return results;
        }

        private Map<String, Map<String, Object>> batchGetAdditionRedisData(Set<String> keys) {
            Map<String, Map<String, Object>> dataMap = new HashMap<>();
            for (String key : keys) {
                Map<String, Object> map = new HashMap<>();
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.UUID, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.UUID));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS));
                map.put(RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID));
                dataMap.put(key, map);
            }
            return dataMap;
        }

        private Map<String, Map<String, Object>> batchGetInvitationRedisData(Set<String> keys) {
            Map<String, Map<String, Object>> dataMap = new HashMap<>();
            for (String key : keys) {
                Map<String, Object> map = new HashMap<>();
                map.put(RedisKeys.GroupChannelAddition.InvitationHashKey.UUID, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.UUID));
                map.put(RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE));
                map.put(RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME));
                map.put(RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME));
                map.put(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED));
                map.put(RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED, redisOperator.hGet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED));
                dataMap.put(key, map);
            }
            return dataMap;
        }

        public com.longx.intelligent.app.imessage.server.data.GroupChannelAddition getRequesterGroupChannelAdditionByUuid(String uuid, HttpSession session) {
            User currentUser = sessionService.getUserOfSession(session);
            Set<String> keys = redisOperator.stringKeys("*" + uuid + "*");
            String key = null;
            for (String k : keys) {
                if(k.contains("requester")){
                    key = k;
                    break;
                }
            }
            if(key == null) return null;
            String[] split = key.split(":");
            String requesterImessageId = split[2];
            String responderGroupChannelId = split[3];
            String message = (String) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.MESSAGE);
            String note = (String) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NOTE);
            List<String> newTagNames = (List<String>) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.NEW_TAG_NAMES);
            List<String> toAddTagIds = (List<String>) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.TO_ADD_TAG_IDS);
            Date requestTime = new Date((Long) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.REQUEST_TIME));
            Long respondTimeLong = (Long) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME);
            Date respondTime = respondTimeLong == null ? null: new Date(respondTimeLong);
            boolean isAccepted = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED);
            boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
            String inviteUuid = (String) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.INVITE_UUID);
            Channel requesterChannelInfo = channelService.findChannelByImessageId(requesterImessageId, session);
            GroupChannel responderGroupChannelInfo = groupChannelService.findGroupChannelById(responderGroupChannelId, currentUser.getImessageId());
            boolean isExpired = isExpired(key);
            return com.longx.intelligent.app.imessage.server.data.GroupChannelAddition.create(uuid, requesterChannelInfo, responderGroupChannelInfo,
                    message, note, newTagNames, toAddTagIds, requestTime, respondTime, isAccepted, isViewed, isExpired, inviteUuid);
        }

        public void acceptChangeRequester(com.longx.intelligent.app.imessage.server.data.GroupChannelAddition groupChannelAddition){
            String key = RedisKeys.GroupChannelAddition.requester(groupChannelAddition.requesterChannel().getImessageId(),
                    groupChannelAddition.responderGroupChannel().getGroupChannelId(), groupChannelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.RESPOND_TIME, groupChannelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_ACCEPTED, groupChannelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED, groupChannelAddition.isViewed());
        }

        public void acceptChangeResponder(com.longx.intelligent.app.imessage.server.data.GroupChannelAddition groupChannelAddition){
            String key = RedisKeys.GroupChannelAddition.responder(groupChannelAddition.responderGroupChannel().getGroupChannelId(),
                    groupChannelAddition.requesterChannel().getImessageId(), groupChannelAddition.uuid());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.RESPOND_TIME, groupChannelAddition.respondTime());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_ACCEPTED, groupChannelAddition.isAccepted());
            redisOperator.hSet(key, RedisKeys.ChannelAddition.ResponderHashKey.IS_VIEWED, groupChannelAddition.isViewed());
        }

        public boolean setOneToViewed(String currentUserImessageId, String uuid) {
            Set<String> keys = redisOperator.keys("*" + uuid + "*");
            AtomicBoolean failed = new AtomicBoolean(false);
            AtomicBoolean allNotMatch = new AtomicBoolean(true);
            for (String key : keys) {
                String[] parts = key.split(":");
                String imessageId = null;
                if (key.contains("responder")) {
                    imessageId = groupChannelService.findGroupChannelById(parts[2], currentUserImessageId).getOwner();
                } else if (key.contains("invitee") || key.contains("inviter") || key.contains("requester")) {
                    imessageId = parts[2];
                }
                if (imessageId == null) {
                    failed.set(true);
                    break;
                }
                if (imessageId.equals(currentUserImessageId)) {
                    allNotMatch.set(false);
                    Boolean isViewed = true;
                    if (key.contains("requester") || key.contains("inviter")) {
                        isViewed = (Boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
                    } else if (key.contains("responder") || key.contains("invitee")) {
                        isViewed = (Boolean) redisOperator.hGet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED);
                    }
                    if (isViewed) {
                        failed.set(true);
                        break;
                    }
                    if (key.contains("requester") || key.contains("inviter")) {
                        redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED, true);
                    } else if (key.contains("responder") || key.contains("invitee")) {
                        redisOperator.hSet(key, RedisKeys.GroupChannelAddition.AdditionHashKey.IS_VIEWED, true);
                    }
                }
            }
            ;
            if (allNotMatch.get()) {
                failed.set(true);
            }
            return !failed.get();
        }

        public void saveInviter(GroupChannelInvitation groupChannelInvitation){
            String key = RedisKeys.GroupChannelAddition.inviter(
                    groupChannelInvitation.inviter().getImessageId(), groupChannelInvitation.invitee().getImessageId(),
                    groupChannelInvitation.groupChannelInvitedTo().getGroupChannelId(), groupChannelInvitation.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.UUID, groupChannelInvitation.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE, groupChannelInvitation.message());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME, groupChannelInvitation.requestTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME, groupChannelInvitation.respondTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED, groupChannelInvitation.isAccepted());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED, groupChannelInvitation.isViewed());
            redisOperator.expireForHash(key, Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public void saveInvitee(GroupChannelInvitation groupChannelInvitation){
            String key = RedisKeys.GroupChannelAddition.invitee(
                    groupChannelInvitation.invitee().getImessageId(), groupChannelInvitation.inviter().getImessageId(),
                    groupChannelInvitation.groupChannelInvitedTo().getGroupChannelId(), groupChannelInvitation.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.UUID, groupChannelInvitation.uuid());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.MESSAGE, groupChannelInvitation.message());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.REQUEST_TIME, groupChannelInvitation.requestTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.RESPOND_TIME, groupChannelInvitation.respondTime());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED, groupChannelInvitation.isAccepted());
            redisOperator.hSet(key, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_VIEWED, groupChannelInvitation.isViewed());
            redisOperator.expireForHash(key, Constants.CHANNEL_ADDITION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public boolean isInInviting(String inviter, String invitee, String groupChannelId) {
            String inviterKeyPrefix = RedisKeys.GroupChannelAddition.inviterPrefix(inviter, invitee, groupChannelId);
            String inviteeKeyPrefix = RedisKeys.GroupChannelAddition.inviteePrefix(invitee, inviter, groupChannelId);
            Set<String> inviterKeys = redisOperator.keys(inviterKeyPrefix + "*");
            Set<String> inviteeKeys = redisOperator.keys(inviteeKeyPrefix + "*");
            boolean booleanPart1 = false;
            for (String inviterKey : inviterKeys) {
                if(redisOperator.exists(inviterKey) && !isExpired(inviterKey)
                        && (Objects.equals(redisOperator.hGet(inviterKey, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED), Boolean.FALSE)
                        || (Objects.equals(redisOperator.hGet(inviterKey, RedisKeys.GroupChannelAddition.InvitationHashKey.IS_ACCEPTED), null)))){
                    booleanPart1 = true;
                    break;
                }
            }
            boolean booleanPart2 = false;
            for (String inviteeKey : inviteeKeys) {
                if(redisOperator.exists(inviteeKey) && !isExpired(inviteeKey)){
                    booleanPart2 = true;
                    break;
                }
            }
            return booleanPart1 && booleanPart2;
        }
    }

    public class GroupChannelNotification {
        public void saveNotification(String toFetchChannelId, com.longx.intelligent.app.imessage.server.data.GroupChannelNotification groupChannelNotification) {
            String disconnectionKey = RedisKeys.GroupChannelNotification.notification(toFetchChannelId, groupChannelNotification.getGroupChannelId(), groupChannelNotification.getChannelId(), groupChannelNotification.getUuid());
            redisOperator.hSet(disconnectionKey, RedisKeys.GroupChannelNotification.NotificationHashKey.IS_VIEWED, groupChannelNotification.isViewed());
            redisOperator.hSet(disconnectionKey, RedisKeys.GroupChannelNotification.NotificationHashKey.PASSIVE, groupChannelNotification.isPassive());
            redisOperator.hSet(disconnectionKey, RedisKeys.GroupChannelNotification.NotificationHashKey.BY_WHOM, groupChannelNotification.getByWhom());
            redisOperator.hSet(disconnectionKey, RedisKeys.GroupChannelNotification.NotificationHashKey.TIME, groupChannelNotification.getTime());
            redisOperator.hSet(disconnectionKey, RedisKeys.GroupChannelNotification.NotificationHashKey.TYPE, groupChannelNotification.getType().name());
            redisOperator.expireForHash(disconnectionKey, Constants.GROUP_CHANNEL_NOTIFICATION_RECORD_DURATION_DAY, TimeUnit.DAYS);
        }

        public com.longx.intelligent.app.imessage.server.data.GroupChannelNotification findNotification(String uuid){
            Set<String> keys = redisOperator.stringKeys("*" + uuid + "*");
            String key = null;
            for (String k : keys) {
                if(k.contains("group_channel_notification")){
                    key = k;
                    break;
                }
            }
            if(key == null) return null;
            String[] split = key.split(":");
            String groupChannelId = split[3];
            String channelId = split[4];
            com.longx.intelligent.app.imessage.server.data.GroupChannelNotification.Type type = com.longx.intelligent.app.imessage.server.data.GroupChannelNotification.Type
                    .valueOf((String) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.TYPE));
            Boolean passive = (Boolean) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.PASSIVE);
            boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.IS_VIEWED);
            String byWhom = (String) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.BY_WHOM);
            Date time = new Date((Long) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.TIME));
            return new com.longx.intelligent.app.imessage.server.data.GroupChannelNotification(uuid, type, groupChannelId, channelId, passive, byWhom, time, isViewed);
        }

        public List<com.longx.intelligent.app.imessage.server.data.GroupChannelNotification> getNotifications(String toFetchChannelId) {
            String notificationWithToFetchChannelId = RedisKeys.GroupChannelNotification.getNotificationWithToFetchChannelId(toFetchChannelId);
            Set<String> keys = redisOperator.keys(notificationWithToFetchChannelId);
            List<com.longx.intelligent.app.imessage.server.data.GroupChannelNotification> results = new ArrayList<>();
            for (String key : keys) {
                if (redisOperator.exists(key) && !isExpired(key)) {
                    String[] split = key.split(":");
                    String groupChannelId = split[3];
                    String channelId = split[4];
                    String uuid = split[5];
                    Boolean passive = (Boolean) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.PASSIVE);
                    boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.IS_VIEWED);
                    String byWhom = (String) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.BY_WHOM);
                    Date time = new Date((Long)redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.TIME));
                    com.longx.intelligent.app.imessage.server.data.GroupChannelNotification.Type type = com.longx.intelligent.app.imessage.server.data.GroupChannelNotification.Type.valueOf((String) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.TYPE));
                    results.add(new com.longx.intelligent.app.imessage.server.data.GroupChannelNotification(uuid, type, groupChannelId, channelId, passive, byWhom, time, isViewed));
                    if(isViewed){
                        redisOperator.renameKey(key, key.replace("TO_FETCH", "FETCH_COMPLETE"));
                    }
                }
            }
            return results;
        }

        public List<com.longx.intelligent.app.imessage.server.data.GroupChannelNotification> getSelfNotifications(String channelId){
            String notificationWithChannelId = RedisKeys.GroupChannelNotification.getNotificationWithChannelId(channelId);
            Set<String> keys = redisOperator.keys(notificationWithChannelId);
            List<com.longx.intelligent.app.imessage.server.data.GroupChannelNotification> results = new ArrayList<>();
            for (String key : keys) {
                if (redisOperator.exists(key) && !isExpired(key)) {
                    String[] split = key.split(":");
                    String groupChannelId = split[1];
                    String uuid = split[3];
                    Boolean passive = (Boolean) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.PASSIVE);
                    boolean isViewed = (boolean) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.IS_VIEWED);
                    String byWhom = (String) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.BY_WHOM);
                    Date time = new Date((Long)redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.TIME));
                    com.longx.intelligent.app.imessage.server.data.GroupChannelNotification.Type type = com.longx.intelligent.app.imessage.server.data.GroupChannelNotification.Type.valueOf((String) redisOperator.hGet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.TYPE));
                    results.add(new com.longx.intelligent.app.imessage.server.data.GroupChannelNotification(uuid, type, groupChannelId, channelId, passive, byWhom, time, isViewed));
                    if(isViewed){
                        redisOperator.renameKey(key, "FETCH_COMPLETE:" + key);
                    }
                }
            }
            return results;
        }

        public boolean isExpired(String key) {
            return redisOperator.getExpire(key, TimeUnit.DAYS) > Constants.GROUP_CHANNEL_NOTIFICATION_RECORD_DURATION_DAY;
        }

        public void setToViewed(String toFetchChannelId, String uuid){
            String notificationWithUuid = RedisKeys.GroupChannelNotification.getNotificationWithUuid(toFetchChannelId, uuid);
            Set<String> keys = redisOperator.keys(notificationWithUuid);
            for (String key : keys) {
                redisOperator.hSet(key, RedisKeys.GroupChannelNotification.NotificationHashKey.IS_VIEWED, true);
            }
        }
    }
}

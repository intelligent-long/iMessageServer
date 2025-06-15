package com.longx.intelligent.app.imessage.server.value;

/**
 * Created by LONG on 2024/3/30 at 12:53 AM.
 */
public class RedisKeys {
    public static class Auth {
        private static final String BASE_PATH = "auth";
        public static final int EXPIRE_HOURS_FAILURE_TIMES = 6;
        private static final String FAILURE_TIMES = BASE_PATH + ":login:failure_times:{IMESSAGE_ID}";
        public static String failureTimes(String imessageId) {
            return FAILURE_TIMES.replace("{IMESSAGE_ID}", imessageId);
        }

        private static final String OFFLINE_DETAIL = BASE_PATH + ":offline:detail:{SESSION_ID}";
        public static String offlineDetail(String sessionId){
            return OFFLINE_DETAIL.replace("{SESSION_ID}", sessionId);
        }
    }

    public static class VerifyCode {
        private static final String BASE_PATH = "verify_code";
        public static final int EXPIRE_MINUTES_VERIFY_CODE = 5;
        public static final int EXPIRE_MINUTES_SEND_LIMIT = 1;
        private static final String SEND_LIMIT = BASE_PATH + ":send_limit:{EMAIL}";
        public static String sendLimit(String email){
            return SEND_LIMIT.replace("{EMAIL}", email);
        }

        private static final String VERIFY_CODE = BASE_PATH + ":{EMAIL}";
        public static String verifyCode(String email){
            return VERIFY_CODE.replace("{EMAIL}", email);
        }

        private static final String FAILURE_TIMES = BASE_PATH + ":failure_times:{EMAIL}";
        public static String failureTimes(String email) {
            return FAILURE_TIMES.replace("{EMAIL}", email);
        }
    }

    public static class ChannelAddition{
        private static final String BASE_PATH = "channel_addition";

        public static class RequesterHashKey{
            public static final String UUID = "uuid";
            public static final String MESSAGE = "message";
            public static final String NOTE = "note";
            public static final String NEW_TAG_NAMES = "new_tag_names";
            public static final String TO_ADD_TAG_IDS = "to_add_tag_ids";
            public static final String REQUEST_TIME = "request_time";
            public static final String RESPOND_TIME = "respond_time";
            public static final String IS_ACCEPTED = "is_accepted";
            public static final String IS_VIEWED = "is_viewed";
        }

        public static class ResponderHashKey{
            public static final String UUID = "uuid";
            public static final String MESSAGE = "message";
            public static final String NOTE = "note";
            public static final String NEW_TAG_NAMES = "new_tag_names";
            public static final String TO_ADD_TAG_IDS = "to_add_tag_ids";
            public static final String REQUEST_TIME = "request_time";
            public static final String RESPOND_TIME = "respond_time";
            public static final String IS_ACCEPTED = "is_accepted";
            public static final String IS_VIEWED = "is_viewed";
        }

        private static final String REQUESTER = BASE_PATH + ":requester:{REQUESTER_IMESSAGE_ID}:{RESPONDER_IMESSAGE_ID}:{UUID}";
        public static String requester(String requesterImessageId, String responderImessageId, String uuid){
            return REQUESTER
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId)
                    .replace("{RESPONDER_IMESSAGE_ID}", responderImessageId)
                    .replace("{UUID}", uuid);
        }
        private static final String RESPONDER = BASE_PATH + ":responder:{RESPONDER_IMESSAGE_ID}:{REQUESTER_IMESSAGE_ID}:{UUID}";
        public static String responder(String responderImessageId, String requesterImessageId, String uuid){
            return RESPONDER
                    .replace("{RESPONDER_IMESSAGE_ID}", responderImessageId)
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId)
                    .replace("{UUID}", uuid);
        }

        private static final String REQUESTER_PREFIX_1 = BASE_PATH + ":requester:{REQUESTER_IMESSAGE_ID}:{RESPONDER_IMESSAGE_ID}:";
        public static String requesterPrefix(String requesterImessageId, String responderImessageId){
            return REQUESTER_PREFIX_1
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId)
                    .replace("{RESPONDER_IMESSAGE_ID}", responderImessageId);
        }

        private static final String RESPONDER_PREFIX_1 = BASE_PATH + ":responder:{RESPONDER_IMESSAGE_ID}:{REQUESTER_IMESSAGE_ID}:";
        public static String responderPrefix(String responderImessageId, String requesterImessageId){
            return RESPONDER_PREFIX_1
                    .replace("{RESPONDER_IMESSAGE_ID}", responderImessageId)
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId);
        }

        private static final String REQUESTER_PREFIX_2 = BASE_PATH + ":requester:{REQUESTER_IMESSAGE_ID}:";
        public static String requesterPrefix(String requesterImessageId){
            return REQUESTER_PREFIX_2
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId);
        }

        private static final String RESPONDER_PREFIX_2 = BASE_PATH + ":responder:{RESPONDER_IMESSAGE_ID}:";
        public static String responderPrefix(String responderImessageId){
            return RESPONDER_PREFIX_2
                    .replace("{RESPONDER_IMESSAGE_ID}", responderImessageId);
        }
    }

    public static class GroupChannelAddition {
        private static final String BASE_PATH = "group_channel_addition";

        public static class AdditionHashKey {
            public static final String UUID = "uuid";
            public static final String MESSAGE = "message";
            public static final String NOTE = "note";
            public static final String NEW_TAG_NAMES = "new_tag_names";
            public static final String TO_ADD_TAG_IDS = "to_add_tag_ids";
            public static final String REQUEST_TIME = "request_time";
            public static final String RESPOND_TIME = "respond_time";
            public static final String IS_ACCEPTED = "is_accepted";
            public static final String IS_VIEWED = "is_viewed";
            public static final String INVITE_UUID = "invite_uuid";
        }

        public static class InvitationHashKey {
            public static final String UUID = "uuid";
            public static final String MESSAGE = "message";
            public static final String REQUEST_TIME = "request_time";
            public static final String RESPOND_TIME = "respond_time";
            public static final String IS_ACCEPTED = "is_accepted";
            public static final String IS_VIEWED = "is_viewed";
        }

        private static final String REQUESTER = BASE_PATH + ":requester:{REQUESTER_IMESSAGE_ID}:{GROUP_CHANNEL_ID}:{UUID}";
        public static String requester(String requesterImessageId, String groupChannelId, String uuid){
            return REQUESTER
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId)
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId)
                    .replace("{UUID}", uuid);
        }

        private static final String RESPONDER = BASE_PATH + ":responder:{GROUP_CHANNEL_ID}:{REQUESTER_IMESSAGE_ID}:{UUID}";
        public static String responder(String groupChannelId, String requesterImessageId, String uuid){
            return RESPONDER
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId)
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId)
                    .replace("{UUID}", uuid);
        }

        private static final String REQUESTER_PREFIX_1 = BASE_PATH + ":requester:{REQUESTER_IMESSAGE_ID}:{GROUP_CHANNEL_ID}:";
        public static String requesterPrefix(String requesterImessageId, String groupChannelId){
            return REQUESTER_PREFIX_1
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId)
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId);
        }

        private static final String RESPONDER_PREFIX_1 = BASE_PATH + ":responder:{GROUP_CHANNEL_ID}:{REQUESTER_IMESSAGE_ID}:";
        public static String responderPrefix(String groupChannelId, String requesterImessageId){
            return RESPONDER_PREFIX_1
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId)
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId);
        }

        private static final String REQUESTER_PREFIX_2 = BASE_PATH + ":requester:{REQUESTER_IMESSAGE_ID}:";
        public static String requesterPrefix(String requesterImessageId){
            return REQUESTER_PREFIX_2
                    .replace("{REQUESTER_IMESSAGE_ID}", requesterImessageId);
        }

        private static final String RESPONDER_PREFIX_2 = BASE_PATH + ":responder:{GROUP_CHANNEL_ID}:";
        public static String responderPrefix(String groupChannelId){
            return RESPONDER_PREFIX_2
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId);
        }

        private static final String INVITER = BASE_PATH + ":inviter:{INVITER_IMESSAGE_ID}:{INVITEE_IMESSAGE_ID}:{GROUP_CHANNEL_ID}:{UUID}";
        public static String inviter(String inviterImessageId, String inviteeImessageId, String groupChannelId, String uuid){
            return INVITER
                    .replace("{INVITER_IMESSAGE_ID}", inviterImessageId)
                    .replace("{INVITEE_IMESSAGE_ID}", inviteeImessageId)
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId)
                    .replace("{UUID}", uuid);
        }

        private static final String INVITEE = BASE_PATH + ":invitee:{INVITEE_IMESSAGE_ID}:{INVITER_IMESSAGE_ID}:{GROUP_CHANNEL_ID}:{UUID}";
        public static String invitee(String inviteeImessageId, String inviterImessageId, String groupChannelId, String uuid){
            return INVITEE
                    .replace("{INVITER_IMESSAGE_ID}", inviterImessageId)
                    .replace("{INVITEE_IMESSAGE_ID}", inviteeImessageId)
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId)
                    .replace("{UUID}", uuid);
        }

        private static final String INVITER_PREFIX_1 = BASE_PATH + ":inviter:{INVITER_IMESSAGE_ID}:{INVITEE_IMESSAGE_ID}:{GROUP_CHANNEL_ID}:";
        public static String inviterPrefix(String inviterImessageId, String inviteeImessageId, String groupChannelId){
            return INVITER_PREFIX_1
                    .replace("{INVITER_IMESSAGE_ID}", inviterImessageId)
                    .replace("{INVITEE_IMESSAGE_ID}", inviteeImessageId)
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId);
        }

        private static final String INVITEE_PREFIX_1 = BASE_PATH + ":invitee:{INVITEE_IMESSAGE_ID}:{INVITER_IMESSAGE_ID}:{GROUP_CHANNEL_ID}:";
        public static String inviteePrefix(String inviteeImessageId, String inviterImessageId, String groupChannelId){
            return INVITEE_PREFIX_1
                    .replace("{INVITER_IMESSAGE_ID}", inviterImessageId)
                    .replace("{INVITEE_IMESSAGE_ID}", inviteeImessageId)
                    .replace("{GROUP_CHANNEL_ID}", groupChannelId);
        }

        private static final String INVITER_PREFIX_2 = BASE_PATH + ":inviter:{INVITER_IMESSAGE_ID}:";
        public static String inviterPrefix(String inviterImessageId){
            return INVITER_PREFIX_2
                    .replace("{INVITER_IMESSAGE_ID}", inviterImessageId);
        }

        private static final String INVITEE_PREFIX_2 = BASE_PATH + ":invitee:{INVITEE_IMESSAGE_ID}:";
        public static String inviteePrefix(String inviteeImessageId){
            return INVITEE_PREFIX_2
                    .replace("{INVITEE_IMESSAGE_ID}", inviteeImessageId);
        }

    }

    public static class Chat{
        private static final String BASE_PATH = "chat_message";

        private static final String CHAT_MESSAGE = BASE_PATH + ":{RECEIVER}:{UUID}";
        public static String getChatMessage(String receiverImessageId, String uuid){
            return CHAT_MESSAGE.replace("{RECEIVER}", receiverImessageId).replace("{UUID}", uuid);
        }

        public static class ChatMessageHashKey {
            public static final String TYPE = "type";
            public static final String UUID = "uuid";
            public static final String FROM = "from";
            public static final String TO = "to";
            public static final String TIME = "time";
            public static final String TEXT = "text";
            public static final String FILE_NAME = "file_name";
            public static final String IMAGE_ID = "image_id";
            public static final String FILE_ID = "file_id";
            public static final String VIDEO_ID = "video_id";
            public static final String VOICE_ID = "voice_id";
            public static final String UNSEND_MESSAGE_UUID = "unsend_message_uuid";
            public static final String EXPIRED_MESSAGE_COUNT = "expired_message_count";
        }

        private static final String CHAT_MESSAGE_PREFIX = BASE_PATH + ":{RECEIVER}:";

        public static String getChatMessagePrefix(String receiverImessageId) {
            return CHAT_MESSAGE_PREFIX.replace("{RECEIVER}", receiverImessageId);
        }

        public static class ChatMessageImageHashKey {
            public static final String IMAGE_ID = "image_id";
            public static final String IMAGE_FILE_NAME = "image_file_name";
            public static final String IMAGE = "image";
        }

        private static final String CHAT_MESSAGE_IMAGE = BASE_PATH + ":image:{IMAGE_ID}";
        public static String getChatMessageImage(String imageId){
            return CHAT_MESSAGE_IMAGE.replace("{IMAGE_ID}", imageId);
        }

        public static class ChatMessageFileHashKey {
            public static final String FILE_ID = "file_id";
            public static final String FILE_FILE_NAME = "file_file_name";
            public static final String FILE = "file";
        }

        private static final String CHAT_MESSAGE_FILE = BASE_PATH + ":file:{FILE_ID}";
        public static String getChatMessageFile(String fileId){
            return CHAT_MESSAGE_FILE.replace("{FILE_ID}", fileId);
        }

        public static class ChatMessageVideoHashKey {
            public static final String VIDEO_ID = "video_id";
            public static final String VIDEO_FILE_NAME = "video_file_name";
            public static final String VIDEO = "video";
        }

        private static final String CHAT_MESSAGE_VIDEO = BASE_PATH + ":video:{VIDEO_ID}";
        public static String getChatMessageVideo(String videoId){
            return CHAT_MESSAGE_VIDEO.replace("{VIDEO_ID}", videoId);
        }

        public static class ChatMessageVoiceHashKey {
            public static final String VOICE_ID = "voice_id";
            public static final String VOICE = "voice";
        }

        private static final String CHAT_MESSAGE_VOICE = BASE_PATH + ":voice:{VOICE_ID}";
        public static String getChatMessageVoice(String voiceId){
            return CHAT_MESSAGE_VOICE.replace("{VOICE_ID}", voiceId);
        }
    }

    public static class Broadcast{
        private static final String BASE_PATH = "broadcast";

        private static final String NEW_BROADCAST_LIKE = BASE_PATH + ":new_like:{TO}:{LIKE_ID}";
        public static String newBroadcastLike(String to, String likeId){
            return NEW_BROADCAST_LIKE.replace("{TO}", to).replace("{LIKE_ID}", likeId);
        }

        private static final String NEW_BROADCAST_COMMENT = BASE_PATH + ":new_comment:{TO}:{COMMENT_ID}";
        public static String newBroadcastComment(String to, String commentId){
            return NEW_BROADCAST_COMMENT.replace("{TO}", to).replace("{COMMENT_ID}", commentId);
        }

        private static final String NEW_BROADCAST_REPLY = BASE_PATH + ":new_reply:{TO}:{COMMENT_ID}";
        public static String newBroadcastReply(String to, String commentId){
            return NEW_BROADCAST_REPLY.replace("{TO}", to).replace("{COMMENT_ID}", commentId);
        }

    }

    public static class GroupChannelNotification {
        private static final String BASE_PATH = "group_channel_notification";

        public static class NotificationHashKey {
            public static final String PASSIVE = "passive";
            public static final String BY_WHOM = "by_whom";
            public static final String TIME = "time";
            public static final String IS_VIEWED = "is_viewed";
            public static final String TYPE = "type";
        }

        private static final String NOTIFICATION = BASE_PATH + ":{GROUP_CHANNEL_ID}:{CHANNEL_ID}:{UUID}";
        public static String notification(String groupChannelId, String channelId, String uuid){
            return NOTIFICATION.replace("{GROUP_CHANNEL_ID}", groupChannelId)
                    .replace("{CHANNEL_ID}", channelId)
                    .replace("{UUID}", uuid);
        }

        private static final String NOTIFICATION_PREFIX = BASE_PATH + ":{GROUP_CHANNEL_ID}:";
        public static String getNotificationPrefix(String groupChannelId) {
            return NOTIFICATION_PREFIX.replace("{GROUP_CHANNEL_ID}", groupChannelId);
        }

        private static final String NOTIFICATION_UUID = "*:{UUID}";
        public static String getNotificationUuid(String uuid){
            return NOTIFICATION_UUID.replace("{UUID}", uuid);
        }

    }
}

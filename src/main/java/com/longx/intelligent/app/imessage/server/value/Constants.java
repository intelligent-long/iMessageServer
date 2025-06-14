package com.longx.intelligent.app.imessage.server.value;

/**
 * Created by LONG on 2024/3/27 at 9:43 PM.
 */
public class Constants {
    public static final String APP_NAME = "iMessage Server";
    public static final String SESSION_ATTRIBUTE_KEY_USER = "user";
    public static final int VERIFY_CODE_SEND_INTERVAL_MINUTES = 1;
    public static final int VERIFY_CODE_MAX_FAILURE_TIMES = 5;
    public static final int LOGIN_MAX_FAILURE_TIMES = 10;
    public static final String[] IMESSAGE_ID_USER_INVALID_CONTENTS = {"imessage", "ichat", "long"};
    public static final String[] GROUP_CHANNEL_ID_USER_INVALID_CONTENTS = {"imessage", "ichat", "long"};
    public static final int CHANGE_IMESSAGE_ID_INTERVAL_DAYS = 90;
    public static final int CHANGE_GROUP_CHANNEL_ID_INTERVAL_DAYS = 30;
    public static final int AVATAR_SIZE = 4000;
    public static final int CHANNEL_ADDITION_RECORD_DURATION_DAY = 365;
    public static final int CHANNEL_ADDITION_ADDITION_DURATION_DAY = 7;
    public static final int GROUP_CHANNEL_ADDITION_RECORD_DURATION_DAY = 365;
    public static final int GROUP_CHANNEL_ADDITION_ADDITION_DURATION_DAY = 7;
    public static final int GROUP_CHANNEL_NOTIFICATION_RECORD_DURATION_DAY = 365;
    public static final int MAX_ALLOW_CHAT_VOICE_DURATION_SEC = 300;
    public static final int MAX_BROADCAST_IMAGE_COUNT = 30;
    public static final int MAX_BROADCAST_VIDEO_COUNT = 5;
    public static final int MAX_BROADCAST_TEXT_LENGTH = 1000;
    public static final int MAX_BROADCAST_VIDEO_FILE_SIZE_BYTE = 512 * 1024 * 1024;
    public static final int MAX_BROADCAST_COMMENT_TEXT_LENGTH = 300;
    public static final int MAX_ALLOW_UNSEND_MINUTES = 3;
    public static final String CONFIG_JSON_FILE_NAME = "imessage-server-properties.json";
    public static final int MAX_LOG_AREA_LINES = 5000;
    public static final int CHAT_MESSAGE_EXPIRATION_TIME_DAY = 365;
}

package com.longx.intelligent.app.imessage.server.mapper;

import com.longx.intelligent.app.imessage.server.data.Avatar;
import com.longx.intelligent.app.imessage.server.data.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * Created by LONG on 2024/3/29 at 11:24 PM.
 */
@Mapper
public interface UserMapper {

    User findUserByImessageId(String imessageId);

    User findUserByImessageIdUser(String imessageIdUser);

    User findUserByEmail(String email);

    int insertUser(User user);

    int updatePassword(String email, String passwordHash);

    int updateImessageIdUser(String newImessageIdUser, String imessageId);

    Date findImessageIdUserLastChangeTime(String imessageId);

    int updateImessageIdUserLastChangeTime(Date lastChangeTime, String imessageId);

    int updateUsername(String newUsername, String imessageId);

    int updateEmail(String newEmail, String imessageId);

    int updateSex(Integer sex, String imessageId);

    int changeRegion(Integer firstRegionAdcode, Integer secondRegionAdcode, Integer thirdRegionAdcode, String imessageId);

    int updateAvatar(Avatar avatar, byte[] data);

    int updateAvatarHashWithUser(String avatarHash, String imessageId);

    Avatar findAvatar(String avatarHash);

    Object findAvatarData(String avatarHash);
}

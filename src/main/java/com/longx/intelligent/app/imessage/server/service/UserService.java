package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.AmapDistrict;
import com.longx.intelligent.app.imessage.server.data.Avatar;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.mapper.AmapDistrictMapper;
import com.longx.intelligent.app.imessage.server.mapper.UserMapper;
import com.longx.intelligent.app.imessage.server.util.NanoIdUtil;
import com.longx.intelligent.app.imessage.server.util.StringUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by LONG on 2024/3/27 at 9:50 PM.
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmapDistrictMapper amapDistrictMapper;

    public synchronized String generateImessageId(){
        String imessageId = "imid_" + NanoIdUtil.randomNanoId();
        boolean isExist = userMapper.findUserByImessageId(imessageId) != null;
        if(isExist){
            return generateImessageId();
        }
        return imessageId;
    }

    public User findUserByImessageId(String imessageId) {
        return userMapper.findUserByImessageId(imessageId);
    }

    public User findUserByImessageIdUser(String imessageIdUser){
        return userMapper.findUserByImessageIdUser(imessageIdUser);
    }

    public User findUserByEmail(String email){
        return userMapper.findUserByEmail(email);
    }

    public boolean insertUser(User user){
        return userMapper.insertUser(user) == 1;
    }

    public boolean updatePassword(String email, String password){
//        String passwordHash = PasswordCrypto.hashPassword(password);
        String passwordHash = password;
        return userMapper.updatePassword(email, passwordHash) == 1;
    }

    public boolean isImessageIdUserValid(String imessageIdUser){
        for (String imessageIdInvalidContent : Constants.IMESSAGE_ID_USER_INVALID_CONTENTS) {
            if (StringUtil.containsIgnoreCase(imessageIdUser, imessageIdInvalidContent)) {
                return false;
            }
        }
        return true;
    }

    public Date findImessageIdUserLastChangeTime(String imessageId){
        return userMapper.findImessageIdUserLastChangeTime(imessageId);
    }

    public boolean updateImessageIdUser(String newImessageIdUser, String imessageId){
        return userMapper.updateImessageIdUser(newImessageIdUser, imessageId) == 1;
    }

    public boolean updateImessageIdUserLastChangeTime(Date lastChangeTime, String imessageId){
        return userMapper.updateImessageIdUserLastChangeTime(lastChangeTime, imessageId) == 1;
    }

    public boolean updateUsername(String newUsername, String imessageId){
        return userMapper.updateUsername(newUsername, imessageId) == 1;
    }

    public boolean updateEmail(String newEmail, String imessageId){
        return userMapper.updateEmail(newEmail, imessageId) == 1;
    }

    public boolean updateSex(Integer sex, String imessageId){
        return userMapper.updateSex(sex, imessageId) == 1;
    }

    public boolean updateAvatar(Avatar avatar, byte[] data){
        return userMapper.updateAvatar(avatar, data) == 1;
    }

    public boolean updateAvatarHashWithUser(String avatarHash, String imessageId){
        return userMapper.updateAvatarHashWithUser(avatarHash, imessageId) == 1;
    }

    public Avatar findAvatar(String avatarHash){
        return userMapper.findAvatar(avatarHash);
    }

    public byte[] findAvatarData(String avatarHash){
        return (byte[]) userMapper.findAvatarData(avatarHash);
    }
}

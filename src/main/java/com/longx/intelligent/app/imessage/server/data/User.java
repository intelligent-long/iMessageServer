package com.longx.intelligent.app.imessage.server.data;

import com.longx.intelligent.app.imessage.server.service.PermissionService;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by LONG on 2024/3/27 at 9:17 PM.
 */
@Component
public class User implements Serializable {
    private final String imessageId;
    private final String imessageIdUser;
    private final String email;
    private final String passwordHash;
    private final Date registerTime;
    private final String username;
    private final Avatar avatar;
    private final Integer sex;
    private final Region firstRegion;
    private final Region secondRegion;
    private final Region thirdRegion;

    public User() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public User(String imessageId, String imessageIdUser, String email, String password, Date registerTime, String username,
                Avatar avatar, Integer sex, Region firstRegion, Region secondRegion, Region thirdRegion) {
        this.imessageId = imessageId;
        this.imessageIdUser = imessageIdUser;
        this.email = email;
//        this.passwordHash = PasswordCrypto.hashPassword(password);
        this.passwordHash = password;
        this.registerTime = registerTime;
        this.username = username;
        this.avatar = avatar;
        this.sex = sex;
        this.firstRegion = firstRegion;
        this.secondRegion = secondRegion;
        this.thirdRegion = thirdRegion;
    }

    public String getImessageId() {
        return imessageId;
    }

    public String getImessageIdUser() {
        return imessageIdUser;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public String getUsername() {
        return username;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Integer getSex() {
        return sex;
    }

    public Region getFirstRegion() {
        return firstRegion;
    }

    public Region getSecondRegion() {
        return secondRegion;
    }

    public Region getThirdRegion() {
        return thirdRegion;
    }

    public static class UserProfileVisibility{
        private boolean emailVisible;
        private boolean sexVisible;
        private boolean regionVisible;

        public UserProfileVisibility() {
        }

        public UserProfileVisibility(boolean emailVisible, boolean sexVisible, boolean regionVisible) {
            this.emailVisible = emailVisible;
            this.sexVisible = sexVisible;
            this.regionVisible = regionVisible;
        }

        public boolean isEmailVisible() {
            return emailVisible;
        }

        public boolean isSexVisible() {
            return sexVisible;
        }

        public boolean isRegionVisible() {
            return regionVisible;
        }
    }

    public static class WaysToFindMe{
        private boolean byImessageIdUser;
        private boolean byEmail;

        public WaysToFindMe() {
        }

        public WaysToFindMe(boolean byImessageIdUser, boolean byEmail) {
            this.byImessageIdUser = byImessageIdUser;
            this.byEmail = byEmail;
        }

        public boolean isByImessageIdUser() {
            return byImessageIdUser;
        }

        public boolean isByEmail() {
            return byEmail;
        }
    }

    public record Self(String imessageId, String imessageIdUser, String email, Date registerTime, String username,
                       Avatar avatar, Integer sex, Region firstRegion, Region secondRegion, Region thirdRegion,
                       UserProfileVisibility userProfileVisibility, WaysToFindMe waysToFindMe) {
    }

    public Self getSelf(PermissionService permissionService){
        UserProfileVisibility userProfileVisibility = permissionService.findUserProfileVisibilityByImessageId(imessageId);
        WaysToFindMe waysToFindMe = permissionService.findWaysToFindMeByImessageId(imessageId);
        return new Self(imessageId, imessageIdUser, email, registerTime, username, avatar, sex, firstRegion, secondRegion, thirdRegion, userProfileVisibility, waysToFindMe);
    }
}

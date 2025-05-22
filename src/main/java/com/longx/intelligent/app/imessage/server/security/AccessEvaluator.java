package com.longx.intelligent.app.imessage.server.security;

import com.longx.intelligent.app.imessage.server.data.User;
import org.springframework.util.AntPathMatcher;

import java.util.Map;

/**
 * Created by LONG on 2024/1/13 at 1:23 AM.
 */
public class AccessEvaluator {
    private final AccessPolicyMapper accessPolicyMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccessEvaluator(AccessPolicyMapper accessPolicyMapper) {
        this.accessPolicyMapper = accessPolicyMapper;
    }

    public boolean access(String path, User user){
        for (Map.Entry<String, AccessPolicy> stringAccessPolicyEntry : accessPolicyMapper.getAccessControlMap().entrySet()) {
            String antPattern = stringAccessPolicyEntry.getKey();
            AccessPolicy accessPolicy = stringAccessPolicyEntry.getValue();
            if(pathMatcher.match(antPattern, path)){
                switch (accessPolicy.allowType()){
                    case ALL -> {
                        return true;
                    }
                    case AUTHED -> {
                        return user != null;
                    }
//                    case HAS_ROLE -> {
//                        if(user == null) return false;
//                        for (String role : accessPolicy.roles()) {
//                            if(user.getRoles().contains(role)){
//                                return true;
//                            }
//                        }
//                        return false;
//                    }
                }
            }
        }
        return false;
    }
}

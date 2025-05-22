package com.longx.intelligent.app.imessage.server.security;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by LONG on 2024/1/13 at 12:47 AM.
 */
@Component
public class AccessPolicyMapper {
    private final Map<String, AccessPolicy> accessControlMap = new LinkedHashMap<>();

    public class MapProcessor {
        private final String antPattern;

        public MapProcessor(String antPattern) {
            this.antPattern = antPattern;
        }

        public AccessPolicyMapper permitAll(){
            accessControlMap.put(antPattern, new AccessPolicy(AccessPolicy.AllowType.ALL, null));
            return AccessPolicyMapper.this;
        }

        public AccessPolicyMapper authed(){
            accessControlMap.put(antPattern, new AccessPolicy(AccessPolicy.AllowType.AUTHED, null));
            return AccessPolicyMapper.this;
        }

        public AccessPolicyMapper hasRole(String... roles){
            accessControlMap.put(antPattern, new AccessPolicy(AccessPolicy.AllowType.HAS_ROLE, roles));
            return AccessPolicyMapper.this;
        }
    }

    public MapProcessor antMatch(String antPattern){
        return new MapProcessor(antPattern);
    }

    public Map<String, AccessPolicy> getAccessControlMap() {
        return accessControlMap;
    }
}

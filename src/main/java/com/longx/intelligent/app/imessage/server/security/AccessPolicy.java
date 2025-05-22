package com.longx.intelligent.app.imessage.server.security;

/**
 * Created by LONG on 2024/1/13 at 12:55 AM.
 */
public record AccessPolicy(AccessPolicy.AllowType allowType,
                           String... roles) {
    public enum AllowType {ALL, AUTHED, HAS_ROLE}
}

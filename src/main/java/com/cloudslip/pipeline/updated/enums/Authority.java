package com.cloudslip.pipeline.updated.enums;

import org.springframework.security.core.GrantedAuthority;


public enum Authority implements GrantedAuthority {
    ROLE_DEV,
    ROLE_OPS,
    ROLE_ADMIN,
    ROLE_SUPER_ADMIN,
    ROLE_AGENT_SERVICE,
    ROLE_GIT_AGENT,
    ANONYMOUS;

    @Override
    public String getAuthority() {
        return this.name();
    }
}

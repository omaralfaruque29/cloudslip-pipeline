package com.cloudslip.pipeline.updated.model.universal;

import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.InitialSettingStatus;
import com.cloudslip.pipeline.updated.enums.UserType;
import com.cloudslip.pipeline.updated.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class User extends BaseEntity implements UserDetails {

    private static final long serialVersionUID = 7954325925563724664L;

    private String username;
    private String password;
    private String verificationCode;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean isEnabled;
    private boolean needToResetPassword;
    private List<Authority> authorities;
    private UserInfo userInfo;
    private UserType userType = UserType.REGULAR;
    private InitialSettingStatus initialSettingStatus;

    @Transient
    private ObjectId companyId;

    @Transient
    private ObjectId organizationId;

    @Transient
    private List<ObjectId> teamIdList;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void setAuthorities(final List<Authority> authorities) {
        this.authorities = authorities;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setAccountNonExpired(final boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(final boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(final boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setEnabled(final boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isNeedToResetPassword() {
        return needToResetPassword;
    }

    public void setNeedToResetPassword(boolean needToResetPassword) {
        this.needToResetPassword = needToResetPassword;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public InitialSettingStatus getInitialSettingStatus() {
        return initialSettingStatus;
    }

    public void setInitialSettingStatus(InitialSettingStatus initialSettingStatus) {
        this.initialSettingStatus = initialSettingStatus;
    }

    @JsonIgnore
    public ObjectId getCompanyId() {
        return companyId;
    }

    @JsonIgnore
    public String getCompanyIdAsString() {
        if(companyId != null) {
            return companyId.toHexString();
        }
        return "";
    }

    public void setCompanyId(ObjectId companyId) {
        this.companyId = companyId;
    }

    @JsonIgnore
    public ObjectId getOrganizationId() {
        return organizationId;
    }

    @JsonIgnore
    public String getOrganizationIdAsString() {
        if(organizationId != null) {
            return organizationId.toHexString();
        }
        return "";
    }

    public void setOrganizationId(ObjectId organizationId) {
        this.organizationId = organizationId;
    }

    @JsonIgnore
    public List<ObjectId> getTeamIdList() {
        return teamIdList;
    }

    @JsonIgnore
    public String getTeamIdsAsString() {
        String teamIdsStr = "";
        if(this.teamIdList != null && this.teamIdList.size() > 0) {
            for (ObjectId teamId: teamIdList) {
                teamIdsStr = teamIdsStr.concat(teamId.toHexString().concat(","));
            }
            if(teamIdsStr.length() > 0) {
                teamIdsStr = teamIdsStr.substring(0, teamIdsStr.length() - 1);
            }
        }
        return teamIdsStr;
    }

    public void setTeamIdList(List<ObjectId> teamIdList) {
        this.teamIdList = teamIdList;
    }

    public boolean hasAuthority(Authority authority) {
        return authorities.contains(authority);
    }

    public boolean hasAnyAuthority(Authority ...authorities) {
        for (Authority authority : authorities) {
            if(this.authorities.contains(authority)) return true;
        }
        return false;
    }

    public boolean hasAnyAuthority(List<Authority> authorities) {
        for (Authority authority : authorities) {
            if(this.authorities.contains(authority)) return true;
        }
        return false;
    }

    public boolean hasAllAuthority(Authority ...authorities) {
        for (Authority authority : authorities) {
            if(!this.authorities.contains(authority)) return false;
        }
        return true;
    }

    @JsonIgnore
    public String getAuthoritiesAsString() {
        String authoritiesStr = "";
        for (Authority authority: authorities) {
            authoritiesStr = authoritiesStr.concat(authority.getAuthority().concat(","));
        }
        if(authoritiesStr.length() > 0) {
            authoritiesStr = authoritiesStr.substring(0, authoritiesStr.length() - 1);
        }
        return authoritiesStr;
    }

    @JsonIgnore
    public String hasTeam(ObjectId teamId) {

        if (this.userInfo.getTeams() == null) {
            return "no_team";   /* User do not assigned to any team */
        }
        for (Team team: this.userInfo.getTeams()) {
            if (team.getObjectId().toString().equals(teamId.toString())) {
                return "exists";
            }
        }
        return "not_exists";
    }

    public String toJsonString() {
        return
            "{" +
                "\"id\": \"" + getId() + "\"" +
                ", \"username\": \"" + username + "\"" +
                ", \"isEnabled\": " + isEnabled +
                ", \"authorities\": " + "\"" + getAuthoritiesAsString() + "\"" +
                ", \"companyId\": " + "\"" + getCompanyIdAsString() + "\"" +
                ", \"organizationId\": " + "\"" + getOrganizationIdAsString() + "\"" +
                ", \"teamIds\": " + "\"" + getTeamIdsAsString() + "\"" +
                "}";
    }
}

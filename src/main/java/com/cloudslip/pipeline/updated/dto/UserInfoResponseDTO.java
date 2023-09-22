package com.cloudslip.pipeline.updated.dto;


import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.model.universal.Team;
import com.cloudslip.pipeline.updated.model.universal.UserInfo;

import java.util.List;

public class UserInfoResponseDTO {
    private UserInfo userInfo;
    private List<Team> companyTeamList;
    private Company companyInfo;
    public UserInfoResponseDTO() {
    }

    public UserInfoResponseDTO(UserInfo userInfo, List<Team> companyTeamList, Company companyInfo) {
        this.userInfo = userInfo;
        this.companyTeamList = companyTeamList;
        this.companyInfo = companyInfo;
    }

    public Company getCompanyInfo() {
        return companyInfo;
    }

    public void setCompanyInfo(Company companyInfo) {
        this.companyInfo = companyInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<Team> getCompanyTeamList() {
        return companyTeamList;
    }

    public void setCompanyTeamList(List<Team> companyTeamList) {
        this.companyTeamList = companyTeamList;
    }
}

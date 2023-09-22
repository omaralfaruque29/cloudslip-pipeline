package com.cloudslip.pipeline.updated.model.universal;

import com.cloudslip.pipeline.updated.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Team extends BaseEntity {

    private String name;
    private String description;
    private ObjectId companyId;
    private Organization organization;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyId() {
        return companyId.toHexString();
    }

    @JsonIgnore
    public ObjectId getCompanyObjectId() {
        return companyId;
    }

    public void setCompanyId(ObjectId companyId) {
        this.companyId = companyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public boolean existIn(List<Team> teamList) {
        for (Team team : teamList) {
            if (this.getObjectId().toString().equals(team.getObjectId().toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean existInTeamIdList(List<ObjectId> teamIdList) {
        for (ObjectId teamId : teamIdList) {
            if (this.getObjectId().toString().equals(teamId.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOrganization(List<Organization> organizationList) {
        for(Organization organization: organizationList) {
            if(this.getOrganization().getObjectId().toString().equals(organization.getObjectId().toString())) {
                return true;
            }
        }
       return false;
    }

    public static List<ObjectId> getTeamIdList(List<Team> teamList) {
        List<ObjectId> teamIdList = new ArrayList<>();
        for (Team team: teamList) {
            teamIdList.add(team.getObjectId());
        }
        return teamIdList;
    }
}

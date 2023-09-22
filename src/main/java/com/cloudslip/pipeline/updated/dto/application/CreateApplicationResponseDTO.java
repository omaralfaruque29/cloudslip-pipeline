package com.cloudslip.pipeline.updated.dto.application;

import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.model.universal.EnvironmentOption;
import com.cloudslip.pipeline.updated.model.universal.Team;

public class CreateApplicationResponseDTO {

    private Company company;
    private Team team;
    private EnvironmentOption environmentOption;

    public CreateApplicationResponseDTO() {
    }

    public CreateApplicationResponseDTO(Company company, Team team, EnvironmentOption environmentOption) {
        this.company = company;
        this.team = team;
        this.environmentOption = environmentOption;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public EnvironmentOption getEnvironmentOption() {
        return environmentOption;
    }

    public void setEnvironmentOption(EnvironmentOption environmentOption) {
        this.environmentOption = environmentOption;
    }
}

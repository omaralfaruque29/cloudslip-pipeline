package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.constant.ListFetchMode;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.universal.Team;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetApplicationListHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(GetApplicationListHelper.class);

    private GetListFilterInput input;
    private ResponseDTO output = new ResponseDTO();
    private Pageable pageable;

    private ResponseDTO response = new ResponseDTO();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ApplicationRepository applicationRepository;

    private List<Team> teamList;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetListFilterInput) input;
        this.setOutput(output);
        this.response = (ResponseDTO) extraParams[0];
        this.pageable = (Pageable) extraParams[1];
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.teamList = objectMapper.convertValue(response.getData(), new TypeReference<List<Team>>() { });
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
    }


    protected void doPerform() {
        if (requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            this.fetchForSuperAdmin();
        } else {
            this.fetchForOtherUsers();
        }
    }


    protected void postPerformCheck() {
    }

    private void fetchForSuperAdmin(){
        if(input.getFetchMode() == null || input.getFetchMode().equals(ListFetchMode.PAGINATION)) {
            output.generateSuccessResponse(applicationRepository.findAllByStatus(pageable, Status.V));
        } else if(input.getFetchMode() != null || input.getFetchMode().equals(ListFetchMode.ALL)) {
            output.generateSuccessResponse(applicationRepository.findAllByStatus(Status.V));
        }
    }

    private void fetchForOtherUsers(){
        if(input.getFetchMode() == null || input.getFetchMode().equals(ListFetchMode.PAGINATION)) {
            output.generateSuccessResponse(applicationRepository.findAllByTeamIdInAndStatus(pageable, Team.getTeamIdList(teamList), Status.V));
        } else if(input.getFetchMode() != null || input.getFetchMode().equals(ListFetchMode.ALL)) {
            output.generateSuccessResponse(applicationRepository.findAllByTeamIdInAndStatus(Team.getTeamIdList(teamList), Status.V));
        }
    }

    protected void doRollback() {

    }
}

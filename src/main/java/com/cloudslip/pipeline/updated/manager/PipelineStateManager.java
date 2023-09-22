package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class PipelineStateManager implements PipelineStateManagerInterface {

    private final Logger log = LoggerFactory.getLogger(PipelineStateManager.class);


    @Override
    public ResponseDTO init(CreateAppCommitDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO getState(GetObjectInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO resetState(ResetAppCommitStateInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO updateState(AppCommitState state, User requester) {
        return null;
    }

    @Override
    public ResponseDTO deleteState(DeleteObjectInputDTO input, User requester) {
        return null;
    }

}

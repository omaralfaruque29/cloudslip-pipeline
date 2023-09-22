package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import com.cloudslip.pipeline.updated.model.universal.User;

public interface PipelineStateManagerInterface {

    public ResponseDTO init(CreateAppCommitDTO input, User requester);

    public ResponseDTO getState(GetObjectInputDTO input, User requester);

    public ResponseDTO resetState(ResetAppCommitStateInputDTO input, User requester);

    public ResponseDTO updateState(AppCommitState state, User requester);

    public ResponseDTO deleteState(DeleteObjectInputDTO input, User requester);

}

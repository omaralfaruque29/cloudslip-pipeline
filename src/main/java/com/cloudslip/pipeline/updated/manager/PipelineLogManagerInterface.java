package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.model.universal.User;

public interface PipelineLogManagerInterface {

    public ResponseDTO getPipelineStepLog(GetPipelineStepLogInputDTO input, User requester);

    public ResponseDTO downloadPipelineStepLog(DownloadPipelineStepLogInputDTO input, User requester);

    public ResponseDTO broadcastPipelineStepLog(BroadcastPipelineStepLogInputDTO input, User requester);
}

package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class PipelineLogManager implements PipelineLogManagerInterface {

    private final Logger log = LoggerFactory.getLogger(PipelineLogManager.class);


    @Override
    public ResponseDTO getPipelineStepLog(GetPipelineStepLogInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO downloadPipelineStepLog(DownloadPipelineStepLogInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO broadcastPipelineStepLog(BroadcastPipelineStepLogInputDTO input, User requester) {
        return null;
    }
}

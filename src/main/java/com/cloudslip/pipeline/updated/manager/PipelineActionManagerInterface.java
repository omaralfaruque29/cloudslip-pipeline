package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.model.universal.User;

public interface PipelineActionManagerInterface {

    public ResponseDTO triggerPipelineFromStart(TriggerPipelineFromStartInputDTO input, User requester);

    public ResponseDTO triggerPipelineStep(TriggerPipelineStepInputDTO input, User requester);

    public ResponseDTO deployInAppVpc(DeployInAppVpcInputDTO input, User requester);

    public ResponseDTO removeDeploymentFromAppVpc(RemoveDeploymentInAppVpcInputDTO input, User requester);

    public ResponseDTO updateDeploymentTraffic(UpdateDeploymentTrafficInputDTO input, User requester);

    public ResponseDTO updateAppEnvironmentStateChecklist(UpdateAppEnvironmentStateChecklistInputDTO input, User requester);

    public ResponseDTO enableAppEnvironment(EnableAppEnvironmentInputDTO input, User requester);

    public ResponseDTO disableAppEnvironment(DisableAppEnvironmentInputDTO input, User requester);

    public ResponseDTO enablePipelineStep(EnableAppPipelineStepInputDTO input, User requester);

    public ResponseDTO disablePipelineStep(DisableAppEnvironmentInputDTO input, User requester);

    public ResponseDTO appPipelineStepStatusUpdate(AppPipelineStepStatusUpdateDTO input, User requester);
}

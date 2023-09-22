package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.helper.pipeline.*;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PipelineActionManager implements PipelineActionManagerInterface {

    private final Logger log = LoggerFactory.getLogger(PipelineActionManager.class);

    @Autowired
    private TriggerPipelineBuildStepHelper triggerPipelineStepHelper;

    @Autowired
    private DeployInAppVpcHelper deployInAppVpcHelper;

    @Autowired
    private RunAppPipelineStepHelper runAppPipelineStepHelper;

    @Autowired
    private AppPipelineStepStatusUpdateHelper appPipelineStepStatusUpdateHelper;

    @Autowired
    private RollbackDeployedApplicationHelper rollbackDeployedApplicationHelper;



    @Override
    public ResponseDTO triggerPipelineFromStart(TriggerPipelineFromStartInputDTO input, User requester) {
        log.debug("Request to trigger pipeline: {}", input.getCommitId());
        return (ResponseDTO) triggerPipelineStepHelper.execute(input, requester);
    }

    @Override
    public ResponseDTO triggerPipelineStep(TriggerPipelineStepInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO deployInAppVpc(DeployInAppVpcInputDTO input, User requester) {
        return (ResponseDTO) deployInAppVpcHelper.execute(input, requester, null);
    }

    /**
     * Doing Rollback in deployed application
     *
     * @param appPipelineStepId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO rollbackDeployment(ObjectId appPipelineStepId, User requester, ObjectId actionId) {
        log.debug("REST request to rollback deployed application : {}", appPipelineStepId);
        return (ResponseDTO) rollbackDeployedApplicationHelper.execute(new GetObjectInputDTO(appPipelineStepId), requester, actionId);
    }

    public ResponseDTO runPipelineStep(RunAppPipelineStepInputDTO input, User requester) {
        return (ResponseDTO) runAppPipelineStepHelper.execute(input, requester);
    }

    @Override
    public ResponseDTO removeDeploymentFromAppVpc(RemoveDeploymentInAppVpcInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO updateDeploymentTraffic(UpdateDeploymentTrafficInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO updateAppEnvironmentStateChecklist(UpdateAppEnvironmentStateChecklistInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO enableAppEnvironment(EnableAppEnvironmentInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO disableAppEnvironment(DisableAppEnvironmentInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO enablePipelineStep(EnableAppPipelineStepInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO disablePipelineStep(DisableAppEnvironmentInputDTO input, User requester) {
        return null;
    }

    @Override
    public ResponseDTO appPipelineStepStatusUpdate(AppPipelineStepStatusUpdateDTO input, User requester) {
        log.debug("Request to trigger pipeline: {}", input.getCommitId());
        return (ResponseDTO) appPipelineStepStatusUpdateHelper.execute(input, requester);
    }
}

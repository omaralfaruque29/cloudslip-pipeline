package com.cloudslip.pipeline.updated.service;


import com.cloudslip.pipeline.updated.dto.CreateAppCommitPipelineStepDTO;
import com.cloudslip.pipeline.updated.dto.CreateAppCommitPipelineStepForAddEnvDTO;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.helper.app_commit_pipeline_step.CreateAppCommitPipelineStepForAddEnvHelper;
import com.cloudslip.pipeline.updated.helper.app_commit_pipeline_step.CreateAppCommitPipelineStepHelper;
import com.cloudslip.pipeline.updated.helper.app_commit_pipeline_step.DeleteAllAppCommitPipelineStepHelper;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppCommitPipelineStepService {

    private final Logger log = LoggerFactory.getLogger(AppCommitPipelineStepService.class);

    @Autowired
    CreateAppCommitPipelineStepHelper createAppCommitPipelineStepHelper;

    @Autowired
    DeleteAllAppCommitPipelineStepHelper deleteAllAppCommitPipelineStepHelper;

    @Autowired
    CreateAppCommitPipelineStepForAddEnvHelper createAppCommitPipelineStepForAddEnvHelper;

    /**
     * Create App Commit Pipe Line Step For App Commit State and return list.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO createAppCommitPipelineStep(CreateAppCommitPipelineStepDTO input, User requester, ObjectId actionId) {
        log.debug("Request to all pipeline step : {}", input);
        return (ResponseDTO) createAppCommitPipelineStepHelper.execute(input, requester, actionId);
    }


    /**
     * Create App Commit Pipe Line Step While Adding Or Customizing App Environments
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO createAppCommitPipelineStepForAddEnv(CreateAppCommitPipelineStepForAddEnvDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create each pipeline step : {}", input);
        return (ResponseDTO) createAppCommitPipelineStepForAddEnvHelper.execute(input, requester, actionId);
    }
    /**
     * Delete App Commits List for For application Environment.
     *
     * @param appCommitId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAllAppCommitPipelineStep(GetObjectInputDTO appCommitId, User requester, ObjectId actionId) {
        log.debug("Request to delete app commit pipeline step list: {}", appCommitId);
        return (ResponseDTO) deleteAllAppCommitPipelineStepHelper.execute(appCommitId, requester, actionId);
    }
}

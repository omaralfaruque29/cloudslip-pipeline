package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.app_pipeline_step.CreateAppPipelineStepDTO;
import com.cloudslip.pipeline.updated.helper.app_pipe_line_step.*;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AppPipelineStepService {

    private final Logger log = LoggerFactory.getLogger(AppPipelineStepService.class);

    @Autowired
    CreateAppPipeLineStepListHelper createAppPipeLineStepListHelper;

    @Autowired
    CreateAppPipeLineStepForAppCreateHelper createAppPipeLineStepForAppCreateHelper;

    @Autowired
    AddPipeLineStepToAppEnvHelper addPipeLineStepToAppEnvHelper;

    @Autowired
    GetAppPipeLineStepListHelper getAppPipeLineStepListHelper;

    @Autowired
    UpdatePipelineStepSuccessorHelper updatePipelineStepSuccessorHelper;

    @Autowired
    DeleteAllAppPipelineStepHelper deleteAllAppPipelineStepHelper;

    @Autowired
    AutoSuccessorAddHelper autoSuccessorAddHelper;


    /**
     * Add Custom Pipeline Step to a Environment.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO addPipelineStepToAppEnv(AddCustomPipelineStepDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to add custom pipeline step to application environment : {}", input);
        return (ResponseDTO) addPipeLineStepToAppEnvHelper.execute(input, requester, actionId);
    }
    /**
     * Update Pipeline Step Successor List
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO updateSuccessors(UpdatePipelineStepSuccessorDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to add custom pipeline step to application environment : {}", input);
        return (ResponseDTO) updatePipelineStepSuccessorHelper.execute(input, requester, actionId);
    }

    /**
     * Auto Pipeline Successor Add (App Environment Add/Update Before Template Create)
     *
     * @param applicationId the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO autoSuccessorCreate(ObjectId applicationId, User requester, ObjectId actionId) {
        log.debug("REST request to auto successor add : {}", applicationId);
        return (ResponseDTO) autoSuccessorAddHelper.execute(new GetObjectInputDTO(applicationId), requester, actionId);
    }

    /**
     * Add PipeLineSteps For application Environment.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO createAppPipeLineStepForAppCreate(CreateAppPipelineStepDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create app pipe line step for each app vpc: {}", input);
        return (ResponseDTO) createAppPipeLineStepForAppCreateHelper.execute(input, requester, actionId);
    }
    /**
     * Add PipeLineSteps For application Environment.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO createAppPipeLineStepList(CreateAppPipelineStepDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create app pipe line step for each app vpc: {}", input);
        return (ResponseDTO) createAppPipeLineStepListHelper.execute(input, requester, actionId);
    }

    /**
     * Get List of Pipe Line Steps by application Environment.
     *
     * @param input to get
     * @return the persisted entity
     */
    public ResponseDTO findAll(GetListFilterInput input, User requester, Pageable pageable) {
        log.debug("REST request to get App Pipeline Step list by application Environment : {}", input);
        return (ResponseDTO) getAppPipeLineStepListHelper.execute(input, requester, pageable);
    }

    /**
     * Delete App Vpc List for For application Environment.
     *
     * @param appEnvironmentId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAllAppPipelineStep(GetObjectInputDTO appEnvironmentId,  User requester, ObjectId actionId) {
        log.debug("Request to delete all pipeline step : {}", appEnvironmentId);
        return (ResponseDTO) deleteAllAppPipelineStepHelper.execute(appEnvironmentId, requester, actionId);
    }

}

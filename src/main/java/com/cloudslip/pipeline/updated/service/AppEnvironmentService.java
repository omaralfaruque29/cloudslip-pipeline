package com.cloudslip.pipeline.updated.service;


import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.app_environment.AddAppEnvironmentsDTO;
import com.cloudslip.pipeline.updated.dto.app_environment.AddDevelopmentEnvironmentDTO;
import com.cloudslip.pipeline.updated.helper.app_environment.*;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.model.universal.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AppEnvironmentService {

    private final Logger log = LoggerFactory.getLogger(AppEnvironmentService.class);

    @Autowired
    AddEnvironmentsHelper addEnvironmentsHelper;

    @Autowired
    AddPipelineStepToListHelper addPipelineStepToListHelper;

    @Autowired
    UpdatePipelineStepToListHelper updatePipelineStepToListHelper;

    @Autowired
    GetAppEnvironmentListHelper getAppEnvironmentListHelper;

    @Autowired
    AddDevelopmentEnvironmentHelper addDevelopmentEnvironmentHelper;

    @Autowired
    DeleteAppEnvironmentHelper deleteAppEnvironmentHelper;


    /**
     * Add Environments to a application.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO addEnvironments(AddAppEnvironmentsDTO input, User requester, ObjectId actionId, ResponseDTO response) {
        log.debug("REST request to create an application : {}", input);
        return (ResponseDTO) addEnvironmentsHelper.execute(input, requester, actionId, response);
    }


    /**
     * Add Development Environment While Creating An application
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO addDevelopmentEnvironment(AddDevelopmentEnvironmentDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to add development environment for applicaiton : {}", input);
        return (ResponseDTO) addDevelopmentEnvironmentHelper.execute(input, requester, actionId);
    }

    /**
     * Delete App Environments By application Id
     *
     * @param appId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAppEnvironments(GetObjectInputDTO appId, User requester, ObjectId actionId) {
        log.debug("REST request to delete App Environments For An application: {}", appId);
        return (ResponseDTO) deleteAppEnvironmentHelper.execute(appId, requester, actionId);
    }

    /**
     * Get List of Environments Response by application ID.
     *
     * @param input to get
     * @return the persisted entity
     */
    public ResponseDTO findAll(GetListFilterInput input, User requester, Pageable pageable) {
        log.debug("REST request to get App Environment list by application : {}", input);
        return (ResponseDTO) getAppEnvironmentListHelper.execute(input, requester, pageable);
    }

    /**
     * Add Pipeline Step to a application Environment Pipeline Step List.
     *
     * @param appPipelineStep the entity to save
     * @return the persisted entity
     */
    public ResponseDTO addPipelineStepToList(AppPipelineStep appPipelineStep, ObjectId appEnvironmentId, User requester, ObjectId actionId) {
        log.debug("REST request to create an application : {}", appPipelineStep);
        return (ResponseDTO) addPipelineStepToListHelper.execute(new AddPipelineStepToListDTO(appPipelineStep, appEnvironmentId), requester, actionId);
    }

    /**
     * Update Pipeline Step List In to a application Environment Pipeline Step List.
     *
     * @param appPipelineStep the entity to save
     * @return the persisted entity
     */
    public ResponseDTO updatePipelineStepToList(AppPipelineStep appPipelineStep, ObjectId appEnvironmentId, User requester, ObjectId actionId) {
        log.debug("REST request to create an application : {}", appPipelineStep);
        return (ResponseDTO) updatePipelineStepToListHelper.execute(new AddPipelineStepToListDTO(appPipelineStep, appEnvironmentId), requester, actionId);
    }
}

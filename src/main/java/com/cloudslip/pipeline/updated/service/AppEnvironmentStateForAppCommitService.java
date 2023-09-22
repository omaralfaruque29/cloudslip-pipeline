package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.AppEnvStateForAddEnvironmentDTO;
import com.cloudslip.pipeline.updated.dto.AppEnvStateForAppCommitDTO;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.helper.app_env_for_app_commit.CreateAppEnvStateForAddEnvironmentHelper;
import com.cloudslip.pipeline.updated.helper.app_env_for_app_commit.CreateAppEnvStateForAppCommitHelper;
import com.cloudslip.pipeline.updated.helper.app_env_for_app_commit.DeleteAllAppEnvStateForAppCommitHelper;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppEnvironmentStateForAppCommitService {

    private final Logger log = LoggerFactory.getLogger(AppEnvironmentStateForAppCommitService.class);

    @Autowired
    CreateAppEnvStateForAppCommitHelper createAppEnvStateForAppCommitHelper;

    @Autowired
    DeleteAllAppEnvStateForAppCommitHelper deleteAllAppEnvStateForAppCommitHelper;

    @Autowired
    CreateAppEnvStateForAddEnvironmentHelper appEnvStateForAddEnvironmentHelper;

    /**
     * Create App Environment State For App Commit and return the list.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO createAppEnvStateForApp(AppEnvStateForAppCommitDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create all app env state for all app : {}", input);
        return (ResponseDTO) createAppEnvStateForAppCommitHelper.execute(input, requester, actionId);
    }

    /**
     * Create App Environment State For App Commit and return the list For Add App Environment
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO createAppEnvStateForAddEnvironment(AppEnvStateForAddEnvironmentDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create app commit state : {}", input);
        return (ResponseDTO) appEnvStateForAddEnvironmentHelper.execute(input, requester, actionId);
    }

    /**
     * Delete App Environment State List By application Environment Id
     *
     * @param appEnvironmentId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAllAppEnvStateForAppCommit(GetObjectInputDTO appEnvironmentId, User requester, ObjectId actionId) {
        log.debug("REST request to delete App Environments State List: {}", appEnvironmentId);
        return (ResponseDTO) deleteAllAppEnvStateForAppCommitHelper.execute(appEnvironmentId, requester, actionId);
    }
}

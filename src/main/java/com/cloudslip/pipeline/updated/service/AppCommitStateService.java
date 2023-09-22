package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.helper.app_commit_state.*;
import com.cloudslip.pipeline.updated.model.universal.User;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AppCommitStateService {

    private final Logger log = LoggerFactory.getLogger(AppCommitStateService.class);

    @Autowired
    GenerateAppCommitStateHelper generateAppCommitStateHelper;

    @Autowired
    GetAppCommitStateHelper getAppCommitStateHelper;

    @Autowired
    SyncAppCommitStateHelper syncAppCommitStateHelper;

    @Autowired
    DeleteAppCommitStateHelper deleteAppCommitStateHelper;

    @Autowired
    CheckAppEnvStateCheckListHelper checkAppEnvStateCheckListHelper;

    /**
     * Create application commit state
     *
     * @param payloadInput the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO generate(ObjectId applicationId, String payloadInput, String userAgent, String githubDelivery, String githubEvent, User requester, ObjectId actionId) {
        log.debug("REST request to generate App Commit State : {}", payloadInput);
        return (ResponseDTO) generateAppCommitStateHelper.execute(new GenerateAppCommitStateDTO(applicationId, payloadInput, userAgent, githubDelivery, githubEvent),
                requester, actionId);
    }

    /**
     * Get List of Applications Commit States
     *
     * @param requester to get
     * @return the persisted entity
     */
    public ResponseDTO findAll(ObjectId input, User requester) {
        log.debug("REST request to get application List");
        return (ResponseDTO) getAppCommitStateHelper.execute(new GetObjectInputDTO(input), requester);
    }

    /**
     * Sync And Save Git Commits
     *
     * @param requester to get
     * @return the persisted entity
     */
    public ResponseDTO sync(ObjectId input, User requester) {
        log.debug("REST request to get application List");
        return (ResponseDTO) syncAppCommitStateHelper.execute(new GetObjectInputDTO(input), requester);
    }

    /**
     * Delete App Commit State By application Commit Id
     *
     * @param appCommitId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAppCommitState(GetObjectInputDTO appCommitId, User requester, ObjectId actionId) {
        log.debug("REST request to delete App Environments: {}", appCommitId);
        return (ResponseDTO) deleteAppCommitStateHelper.execute(appCommitId, requester, actionId);
    }

    /**
     * Check And Uncheck Check Items of Checklist In App Env State for App Commit
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO checkAppEnvStateCheckList(CheckAppEnvStateChecklistDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to check app env state checklist of app commit state: {}", input);
        return (ResponseDTO) checkAppEnvStateCheckListHelper.execute(input, requester, actionId);
    }
}

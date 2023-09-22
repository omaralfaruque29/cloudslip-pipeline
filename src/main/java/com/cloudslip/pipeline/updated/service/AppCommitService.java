package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.CreateAppCommitDTO;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.helper.app_commit.CreateAppCommitHelper;
import com.cloudslip.pipeline.updated.helper.app_commit.DeleteAllAppCommitHelper;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppCommitService {

    private final Logger log = LoggerFactory.getLogger(AppCommitService.class);

    @Autowired
    CreateAppCommitHelper createAppCommitHelper;

    @Autowired
    DeleteAllAppCommitHelper deleteAllAppCommitHelper;

    /**
     * Create App Commit For App Commit State.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    @Transactional
    public ResponseDTO create(CreateAppCommitDTO input, User requester) {
        log.debug("Request to create app commit : {}", input);
        return (ResponseDTO) createAppCommitHelper.execute(input, requester);
    }

    /**
     * Delete App Commits List for For application
     *
     * @param appId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAllAppCommit(GetObjectInputDTO appId, User requester, ObjectId actionId) {
        log.debug("Request to delete all app commits for an application: {}", appId);
        return (ResponseDTO) deleteAllAppCommitHelper.execute(appId, requester, actionId);
    }
}

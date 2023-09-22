package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.helper.app_environment_checklist.AddChecklistHelper;
import com.cloudslip.pipeline.updated.helper.app_environment_checklist.DeleteChecklistHelper;
import com.cloudslip.pipeline.updated.helper.app_environment_checklist.GetCheckListHelper;
import com.cloudslip.pipeline.updated.helper.app_environment_checklist.UpdateChecklistHelper;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentChecklistRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AppEnvironmentChecklistService {

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger log = LoggerFactory.getLogger(AppEnvironmentChecklistService.class);

    @Autowired
    AppEnvironmentChecklistRepository appEnvironmentChecklistRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AddChecklistHelper addChecklistHelper;

    @Autowired
    UpdateChecklistHelper updateChecklistHelper;

    @Autowired
    GetCheckListHelper getCheckListHelper;

    @Autowired
    DeleteChecklistHelper deleteChecklistHelper;

    /**
     * Add app environment check list for Environment.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO createChecklist(AddAppEnvironmentChecklistDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to add checklist to application environment: {}", input);
        return (ResponseDTO) addChecklistHelper.execute(input, requester, actionId);
    }

    /**
     * Update app environment check list for Environment.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO updateChecklist(UpdateAppEnvironmentChecklistDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to update checklist to application environment: {}", input);
        return (ResponseDTO) updateChecklistHelper.execute(input, requester, actionId);
    }

    /**
     * Get Checklist by application and app environment.
     *
     * @param applicationId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO findChecklist(ObjectId applicationId, ObjectId appEnvId, User requester) {
        log.debug("REST request to get App Environment Checklist by application and App Environment : {}", applicationId);
        return (ResponseDTO) getCheckListHelper.execute(new GetChecklistDTO(applicationId, appEnvId), requester);
    }

    /**
     * Delete App Env Checklist.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteChecklist(DeleteChecklistDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to delete application environment checklist : {}", input.getAppEnvChecklistId());
        return (ResponseDTO) deleteChecklistHelper.execute(input, requester, actionId);
    }
}

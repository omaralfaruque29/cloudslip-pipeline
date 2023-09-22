package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.application.AddApplicationAdvanceConfigDTO;
import com.cloudslip.pipeline.updated.dto.application.CreateApplicationDTO;
import com.cloudslip.pipeline.updated.dto.application.UpdateApplicationDTO;
import com.cloudslip.pipeline.updated.helper.application.*;
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
public class ApplicationService {

    private final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private CreateApplicationHelper createApplicationHelper;

    @Autowired
    private UpdateApplicationHelper updateApplicationHelper;

    @Autowired
    private DeleteApplicationHelper deleteApplicationHelper;

    @Autowired
    private GetApplicationHelper getApplicationHelper;

    @Autowired
    private GetApplicationListHelper getApplicationListHelper;

    @Autowired
    private CreateApplicationTemplateHelper createApplicationTemplateHelper;

    @Autowired
    private IsUserAllowedToSubscribeToApplicationWebSocketTopicHelper isUserAllowedToSubscribeToApplicationWebSocketTopicHelper;

    @Autowired
    private UpdateApplicationWebSocketSubscriberCountHelper updateApplicationWebSocketSubscriberCountHelper;


    @Autowired
    ConfigUpdateApplicationTemplateHelper configUpdateApplicationTemplateHelper;

    @Autowired
    AddApplicationAdvanceConfigHelper addApplicationAdvanceConfigHelper;

    @Autowired
    private GetApplicationDeploymentStatusHelper getApplicationDeploymentStatusHelper;

    @Autowired
    private GetApplicationTeamHelper getApplicationTeamHelper;

    @Autowired
    private GetApplicationAndAppVpcListByVpcHelper getApplicationAndAppVpcListByVpcHelper;

    /**
     * Create an application.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO create(CreateApplicationDTO input, User requester, ObjectId actionId, ResponseDTO response) {
        log.debug("REST request to create an application : {}", input);
        return (ResponseDTO) createApplicationHelper.execute(input, requester, actionId, response);
    }

    /**
     * Update an application.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO update(UpdateApplicationDTO input, User requester, ObjectId actionId, ResponseDTO response) {
        log.debug("REST request to create an application : {}", input);
        return (ResponseDTO) updateApplicationHelper.execute(input, requester, actionId, response);
    }

    /**
     * Delete an application.
     *
     * @param appId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO delete(ObjectId appId, boolean gitDeleteFlag, User requester, ObjectId actionId) {
        log.debug("REST request to delete an application : {}", appId);
        return (ResponseDTO) deleteApplicationHelper.execute(new GetObjectInputDTO(appId), requester, actionId, gitDeleteFlag);
    }

    /**
     * Get Applications
     *
     * @param requester to get
     * @return the persisted entity
     */
    public ResponseDTO find(ObjectId input, User requester) {
        log.debug("REST request to get application");
        return (ResponseDTO) getApplicationHelper.execute(new GetObjectInputDTO(input), requester);
    }

    /**
     * Get List of Applications
     *
     * @param requester to get
     * @return the persisted entity
     */
    public ResponseDTO findAll(GetListFilterInput input, User requester, ResponseDTO response, Pageable pageable) {
        log.debug("REST request to get application List");
        return (ResponseDTO) getApplicationListHelper.execute(input, requester, response, pageable);
    }

    /**
     * Add Advance Configuration to application and App Vpc
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO addAdvanceConfig(AddApplicationAdvanceConfigDTO input, User requester, ObjectId actionId) {
        log.debug("Request to add advance config to application and app vpc : {}", input);
        return (ResponseDTO) addApplicationAdvanceConfigHelper.execute(input, requester, actionId);
    }


    /**
     * Build application .
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO createTemplate(CreateApplicationTemplateDTO input, User requester, ObjectId actionId, ResponseDTO response) {
        log.debug("REST request to create an application : {}", input);
        return (ResponseDTO) createApplicationTemplateHelper.execute(input, requester, actionId, response);
    }

    /**
     * Check if user is allowed to subscribe to application web-socket topic
     *
     * @param input to check
     * @return boolean as data in ResponseDTO
     */
    public ResponseDTO isUserIsAllowedToSubscribeToWebSocketTopic(IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO input, User requester) {
        log.debug("REST request to check if user is allowed to subscribe to application web socket topic");
        return (ResponseDTO) isUserAllowedToSubscribeToApplicationWebSocketTopicHelper.execute(input, requester);
    }

    /**
     * Increase or decrease subscriber count of application web-socket topic
     *
     * @param input (web socket topic name, type)
     * @return updated count as data in ResponseDTO
     */
    public ResponseDTO updateWebSocketSubscriberCount(UpdateApplicationWebSocketSubscriberCountInputDTO input, User requester) {
        log.debug("REST request to increase or decrease application web socket topic subscription count");
        return (ResponseDTO) updateApplicationWebSocketSubscriberCountHelper.execute(input, requester);
    }


    /**
     * Config File Update application .
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO configUpdateApplicationTemplate(ConfigUpdateApplicationTemplateDTO input, User requester, ObjectId actionId) {
        log.debug("REST request to update config files in application template : {}", input);
        return (ResponseDTO) configUpdateApplicationTemplateHelper.execute(input, requester, actionId);
    }

    /**
     * Get application Deployment Status.
     *
     * @param appId the entity to get details about application
     * @return the persisted entity
     */
    public ResponseDTO getAppDeploymentStatus(User requester, ObjectId actionId, ObjectId appId) {
        log.debug("REST request to get application deployment status ", appId);
        return (ResponseDTO) getApplicationDeploymentStatusHelper.execute(new GetObjectInputDTO(appId), requester, actionId);
    }


    /**
     * Get application Team.
     *
     * @param appId the entity to get details about application
     * @return the persisted entity
     */
    public ResponseDTO getAppTeam(User requester, ObjectId appId) {
        log.debug("REST request to get team for application ", appId);
        return (ResponseDTO) getApplicationTeamHelper.execute(new GetObjectInputDTO(appId), requester);
    }

    /**
     * Get application List By Vpc Id.
     *
     * @param vpcId the entity to get details about application
     * @return the persisted entity
     */
    public ResponseDTO getApplicationAndAppVpcList(User requester, ObjectId vpcId) {
        log.debug("REST request to get list of application by vpc ", vpcId);
        return (ResponseDTO) getApplicationAndAppVpcListByVpcHelper.execute(new GetObjectInputDTO(vpcId), requester);
    }
}

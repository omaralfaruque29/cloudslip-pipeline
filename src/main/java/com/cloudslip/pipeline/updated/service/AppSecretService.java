package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.app_secrets.CreateAppSecretDTO;
import com.cloudslip.pipeline.updated.dto.app_secrets.UpdateAppSecretDTO;
import com.cloudslip.pipeline.updated.helper.app_secrets.*;
import com.cloudslip.pipeline.updated.model.AppSecret;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.repository.AppSecretRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppSecretService {

    private final Logger log = LoggerFactory.getLogger(AppSecretService.class);

    private final AppSecretRepository appSecretRepository;

    @Autowired
    private CreateAppSecretHelper createAppSecretHelper;

    @Autowired
    private UpdateAppSecretHelper updateAppSecretHelper;

    @Autowired
    private GetAppSecretHelper getAppSecretHelper;

    @Autowired
    private GetAppSecretListHelper getAppSecretListHelper;

    @Autowired
    private GetAppSecretListByApplicationHelper getAppSecretListByApplicationHelper;

    @Autowired
    private GetAppSecretEnvironmentListHelper getAppSecretEnvironmentListHelper;

    @Autowired
    private DeleteAppSecretHelper deleteAppSecretHelper;

    public AppSecretService(AppSecretRepository appSecretRepository) {
        this.appSecretRepository = appSecretRepository;
    }

    /**
     * Save a AppSecret.
     *
     * @param appSecret the entity to save
     * @return the persisted entity
     */
    public AppSecret save(AppSecret appSecret) {
        log.debug("Request to save App Secrets : {}", appSecret);
        return appSecretRepository.save(appSecret);
    }


    /**
     * Create an AppSecret.
     *
     * @param input the entity to create
     * @return the persisted entity
     */
    public ResponseDTO create(CreateAppSecretDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create App Secrets : {}", input);
        return (ResponseDTO) createAppSecretHelper.execute(input, requester, actionId);
    }

    /**
     * Update an AppSecret.
     *
     * @param input the entity to create
     * @return the persisted entity
     */
    public ResponseDTO update(UpdateAppSecretDTO input, User requester, ObjectId actionId) {
        log.debug("Request to update App Secrets : {}", input);
        return (ResponseDTO) updateAppSecretHelper.execute(input, requester, actionId);
    }

    /**
     * Get all the AppSecret.
     *
     * @param requester the current user information
     * @param input the list fetching filter inputs
     * @param pageable the pagination information
     * @return the list of entities
     */
    public ResponseDTO findAll(GetListFilterInput input, User requester, Pageable pageable) {
        log.debug("Request to get all App Secrets");
        return (ResponseDTO) getAppSecretListHelper.execute(input, requester, pageable);
    }

    /**
     * Get all by appId the AppSecret.
     *
     * @param requester the current user information
     * @param input the list fetching filter inputs
     * @param pageable the pagination information
     * @return the list of entities
     */
    public ResponseDTO findAllByApplicationId(GetListFilterInput input, User requester, Pageable pageable, ObjectId applicationId) {
        log.debug("Request to get all App Secrets By Application");
        return (ResponseDTO) getAppSecretListByApplicationHelper.execute(input, requester, pageable, applicationId);
    }

    /**
     * Get all by envList by App id.
     *
     * @param id the id of the entity
     * @return the list of entities
     */
    public ResponseDTO findAllAppSecretEnvironmentList(User requester, ObjectId id) {
        log.debug("Request to get all App Secrets By Application");
        return (ResponseDTO) getAppSecretEnvironmentListHelper.execute(new GetObjectInputDTO(id), requester);
    }

    /**
     * Get one AppSecret by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    public ResponseDTO findById(User requester, ObjectId id) {
        log.debug("Request to get App Secrets : {}", id);
        return (ResponseDTO) getAppSecretHelper.execute(new GetObjectInputDTO(id), requester);
    }

    /**
     * Delete the AppSecret by id.
     *
     * @param id the id of the entity
     */
    public ResponseDTO delete(User requester, ObjectId actionId, ObjectId id) {
        log.debug("Request to delete App Secrets : {}", id);
        return (ResponseDTO) deleteAppSecretHelper.execute(new GetObjectInputDTO(id), requester, actionId);
    }
}

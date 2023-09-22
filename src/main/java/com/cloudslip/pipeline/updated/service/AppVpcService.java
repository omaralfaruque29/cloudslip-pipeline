package com.cloudslip.pipeline.updated.service;


import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.helper.app_vpc.AddCustomIngressToAppVpcHelper;
import com.cloudslip.pipeline.updated.helper.app_vpc.CreateAppVpcHelper;
import com.cloudslip.pipeline.updated.helper.app_vpc.DeleteAllAppVpcHelper;
import com.cloudslip.pipeline.updated.helper.app_vpc.GetAppVpcListHelper;
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
public class AppVpcService {

    private final Logger log = LoggerFactory.getLogger(AppVpcService.class);

    @Autowired
    private CreateAppVpcHelper createAppVpcHelper;

    @Autowired
    private DeleteAllAppVpcHelper deleteAllAppVpcHelper;

    @Autowired
    private AddCustomIngressToAppVpcHelper addCustomIngressToAppVpcHelper;

    @Autowired
    private GetAppVpcListHelper getAppVpcListHelper;


    /**
     * Create Vpc List for For application Environment.
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO createAppVpcList(CreateAppVpcDTO input, User requester, ObjectId actionId) {
        log.debug("Request to create app vpc list for an app environments: {}", input);
        return (ResponseDTO) createAppVpcHelper.execute(input, requester, actionId);
    }


    /**
     * Delete App Vpc List for For application Environment.
     *
     * @param appEnvironmentId the entity to save
     * @return the persisted entity
     */
    public ResponseDTO deleteAllAppVpc(GetObjectInputDTO appEnvironmentId,  User requester, ObjectId actionId) {
        log.debug("Request to delete app vpc list for an app environments: {}", appEnvironmentId);
        return (ResponseDTO) deleteAllAppVpcHelper.execute(appEnvironmentId, requester, actionId);
    }

    /**
     * Add Custom Ingress to application vpc
     *
     * @param input the entity to save
     * @return the persisted entity
     */
    public ResponseDTO addCustomIngress(AddCustomIngressDTO input, User requester, ObjectId actionId) {
        log.debug("Request to add custom ingress: {}", input);
        return (ResponseDTO) addCustomIngressToAppVpcHelper.execute(input, requester, actionId);
    }

    /**
     * Get List of App Vpc Response by application ID.
     *
     * @param input to get
     * @return the persisted entity
     */
    public ResponseDTO findAll(GetListFilterInput input, User requester, Pageable pageable) {
        log.debug("REST request to get App Vpc list by application : {}", input);
        return (ResponseDTO) getAppVpcListHelper.execute(input, requester, pageable);
    }
}

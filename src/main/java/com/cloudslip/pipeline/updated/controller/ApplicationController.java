package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.application.AddApplicationAdvanceConfigDTO;
import com.cloudslip.pipeline.updated.dto.application.CreateApplicationDTO;
import com.cloudslip.pipeline.updated.dto.application.UpdateApplicationDTO;
import com.cloudslip.pipeline.updated.enums.ApplicationCreationType;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.ApplicationService;
import com.cloudslip.pipeline.updated.util.Utils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private CustomRestTemplate restTemplate;

    private final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private ApplicationProperties applicationProperties;



    @RequestMapping(value = "/application/create", method = RequestMethod.POST)
    public ResponseEntity<?> createApplication(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                               @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                               @Valid @RequestBody CreateApplicationDTO input) throws URISyntaxException {
        log.debug("REST request to create application : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        HttpHeaders headers = Utils.generateHttpHeaders(currentUserStr);
        HttpEntity<String> request = new HttpEntity<>("parameter", headers);
        if(ApplicationCreationType.valueOf(input.getAppCreationType()) == ApplicationCreationType.FROM_GIT_SOURCE && input.getName() != null && input.getGitRepositoryName() == null){
            input.setGitRepositoryName(input.getName());
        }
        input.setGitRepositoryName(input.getName());
        if (input.getTeamId() == null) {
            return new ResponseEntity<>(new ResponseDTO<>().generateErrorResponse("Team id is required"), HttpStatus.OK);
        }
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(applicationProperties.getUserManagementServiceBaseUrl() + "api/application/create-app/" + input.getTeamId(), HttpMethod.GET, request, ResponseDTO.class);
        if (response.getBody().getStatus() == ResponseStatus.error) {
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        }
        ResponseDTO result = applicationService.create(input, requester, actionId, response.getBody());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/update", method = RequestMethod.POST)
    public ResponseEntity<?> updateApplication(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                               @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                               @Valid @RequestBody UpdateApplicationDTO input) throws URISyntaxException {
        log.debug("REST request to update application : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        HttpHeaders headers = Utils.generateHttpHeaders(currentUserStr);
        HttpEntity<String> request = new HttpEntity<>("parameter", headers);
        if (input.getTeamId() == null) {
            return new ResponseEntity<>(new ResponseDTO<>().generateErrorResponse("Team id is required"), HttpStatus.OK);
        }
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(applicationProperties.getUserManagementServiceBaseUrl() + "api/application/create-app/" + input.getTeamId(), HttpMethod.GET, request, ResponseDTO.class);
        if (response.getBody().getStatus() == ResponseStatus.error) {
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        }
        ResponseDTO result = applicationService.update(input, requester, actionId, response.getBody());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/get/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getApplication(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("id") ObjectId appId) {
        log.debug("REST request to get application");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.find(appId, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/get-list", method = RequestMethod.GET)
    public ResponseEntity<?> getApplicationList(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @Nullable GetListFilterInput input, Pageable pageable) {
        log.debug("REST request to get application List");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        HttpHeaders headers = Utils.generateHttpHeaders(currentUserStr);
        HttpEntity<String> request = new HttpEntity<>("parameter", headers);
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(applicationProperties.getUserManagementServiceBaseUrl() + "api/application/get-team-list", HttpMethod.GET, request, ResponseDTO.class);
        if (response.getBody().getStatus() == ResponseStatus.error) {
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        }
        ResponseDTO result = applicationService.findAll(input, requester, response.getBody(), pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/create-template", method = RequestMethod.POST)
    public ResponseEntity<?> createApplicationTemplate(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                               @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                               @Valid @RequestBody CreateApplicationTemplateDTO input) throws URISyntaxException {
        log.info("REST request to create application Template : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        HttpHeaders headers = Utils.generateHttpHeaders(currentUserStr);
        HttpEntity<GetObjectInputDTO> request = new HttpEntity<>(input.getCompanyId() != null ? new GetObjectInputDTO(input.getCompanyId()) : new GetObjectInputDTO(), headers);
        ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/application/get-user-and-company-info", request, ResponseDTO.class);
        if(response.getStatus() == ResponseStatus.error){
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        ResponseDTO result = applicationService.createTemplate(input, requester, actionId, response);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/delete/{app-id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId, @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("app-id") ObjectId id, @RequestParam("flag") boolean gitDeleteFlag) {
        log.debug("REST request to delete application : {}", id);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.delete(id, gitDeleteFlag, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/add-advance-config", method = RequestMethod.POST)
    public ResponseEntity<?> addApplicationAdvanceConfig(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                         @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                         @Valid @RequestBody AddApplicationAdvanceConfigDTO input) throws URISyntaxException {
        log.debug("REST request to add advance config to application and app vpc : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.addAdvanceConfig(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/web-socket/is-user-allowed-to-subscribe/{webSocketTopic}", method = RequestMethod.GET)
    public ResponseEntity<?> isUserAllowedToSubscribeToWebSocketTopic(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("webSocketTopic") String webSocketTopic) {
        log.debug("REST request to check if user is allowed to subscribe to web socket topic of an application: {}", webSocketTopic);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.isUserIsAllowedToSubscribeToWebSocketTopic(new IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO(webSocketTopic), requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/web-socket/update-subscriber-count", method = RequestMethod.POST)
    public ResponseEntity<?> updateApplicationWebSocketTopicSubscriberCount(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @RequestBody UpdateApplicationWebSocketSubscriberCountInputDTO input) {
        log.info("REST request to {} subscriber count of web socket topic: {}", input.getType(), input.getWebSocketTopic());
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.updateWebSocketSubscriberCount(input, requester);
        if(result.getStatus() == ResponseStatus.error){
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/deployment-status/{app-id}", method = RequestMethod.GET)
    public ResponseEntity<?> getAppDeploymentStatus(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId, @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("app-id") ObjectId appId){
        log.debug("REST request to get application deployment status: %s", appId);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.getAppDeploymentStatus(requester, actionId, appId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/get-app-team/{app-id}", method = RequestMethod.GET)
    public ResponseEntity<?> getApplicationTeam(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("app-id") ObjectId appId){
        log.debug("REST request to get team for application : %s", appId);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.getAppTeam(requester, appId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/application/get-list-by-vpc/{vpc-id}", method = RequestMethod.GET)
    public ResponseEntity<?> getApplicationAndAppVpcListByVpc(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("vpc-id") ObjectId vpcId){
        log.debug("REST request to get list of application and App Vpc by vpc : %s", vpcId);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = applicationService.getApplicationAndAppVpcList(requester, vpcId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

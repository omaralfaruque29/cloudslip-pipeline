package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.app_environment.AddAppEnvironmentsDTO;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.AppEnvironmentService;
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
public class AppEnvironmentController {

    private final Logger log = LoggerFactory.getLogger(AppEnvironmentController.class);

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private AppEnvironmentService appEnvironmentService;
    
    @Autowired
    private ApplicationProperties applicationProperties;
    

    @RequestMapping(value = "/app-env/add-environments", method = RequestMethod.POST)
    public ResponseEntity<?> addEnvironmentsForApplication(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                          @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                          @Valid @RequestBody AddAppEnvironmentsDTO input) throws URISyntaxException {
        log.debug("REST request to add environments to application : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        HttpHeaders headers = Utils.generateHttpHeaders(currentUserStr);
        HttpEntity<AddAppEnvironmentsDTO> request = new HttpEntity<>(input, headers);
        ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/app-env/get-environments", request, ResponseDTO.class);
        if(response.getStatus() == ResponseStatus.error){
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        ResponseDTO result = appEnvironmentService.addEnvironments(input, requester, actionId, response);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-env/get-environments", method = RequestMethod.GET)
    public ResponseEntity<?> getAppEnvironmentListByApplication(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                                @Nullable GetListFilterInput input,
                                                                Pageable pageable) {
        log.debug("REST request to get App Environment list by application : {}", input);
        User currentUser = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appEnvironmentService.findAll(input, currentUser, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

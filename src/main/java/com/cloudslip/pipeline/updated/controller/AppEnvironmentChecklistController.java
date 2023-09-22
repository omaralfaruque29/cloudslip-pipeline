package com.cloudslip.pipeline.updated.controller;


import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.AddAppEnvironmentChecklistDTO;
import com.cloudslip.pipeline.updated.dto.DeleteChecklistDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.UpdateAppEnvironmentChecklistDTO;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.AppEnvironmentChecklistService;
import com.cloudslip.pipeline.updated.util.Utils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class AppEnvironmentChecklistController {

    private final Logger log = LoggerFactory.getLogger(AppEnvironmentChecklistController.class);


    @Autowired
    private AppEnvironmentChecklistService appEnvironmentChecklistService;


    @RequestMapping(value = "/app-env-checklist/add-checklists", method = RequestMethod.POST)
    public ResponseEntity<?> createChecklistForAppEnvironment(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                              @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                              @Valid @RequestBody AddAppEnvironmentChecklistDTO input) throws URISyntaxException {
        log.debug("REST request to add checklist to application environment: {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appEnvironmentChecklistService.createChecklist(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-env-checklist/update-checklists", method = RequestMethod.POST)
    public ResponseEntity<?> updateChecklistForAppEnvironment(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                              @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                              @Valid @RequestBody UpdateAppEnvironmentChecklistDTO input) throws URISyntaxException {
        log.debug("REST request to update checklist to application environment: {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appEnvironmentChecklistService.updateChecklist(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-env-checklist/get-checklists", method = RequestMethod.GET)
    public ResponseEntity<?> getChecklistForAppEnvironment(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                           @RequestParam("appId") ObjectId applicationId, @RequestParam("appEnvId") ObjectId appEnvId) {
        log.debug("REST request to get App Environment Checklist by application and App Environment : {}", applicationId);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appEnvironmentChecklistService.findChecklist(applicationId, appEnvId, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-env-checklist/delete-checklists", method = RequestMethod.POST)
    public ResponseEntity<?> deleteChecklistForAppEnvironment(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                           @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                           @RequestBody @Valid DeleteChecklistDTO dto) {
        log.debug("REST request to delete App Environment Checklist : {}", dto.getAppEnvChecklistId());
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appEnvironmentChecklistService.deleteChecklist(dto, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

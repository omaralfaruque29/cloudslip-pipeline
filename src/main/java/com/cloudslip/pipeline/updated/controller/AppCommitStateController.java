package com.cloudslip.pipeline.updated.controller;


import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.dto.CheckAppEnvStateChecklistDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.AppCommitStateService;
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
public class AppCommitStateController {

    private final Logger log = LoggerFactory.getLogger(AppCommitStateController.class);

    @Autowired
    private AppCommitStateService appCommitStateService;


    @RequestMapping(value = "/app-commit-state/generate-state", method = RequestMethod.POST)
    public ResponseEntity<?> initCommitState(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                           @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                           @RequestHeader(value = "User-Agent") String userAgent,
                                                           @RequestHeader(value = "X-GitHub-Delivery") String githubDelivery,
                                                           @RequestHeader(value = "X-GitHub-Event") String githubEvent,
                                                           @RequestHeader(value = "application-id") ObjectId applicationId,
                                                           @RequestBody String payloadInput) {
        log.debug("REST request to generate App Commit State");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appCommitStateService.generate(applicationId, payloadInput, userAgent, githubDelivery, githubEvent, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-commit-state/get-states/{app-id}", method = RequestMethod.GET)
    public ResponseEntity<?> getAppCommitStates(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("app-id") ObjectId appId) {
        log.debug("REST request to get application Commit State");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appCommitStateService.findAll(appId, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-commit-state/sync/{app-id}", method = RequestMethod.GET)
    public ResponseEntity<?> syncAppCommitStatesFromGit(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, @PathVariable("app-id") ObjectId appId) {
        log.debug("REST request to get application Commit State");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appCommitStateService.sync(appId, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-commit-state/check-app-env-state-checklist", method = RequestMethod.POST)
    public ResponseEntity<?> checkAppEnvStateChecklist(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                              @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                              @Valid @RequestBody CheckAppEnvStateChecklistDTO input) throws URISyntaxException {
        log.debug("REST request to check and uncheck the app env state checklist in app commit state : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appCommitStateService.checkAppEnvStateCheckList(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

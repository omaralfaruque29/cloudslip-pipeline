package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.manager.PipelineActionManager;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.AppPipelineStepService;
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
public class AppPipelineStepController {

    private final Logger log = LoggerFactory.getLogger(AppPipelineStepController.class);

    @Autowired
    private AppPipelineStepService appPipelineStepService;

    @Autowired
    private PipelineActionManager pipelineActionManager;

    @RequestMapping(value = "/app-pipeline-step/add-custom-pipeline-step", method = RequestMethod.POST)
    public ResponseEntity<?> addCustomPipeLineStepsForAppEnv(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                             @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                             @Valid @RequestBody AddCustomPipelineStepDTO input) throws URISyntaxException {
        log.debug("REST request to add custom pipeline step to application environment : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appPipelineStepService.addPipelineStepToAppEnv(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-pipeline-step/update-pipeline-step-successor", method = RequestMethod.POST)
    public ResponseEntity<?> updateSuccessorForPipelineStep(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                             @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                             @Valid @RequestBody UpdatePipelineStepSuccessorDTO input) throws URISyntaxException {
        log.debug("REST request to update pipeline step successors : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appPipelineStepService.updateSuccessors(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-pipeline-step/get-list", method = RequestMethod.GET)
    public ResponseEntity<?> getPipelineStepsByAppEnv(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                      @Nullable GetListFilterInput input,
                                                      Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get App Pipeline Step list by application Environment : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appPipelineStepService.findAll(input, requester, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-pipeline-step/trigger-pipeline", method = RequestMethod.POST)
    public ResponseEntity<?> triggerJenkinsJobForBuild(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                       @RequestBody TriggerPipelineFromStartInputDTO input) throws Exception {
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = pipelineActionManager.triggerPipelineFromStart(input, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-pipeline-step/update-status", method = RequestMethod.POST)
    public ResponseEntity<?> pipelineRunning(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                             @RequestBody AppPipelineStepStatusUpdateDTO input) throws Exception {
        input.setStatusType(input.getStatusType());
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = pipelineActionManager.appPipelineStepStatusUpdate(input, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}

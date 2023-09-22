package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.manager.PipelineActionManager;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.util.Utils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;


@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final Logger log = LoggerFactory.getLogger(PipelineController.class);

    @Autowired
    private PipelineActionManager pipelineActionManager;



    @RequestMapping(value = "/app/deploy", method = RequestMethod.POST)
    public ResponseEntity<?> deployApplication(@RequestHeader(value = HttpHeader.CURRENT_USER) String requesterStr,
                                               @RequestBody DeployInAppVpcInputDTO input) throws URISyntaxException {
        log.debug("REST request to create application : {}", input);
        User requester = Utils.generateUserFromJsonStr(requesterStr);
        ResponseDTO result = pipelineActionManager.deployInAppVpc(input, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/rollback/{app-pipeline-step-id}", method = RequestMethod.PUT)
    public ResponseEntity<?> rollbackDeployedApplication(@RequestHeader(value = HttpHeader.CURRENT_USER) String requesterStr,
                                                         @RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                                         @PathVariable("app-pipeline-step-id") ObjectId appPipelineStepId) throws URISyntaxException {
        log.debug("REST request to rollback deployed application : {}", appPipelineStepId);
        User requester = Utils.generateUserFromJsonStr(requesterStr);
        ResponseDTO result = pipelineActionManager.rollbackDeployment(appPipelineStepId, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/run-step", method = RequestMethod.POST)
    public ResponseEntity<?> runAppPipelineStep(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId, @RequestHeader(value = HttpHeader.CURRENT_USER) String requesterStr,
                                                @RequestBody RunAppPipelineStepInputDTO input) throws URISyntaxException {
        log.debug("REST request to create application : {}", input);
        User requester = Utils.generateUserFromJsonStr(requesterStr);
        ResponseDTO result = pipelineActionManager.runPipelineStep(input, requester);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

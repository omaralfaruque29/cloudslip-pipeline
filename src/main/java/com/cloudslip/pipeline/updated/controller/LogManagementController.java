package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.dto.GetLogInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.LogFetchService;
import com.cloudslip.pipeline.updated.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class LogManagementController {

    private final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private LogFetchService logFetchService;

    @RequestMapping(value = "/log/fetch", method = RequestMethod.GET)
    public ResponseEntity<?> fetchLog(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr, GetLogInputDTO getLogInputDTO) throws URISyntaxException {
        log.debug("REST request to fetch log: {}", getLogInputDTO.getAppCommitPipelineStepId());
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO responseDTO = logFetchService.fetchLog(getLogInputDTO,requester);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}

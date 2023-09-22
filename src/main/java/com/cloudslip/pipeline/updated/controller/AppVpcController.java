package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.dto.AddCustomIngressDTO;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.AppVpcService;
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
public class AppVpcController {

    private final Logger log = LoggerFactory.getLogger(AppVpcController.class);

    @Autowired
    private AppVpcService appVpcService;


    @RequestMapping(value = "/app-vpc/add-custom-ingress", method = RequestMethod.POST)
    public ResponseEntity<?> addCustomIngress(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                               @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                               @Valid @RequestBody AddCustomIngressDTO input) throws URISyntaxException {
        log.debug("REST request to add custom ingress to app vpc : {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appVpcService.addCustomIngress(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-vpc/get-list", method = RequestMethod.GET)
    public ResponseEntity<?> getAppVpcListByApplication(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                                @Nullable GetListFilterInput input,
                                                                Pageable pageable) {
        log.debug("REST request to get App Vpc list by application : {}", input);
        User currentUser = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appVpcService.findAll(input, currentUser, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

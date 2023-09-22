package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.constant.HttpHeader;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.app_secrets.CreateAppSecretDTO;
import com.cloudslip.pipeline.updated.dto.app_secrets.UpdateAppSecretDTO;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.service.AppSecretService;
import com.cloudslip.pipeline.updated.util.Utils;
import com.mongodb.lang.Nullable;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class AppSecretController {
    private final Logger log = LoggerFactory.getLogger(AppSecretController.class);

    @Autowired
    private AppSecretService appSecretService;

    @RequestMapping(value = "/app-secret/create", method = RequestMethod.POST)
    public ResponseEntity<?> createAppSecret(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                              @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                              @RequestBody CreateAppSecretDTO input) throws URISyntaxException {
        log.debug("REST request to create App Secret: {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appSecretService.create(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-secret/update", method = RequestMethod.PUT)
    public ResponseEntity<?> updateAppSecret(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                              @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                             @RequestBody UpdateAppSecretDTO input) throws URISyntaxException {
        log.debug("REST request to update App Secret: {}", input);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
       ResponseDTO result = appSecretService.update(input, requester, actionId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-secret/get/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getAppSecret(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                            @PathVariable("id") ObjectId id) {
        log.debug("REST request to get App Secret: {}", id);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appSecretService.findById(requester, id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @RequestMapping(value = "/app-secret/get-list", method = RequestMethod.GET)
    public ResponseEntity<?> getAppSecretList(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                               @Nullable GetListFilterInput input, Pageable pageable) {
        log.debug("REST request to get App Secret List");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
       ResponseDTO result = appSecretService.findAll(input, requester, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-secret/{id}/get-list-by-application", method = RequestMethod.GET)
    public ResponseEntity<?> getAppSecretListByApplication(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                              @Nullable GetListFilterInput input, Pageable pageable,
                                              @PathVariable("id") ObjectId appId) {
        log.debug("REST request to get App Secret List By Application Id");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appSecretService.findAllByApplicationId(input, requester, pageable, appId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-secret/get-app-secret-environment-list/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getAppSecretEnvironmentList(@RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                                         @PathVariable("id") ObjectId appId) {
        log.debug("REST request to get App Secret List By Application Id");
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appSecretService.findAllAppSecretEnvironmentList(requester, appId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/app-secret/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAppSecret(@RequestHeader(value = HttpHeader.ACTION_ID) ObjectId actionId,
                                              @RequestHeader(value = HttpHeader.CURRENT_USER) String currentUserStr,
                                              @PathVariable ObjectId id) {
        log.debug("REST request to delete App Secret : {}", id);
        User requester = Utils.generateUserFromJsonStr(currentUserStr);
        ResponseDTO result = appSecretService.delete(requester, actionId, id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

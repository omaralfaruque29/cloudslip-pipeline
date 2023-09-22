/*
package com.cloudslip.pipeline.controller;

import com.cloudslip.pipeline.model.jenkins.JenkinsBuildResponseModel;
import com.cloudslip.pipeline.model.jenkins.JenkinsConsoleLog;
import com.cloudslip.pipeline.model.SimpleResponse;
import com.cloudslip.pipeline.model.TriggerCreateAppRequest;
import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.service.KubernetesService;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class PipelineControllerOld {

    @Autowired
    private JenkinsService jenkinsService;

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private Set<String> restrictedApps;

    */
/*@Timed(name = "startBuildWithCommit.time")
    @Metered(name = "startBuildWithCommit.count")
    @RequestMapping(value = "/buildWithCommit/{serviceName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<JenkinsBuildResponseModel> startBuildWithCommit(@PathVariable String serviceName,
                                               @RequestParam(required = true) String gitCommitId) throws Exception {
        if (restrictedApps.contains(serviceName.toLowerCase())) {
            return ResponseEntity.unprocessableEntity().body(new JenkinsBuildResponseModel());
        }
        JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.triggerBuildUsingCommit(serviceName, gitCommitId);
        return ResponseEntity.ok(jenkinsBuildResponseModel);
    }*//*


    //New implementation
    @RequestMapping(value = "/createApp", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<JenkinsBuildResponseModel> createAppGetQueuedItem(@RequestBody TriggerCreateAppRequest appRequest) throws Exception {
        JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.triggerCreateAppBuildGetQueuedResponse(appRequest);
        return ResponseEntity.ok(jenkinsBuildResponseModel);
    }


    @RequestMapping(value = "/queuedInfo", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<JenkinsBuildResponseModel> getQueuedStatus(@RequestParam String appName,
                                                                     @RequestParam String currentJobName,
                                                                     @RequestParam String queuedUrl) throws Exception {
        JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.fetchQueuedStatus(appName, currentJobName, queuedUrl);
        return ResponseEntity.ok(jenkinsBuildResponseModel);
    }

    @Timed (value = "getPipelineStepBuildStatus.time")
    @RequestMapping(value = "/buildStatus/{jobName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<JenkinsBuildResponseModel> getPipelineStepBuildStatus(@PathVariable String jobName,
                                                                                @RequestParam(required = true) Integer buildNumber,
                                                                                @RequestParam(required = false) Integer pipelineStepIndex) throws Exception {
        JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.getPipelineStepsBuildInfo(jobName, buildNumber, pipelineStepIndex);
        return ResponseEntity.ok(jenkinsBuildResponseModel);
    }

    @Timed(value = "getCreateAppFinalBuildStatus.time")
    @RequestMapping(value = "/finalBuildStatus/{jobName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<JenkinsBuildResponseModel> getCreateAppFinalBuildStatus(@PathVariable String jobName,
                                                                                  @RequestParam(required = false) Integer buildNumber) throws Exception {
        JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.getCreateAppFinalBuildInfo(jobName, buildNumber);
        return ResponseEntity.ok(jenkinsBuildResponseModel);
    }

    @RequestMapping(value = "/log/{jobName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<JenkinsConsoleLog> getConsoleLog(@PathVariable String jobName,
                                                           @RequestParam(required = true) Integer buildNumber,
                                                           @RequestParam(required = false) Integer startIndex) throws Exception {
        JenkinsConsoleLog jenkinsConsoleLog = jenkinsService.getConsoleLogFromSpecificIndex(jobName, buildNumber, startIndex);
        return ResponseEntity.ok(jenkinsConsoleLog);
    }

    @RequestMapping(value = "/runningBuild/{appName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<JenkinsBuildResponseModel> findRunningBuild(@PathVariable String appName) throws Exception {
        JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.findRunningBuild(appName);
        return ResponseEntity.ok(jenkinsBuildResponseModel);
    }

    @Timed (value = "getAppAccessUrl.time")
    @RequestMapping(value = "/url/{appName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<SimpleResponse> getAppAccessUrl(@PathVariable String appName) throws Exception {
        String appUrl = kubernetesService.getAppUrl(appName);
        SimpleResponse simpleResponse = new SimpleResponse(appUrl);
        return ResponseEntity.ok(simpleResponse);
    }

    @RequestMapping(value = "/notify/build", method = RequestMethod.GET)
    public ResponseEntity<String> testNotify(@RequestParam(required = true) String appName,
                                             @RequestParam(required = true) String commitId) throws Exception {
        System.out.println("app name: " + appName);
        System.out.println("commit id: " + commitId);
        return ResponseEntity.ok("Notified");
    }
}
*/

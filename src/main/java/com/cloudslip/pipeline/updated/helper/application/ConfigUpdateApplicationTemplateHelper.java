package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.model.jenkins.JenkinsBuildResponseModel;
import com.cloudslip.pipeline.model.jenkins.JenkinsConsoleLog;
import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.updated.constant.JenkinsJobName;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ConfigUpdateApplicationTemplateDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.ApplicationType;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import org.apache.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigUpdateApplicationTemplateHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(ConfigUpdateApplicationTemplateHelper.class);

    private ConfigUpdateApplicationTemplateDTO input;
    private ResponseDTO output = new ResponseDTO();

    String selectedAppEnvironmentParameter = "";
    String unselectedAppEnvironmentParameter = "";

    @Autowired
    JenkinsServer jenkinsServer;

    @Autowired
    JenkinsService jenkinsService;

    private int retryCount = 0;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (ConfigUpdateApplicationTemplateDTO) input;
        this.setOutput(output);
        this.selectedAppEnvironmentParameter = "";
        this.unselectedAppEnvironmentParameter = "";
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.triggerPipelineJob();
        output.generateSuccessResponse(input.getSelectedAppEnvironments(), "Config Files Updating Into application Template");
    }


    protected void doPerform() {
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void triggerPipelineJob() {
        try {
            Map<String, String> paramMap = this.getCommonParametersForGenerator();
            String jobName = this.getJenkinsJobName();
            JobWithDetails job = jenkinsServer.getJob(jobName);
            QueueReference queueRef = job.build(paramMap);
            jenkinsService.buildResponseForQueuedJob(jobName, jobName, queueRef);
            Thread thread = new Thread() {
                public void run(){
                    try {
                        int logCurrentIndex = 0;
                        System.out.println("Pipeline Running");
                        JenkinsBuildResponseModel jenkinsBuildResponseModel = null;
                        JenkinsConsoleLog jenkinsConsoleLog = null;
                        while(true) {
                            Thread.sleep(2000);
                            jenkinsBuildResponseModel = jenkinsService.getCurrentQueuedStatus(jobName, jobName, queueRef);
                            if(jenkinsBuildResponseModel != null && jenkinsBuildResponseModel.getBuildId() != null && !jenkinsBuildResponseModel.getBuildId().equals("")) {
                                String buildId = jenkinsBuildResponseModel.getBuildId();
                                jenkinsConsoleLog = jenkinsService.getConsoleLogFromSpecificIndex(jobName, Integer.parseInt(buildId), logCurrentIndex);
                                logCurrentIndex = jenkinsConsoleLog.getTextSize();
                                System.out.println(jenkinsConsoleLog.getLogText());
                                if (jenkinsBuildResponseModel.getStatus().equals("FAILURE")) {
                                    System.out.println(jenkinsConsoleLog.getLogText());
                                    System.out.println(jenkinsBuildResponseModel.getStatus());
                                    break;
                                } else if (jenkinsBuildResponseModel.getNextBuildStatusUrl() == null && jenkinsBuildResponseModel.getStatus().equals("SUCCESS")) {
                                    System.out.println(jenkinsConsoleLog.getLogText());
                                    System.out.println(jenkinsBuildResponseModel.getStatus());
                                    break;
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        } catch (NoHttpResponseException ex) {
            log.error(ex.getMessage());
            retry();
        } catch(Exception ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("error occurred while triggering pipeline");
        }
    }
    private void retry() {
        retryCount++;
        log.info("App Pipeline Trigger: Retry no " + Integer.toString(retryCount));
        if(retryCount <= 3) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
                output.generateErrorResponse("error occurred while retrying to trigger pipeline for application template creation");
            }
            this.triggerPipelineJob();
        } else {
            output.generateErrorResponse("error occurred while triggering pipeline for application template creation (Jenkins connection error)!");
        }
    }

    private String getJenkinsJobName() {
        if (input.getApplication().getType() == ApplicationType.SPRING_BOOT) {
            return JenkinsJobName.CONFIG_UPDATE_SPRING_BOOT;
        } else if (input.getApplication().getType() == ApplicationType.EXPRESS_JS){
            return  JenkinsJobName.CONFIG_UPDATE_EXPRESS_JS;
        }
        return "";
    }

    private Map<String, String> getCommonParametersForGenerator() {
        this.selectedAppEnvironmentParameter();
        this.unselectedAppEnvironmentParameter();
        if (!selectedAppEnvironmentParameter.equals("") && selectedAppEnvironmentParameter.charAt(selectedAppEnvironmentParameter.length() - 1) == ';') {
            selectedAppEnvironmentParameter = selectedAppEnvironmentParameter.substring(0, selectedAppEnvironmentParameter.length() - 1);
        }
        if (!unselectedAppEnvironmentParameter.equals("") && unselectedAppEnvironmentParameter.charAt(unselectedAppEnvironmentParameter.length() - 1) == ';') {
            unselectedAppEnvironmentParameter = unselectedAppEnvironmentParameter.substring(0, unselectedAppEnvironmentParameter.length() - 1);
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("applicationName", input.getApplication().getName().toLowerCase().replaceAll(" ", "-"));
        paramMap.put("numOfReplicas", "1");
        paramMap.put("gitUsername", input.getCompany().getGitInfo().getUsername());
        paramMap.put("gitAuthToken", input.getCompany().getGitInfo().getSecretKey());
        paramMap.put("selectedAppEnvironments", selectedAppEnvironmentParameter);
        paramMap.put("unselectedAppEnvironments", unselectedAppEnvironmentParameter);
        if (input.getApplication().getType() == ApplicationType.SPRING_BOOT) {
            paramMap.put("applicationPackage", input.getApplication().getPackageName());
        }
        return paramMap;
    }

    private void selectedAppEnvironmentParameter() {
        for (int countAppEnv = 0; countAppEnv < input.getSelectedAppEnvironments().size(); countAppEnv++) {
            if (!input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().isEmpty()) {
                selectedAppEnvironmentParameter += input.getSelectedAppEnvironments().get(countAppEnv).getEnvironment().getShortName().toLowerCase().trim().replaceAll(" ", "_") + ":";

                // Selected App Vpc List For Selected App Environments
                for (int countCluster = 0; countCluster < input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().size(); countCluster++) {
                    String defaultIngressUrl = input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().get(countCluster).getIngressConfig().getDefaultIngressUrl() != null ? input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().get(countCluster).getIngressConfig().getDefaultIngressUrl().toLowerCase() : "default";
                    if (countCluster == input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().size() - 1) {
                        selectedAppEnvironmentParameter += input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().get(countCluster).getVpc().getName().trim()
                                +"#" + defaultIngressUrl ;
                    } else {
                        selectedAppEnvironmentParameter += input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().get(countCluster).getVpc().getName().trim()
                                +"#" + defaultIngressUrl + ",";
                    }
                }
            }
            if  (countAppEnv < input.getSelectedAppEnvironments().size() - 1
                    && !input.getSelectedAppEnvironments().get(countAppEnv).getAppVpcList().isEmpty()) {
                selectedAppEnvironmentParameter += ";";
            }

            // Unselected App Vpc List For Selected App Environments
            if (input.getUnselectedAppVpcMap().containsKey(input.getSelectedAppEnvironments().get(countAppEnv).getObjectId().toString())) {
                List <AppVpc> unselectedAppVpcList = input.getUnselectedAppVpcMap().get(input.getSelectedAppEnvironments().get(countAppEnv).getObjectId().toString());
                if (!unselectedAppVpcList.isEmpty()) {
                    unselectedAppEnvironmentParameter += input.getSelectedAppEnvironments().get(countAppEnv).getEnvironment().getShortName().toLowerCase().trim().replaceAll(" ", "_") + ":";
                    for (int countCluster = 0; countCluster < unselectedAppVpcList.size(); countCluster++) {
                        if (countCluster == unselectedAppVpcList.size() - 1) {
                            unselectedAppEnvironmentParameter += unselectedAppVpcList.get(countCluster).getVpc().getName().trim();
                        } else {
                            unselectedAppEnvironmentParameter += unselectedAppVpcList.get(countCluster).getVpc().getName().trim() + ",";
                        }
                    }
                    unselectedAppEnvironmentParameter += ";";
                }
            }
        }
    }

    private void unselectedAppEnvironmentParameter() {
        for (int countAppEnv = 0; countAppEnv < input.getUnselectedAppEnvironments().size(); countAppEnv++) {
            if (!input.getUnselectedAppEnvironments().get(countAppEnv).getAppVpcList().isEmpty()) {
                unselectedAppEnvironmentParameter += input.getUnselectedAppEnvironments().get(countAppEnv).getEnvironment().getShortName().toLowerCase().trim().replaceAll(" ", "_") + ":";
                // Unselected App Vpc List For Selected App Environments
                for (int countCluster = 0; countCluster < input.getUnselectedAppEnvironments().get(countAppEnv).getAppVpcList().size(); countCluster++) {
                    if (countCluster == input.getUnselectedAppEnvironments().get(countAppEnv).getAppVpcList().size() - 1) {
                        unselectedAppEnvironmentParameter += input.getUnselectedAppEnvironments().get(countAppEnv).getAppVpcList().get(countCluster).getVpc().getName().trim();
                    } else {
                        unselectedAppEnvironmentParameter += input.getUnselectedAppEnvironments().get(countAppEnv).getAppVpcList().get(countCluster).getVpc().getName().trim() + ",";
                    }
                }
            }

            if  (countAppEnv < input.getUnselectedAppEnvironments().size() - 1
                    && !input.getUnselectedAppEnvironments().get(countAppEnv).getAppVpcList().isEmpty()) {
                unselectedAppEnvironmentParameter += ";";
            }
        }
    }
}

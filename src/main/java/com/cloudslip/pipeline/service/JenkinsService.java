package com.cloudslip.pipeline.service;

import com.cloudslip.pipeline.model.*;
import com.cloudslip.pipeline.model.jenkins.JenkinsBuildResponseModel;
import com.cloudslip.pipeline.model.jenkins.JenkinsConsoleLog;
import com.cloudslip.pipeline.model.jenkins.PipelineStep;
import com.cloudslip.pipeline.model.jenkins.wfapi.Pipeline;
import com.cloudslip.pipeline.model.jenkins.wfapi.Stage;
import com.cloudslip.pipeline.util.JenkinsUtil;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

@Component
public class JenkinsService {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsService.class);

    @Autowired
    private JenkinsServer jenkinsServer;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${jenkins.url}")
    private String jenkinsUrl;

    @Value("${jenkins.token}")
    private String jenkinsToken;

    //New implementation
    public JenkinsBuildResponseModel triggerCreateAppBuildGetQueuedResponse(TriggerCreateAppRequest request) throws Exception {
        String jobName = "create-app";
        JobWithDetails job = jenkinsServer.getJob(jobName);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appName", request.getName());
        paramMap.put("packageName", request.getPackageName());
        paramMap.put("ingressUrl", request.getDomainUrl());
        paramMap.put("numOfReplicas", String.valueOf(request.getReplicas()));
        QueueReference queueRef = job.build(paramMap);
        JenkinsBuildResponseModel jenkinsBuildResponseModel = buildResponseForQueuedJob(jobName, jobName, queueRef);
        return jenkinsBuildResponseModel;
    }

    public JenkinsBuildResponseModel fetchQueuedStatus(String appName, String currentJobName, String queuedUrl) throws Exception {
        QueueReference queueRef = new QueueReference(queuedUrl);
        return getCurrentQueuedStatus(appName, currentJobName, queueRef);
    }

    public JenkinsBuildResponseModel getCurrentQueuedStatus(String appName, String currentJobName, QueueReference queueRef) throws Exception {
        JobWithDetails job = jenkinsServer.getJob(currentJobName);
        QueueItem queueItem = jenkinsServer.getQueueItem(queueRef);
        if (queueItem.isCancelled()) {
            LOG.info("Job has been canceled.");
            return null;
        }
        if (job.isInQueue() && queueItem.getExecutable() == null) {
            return buildResponseForQueuedJob(appName, currentJobName, queueRef);
        }
        if (queueItem.getExecutable() != null) {
            Build currentBuild = job.getBuildByNumber(queueItem.getExecutable().getNumber().intValue());
            if (currentBuild == null) {
                LOG.info("No current build !");
                return null;
            }
            BuildWithDetails details = currentBuild.details();
            JenkinsBuildResponseModel responseModel = getResponseModelForBuildJob(currentJobName, details);
            responseModel.setCurrentBuildStatusUrl(getCurrentBuildUrl(currentJobName, details));
            return responseModel;
        }
        return null;
    }

    public JenkinsConsoleLog getConsoleLogFromSpecificIndex(String jobName, Integer buildNumber, Integer startIndex) throws Exception {
        JobWithDetails job = jenkinsServer.getJob(jobName);
        Build build = getBuild(buildNumber, job);
        int counter = 0;
        while (build == null && counter < 3) {
            Thread.sleep(300);
            build = getBuild(buildNumber, job);
            counter++;
        }
        BuildWithDetails details = build.details();
        ConsoleLog consoleLog = details.getConsoleOutputText(startIndex);
        JenkinsConsoleLog jenkinsConsoleLog = new JenkinsConsoleLog();
        jenkinsConsoleLog.setHasMoreText(consoleLog.getHasMoreData());
        jenkinsConsoleLog.setTextSize(consoleLog.getCurrentBufferSize());
        jenkinsConsoleLog.setLogText(consoleLog.getConsoleLog());
        if (consoleLog.getHasMoreData() != null && consoleLog.getHasMoreData()) {
            jenkinsConsoleLog.setNextLogUrl("/log/" + jobName + "?buildNumber="
                    + buildNumber + "&startIndex=" + consoleLog.getCurrentBufferSize());
        }
        return jenkinsConsoleLog;
    }

    public JenkinsBuildResponseModel getPipelineStepsBuildInfo(String jobName, Integer buildNumber, Integer pipelineStepIndex) throws Exception {
        try {
            if (pipelineStepIndex == null) {
                pipelineStepIndex = -1;
            }
            RestTemplate restTemplate = restTemplateBuilder.basicAuthorization("admin", jenkinsToken).build();
            ResponseEntity<Pipeline> pipelineResponseEntity = restTemplate.getForEntity(new URI(jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/wfapi/describe"), Pipeline.class);
            Pipeline pipeline = pipelineResponseEntity.getBody();
            JenkinsBuildResponseModel responseModel = new JenkinsBuildResponseModel();
            responseModel.setAppName(jobName.split("-pipeline")[0]);
            responseModel.setCurrentJobName(jobName);
            responseModel.setBuildId(buildNumber.toString());
            responseModel.setTimeStarted(new Date(pipeline.getStartTimeMillis()));
            if (pipeline.getStages() == null || pipeline.getStages().size() == 0) {
                if (pipeline.getStatus().equals(Status.RUNNING.getStatusName()) || pipeline.getStatus().equals(Status.QUEUED.getStatusName())) {
                    responseModel.setStatus(pipeline.getStatus());
                    responseModel.setCurrentBuildStatusUrl("/buildStatus/" + jobName + "?buildNumber=" + buildNumber + "&pipelineStepIndex=0");
                } else {
                    responseModel.setStatus(Status.FAILED.getStatusName());
                    responseModel.setCurrentJenkinsBuildUrl(jenkinsUrl + "/job/" + jobName + "/" + buildNumber);
                    responseModel.setActualDuration(pipeline.getDurationMillis());
                }
                return responseModel;
            } else {
                if (pipelineStepIndex < 0) {
                    int index = 0;
                    buildPipelineStepResponseModel(jobName, buildNumber, pipeline, responseModel, index);
                } else {
                    buildPipelineStepResponseModel(jobName, buildNumber, pipeline, responseModel, pipelineStepIndex);
                }
            }
            JobWithDetails job = jenkinsServer.getJob(jobName);
            Build build = getBuild(buildNumber, job);
            BuildWithDetails details = build.details();
            responseModel.setGitCommitId(JenkinsUtil.findJenkinsGitCommitFromActions(details.getActions()));
            responseModel.setConsoleLogUrl("/log/" + jobName + "?buildNumber="
                    + details.getId() + "&startIndex=0");
            return responseModel;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void buildPipelineStepResponseModel(final String jobName, final Integer buildNumber, final Pipeline pipeline, final JenkinsBuildResponseModel responseModel, final int index) {
        Stage currentStage = pipeline.getStages().get(index);
        PipelineStep pipelineStep = new PipelineStep(currentStage.getName(),
                currentStage.getStatus(), index, new Date(currentStage.getStartTimeMillis()), 10000L);
        responseModel.setPipelineStep(pipelineStep);
        responseModel.setStatus(pipeline.getStatus());
        if (pipelineStep.getPipelineStepStatus().equals(Status.RUNNING.getStatusName())) {
            responseModel.setCurrentBuildStatusUrl("/buildStatus/" + jobName + "?buildNumber=" + buildNumber + "&pipelineStepIndex=" + index);
        } else {
            if (pipeline.getStages().size() > (index + 1)) {
                int nextIndex = index + 1;
                responseModel.setNextBuildStatusUrl("/buildStatus/" + jobName + "?buildNumber=" + buildNumber + "&pipelineStepIndex=" + nextIndex);
            }
        }
    }

    public JenkinsBuildResponseModel getJobBuildInfo(String jobName, Integer buildNumber) throws Exception {
        JobWithDetails job = jenkinsServer.getJob(jobName);
        Build build = getBuild(buildNumber, job);
        if (build == null) {
            return null;
        }
        BuildWithDetails details = build.details();
        JenkinsBuildResponseModel responseModel = getResponseModelForBuildJob(jobName, details);
        if (details.getResult() != null && details.getResult().name().equals(BuildResult.SUCCESS.name())) {
            responseModel.setNextBuildStatusUrl(getInitialPipelineBuildUrl(jobName, responseModel.getAppName()));
        } else {
            responseModel.setCurrentBuildStatusUrl(getCurrentBuildUrl(jobName, details));
        }

        return responseModel;
    }


    public JenkinsBuildResponseModel findRunningBuild(String appName) throws Exception {
        String jobName = appName + "-pipeline";
        JobWithDetails job = jenkinsServer.getJob(jobName);
        Build lastBuild = job.getLastBuild();
        if (lastBuild != null) {
            BuildWithDetails lastBuildDetails = lastBuild.details();
            if (lastBuildDetails.isBuilding()) {
                return getPipelineStepsBuildInfo(jobName, Integer.valueOf(lastBuildDetails.getId()), null);
            }
        }
        return null;
    }

    /*public JenkinsBuildResponseModel triggerBuildUsingCommit(String appName, String gitCommitId) throws Exception {
        if (gitCommitId == null) {
            throw new Exception("Need some commitId");
        }
        String jobName = appName + "-build";
        JobWithDetails job = jenkinsServer.getJob(jobName);
        if (job == null) {
            return null;
        }
        QueueReference queueRef = job.build(Collections.singletonMap("commit_id", gitCommitId));
        return getResponseAfterTrigger(jobName, queueRef);
    }*/

    public String deleteAppFromJenkinsAndKube(String jobName) {
        try {
            jenkinsServer.deleteJob(jobName + "-pipeline");
            JobWithDetails job = jenkinsServer.getJob("delete-app-deployment");
            job.build(Collections.singletonMap("appName", jobName));
        } catch (IOException e) {
            e.printStackTrace();
            return "exception";
        }
        LOG.info("{}-build jenkins job deleted !!!", jobName);
        return "deleted";
    }

    public void deleteAppFromJenkins(String jobName) throws Exception {
        jenkinsServer.deleteJob(jobName);
    }

    public JobWithDetails getJobDetails(String jobName) throws Exception {
        return jenkinsServer.getJob(jobName);
    }


    public JenkinsBuildResponseModel buildResponseForQueuedJob(final String appName,
                                                                final String jobName,
                                                                final QueueReference queueRef) throws IOException, InterruptedException {
        JenkinsBuildResponseModel responseModel = new JenkinsBuildResponseModel();
        responseModel.setCurrentJobName(jobName);
        responseModel.setStatus(Status.QUEUED.getStatusName());
        responseModel.setCurrentJenkinsBuildUrl(queueRef.getQueueItemUrlPart());
        StringBuilder currentBuildStatusUrlBuilder = new StringBuilder("/queuedInfo");
        currentBuildStatusUrlBuilder.append("?appName=");
        currentBuildStatusUrlBuilder.append(appName);
        currentBuildStatusUrlBuilder.append("&currentJobName=");
        currentBuildStatusUrlBuilder.append(jobName);
        currentBuildStatusUrlBuilder.append("&queuedUrl=");
        currentBuildStatusUrlBuilder.append(queueRef.getQueueItemUrlPart());
        responseModel.setCurrentBuildStatusUrl(currentBuildStatusUrlBuilder.toString());
        responseModel.setAppName(appName);
        return responseModel;
    }

    /*private JenkinsBuildResponseModel getResponseAfterTrigger(final String jobName,
                                                              final QueueReference queueRef) throws IOException, InterruptedException {
        JobWithDetails job = jenkinsServer.getJob(jobName);
        QueueItem queueItem = jenkinsServer.getQueueItem(queueRef);
        while (!queueItem.isCancelled() && job.isInQueue() && queueItem.getExecutable() == null) {
            Thread.sleep(200);
            job = jenkinsServer.getJob(jobName);
            queueItem = jenkinsServer.getQueueItem(queueRef);
        }

        if (queueItem.isCancelled()) {
            LOG.info("Job has been canceled.");
            return null;
        }
        if (queueItem.getExecutable() == null) {
            LOG.info("No Job executed !");
            return null;
        }
        Build currentBuild = job.getBuildByNumber(queueItem.getExecutable().getNumber().intValue());
        if (currentBuild == null) {
            LOG.info("No current build !");
            return null;
        }
        BuildWithDetails details = currentBuild.details();
        JenkinsBuildResponseModel responseModel = getResponseModelForBuildJob(jobName, details);
        responseModel.setCurrentBuildStatusUrl(getCurrentBuildUrl(jobName, details));
        return responseModel;
    }*/

    private String getInitialPipelineBuildUrl(String jobName, String appName) throws IOException, InterruptedException {
        StringBuilder nextBuildUrlBuilder = new StringBuilder("");
        nextBuildUrlBuilder.append("/buildStatus/");
        nextBuildUrlBuilder.append(appName);
        nextBuildUrlBuilder.append("-pipeline");
        nextBuildUrlBuilder.append("?buildNumber=1&pipelineStepIndex=-1");
        return nextBuildUrlBuilder.toString();
    }

    private String getCurrentBuildUrl(final String jobName, final BuildWithDetails buildWithDetails) throws IOException {
        StringBuilder currentBuildUrlBuilder = new StringBuilder("");
        currentBuildUrlBuilder.append("/finalBuildStatus/");
        currentBuildUrlBuilder.append(jobName);
        currentBuildUrlBuilder.append("?buildNumber=");
        currentBuildUrlBuilder.append(buildWithDetails.getId());
        return currentBuildUrlBuilder.toString();
    }

    private Build getBuild(final Integer buildNumber, final JobWithDetails job) {
        Build build;
        if (buildNumber != null) {
            build = job.getBuildByNumber(buildNumber);
        } else {
            build = job.getLastBuild();
        }
        return build;
    }

    private JenkinsBuildResponseModel getResponseModelForBuildJob(String jobName, BuildWithDetails details) throws IOException {

        JenkinsBuildResponseModel responseModel = new JenkinsBuildResponseModel();
        String actualJobName = JenkinsUtil.findBuildParamName(details.getActions());
        int buildStringIndex = jobName.lastIndexOf("-pipeline");
        String appNameFromJobName = null;
        if (buildStringIndex != -1) {
            appNameFromJobName = jobName.substring(0, buildStringIndex);
        }
        responseModel.setAppName(actualJobName == null ? appNameFromJobName : actualJobName);
        responseModel.setCurrentJobName(jobName);
        responseModel.setBuildId(details.getId());
        responseModel.setActualDuration(details.getDuration());
        responseModel.setEstimatedDuration(details.getEstimatedDuration());
        responseModel.setCurrentJenkinsBuildUrl(details.getUrl());
        responseModel.setTimeStarted(new Date(details.getTimestamp()));
        responseModel.setStatus(details.getResult() == null ? Status.RUNNING.getStatusName() : details.getResult().name());
        if (!responseModel.getCurrentJobName().equals("create-app")) {
            responseModel.setGitCommitId(JenkinsUtil.findJenkinsGitCommitFromActions(details.getActions()));
        }
        responseModel.setConsoleLogUrl("/log/" + jobName + "?buildNumber="
                + details.getId() + "&startIndex=0");
        return responseModel;
    }


   /* public JenkinsBuildResponseModel triggerJenkinsJobForBuild(String commitId) throws Exception {
        try {
            String jobName = "app-1-salfa-pipeline";
            JobWithDetails job = jenkinsServer.getJob(jobName);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("commitId", commitId);
            QueueReference queueRef = job.build(paramMap);
            JenkinsBuildResponseModel jenkinsBuildResponseModel = buildResponseForQueuedJob(jobName, jobName, queueRef);

            Thread thread = new Thread() {
                public void run(){
                    try {
                        int logCurrentIndex = 0;
                        System.out.println("Pipeline Running");
                        JenkinsBuildResponseModel jenkinsBuildResponseModel1 = null;
                        JenkinsConsoleLog jenkinsConsoleLog = null;
                        while(true) {
                            Thread.sleep(2000);
                            jenkinsBuildResponseModel1 = getCurrentQueuedStatus(jobName, jobName, queueRef);
                            if(jenkinsBuildResponseModel1 != null) {
                                String buildId = jenkinsBuildResponseModel1.getBuildId().toString();
                                jenkinsConsoleLog = getConsoleLogFromSpecificIndex(jobName, Integer.parseInt(buildId), logCurrentIndex);
                                logCurrentIndex = jenkinsConsoleLog.getTextSize();
                                System.out.println(jenkinsConsoleLog.getLogText());
                                if(jenkinsBuildResponseModel1.getStatus().equals("FAILURE") || (jenkinsBuildResponseModel1.getNextBuildStatusUrl() == null && jenkinsBuildResponseModel1.getStatus().equals("SUCCESS"))){
                                    jenkinsConsoleLog = getConsoleLogFromSpecificIndex(jobName, Integer.parseInt(buildId), logCurrentIndex);
                                    System.out.println(jenkinsConsoleLog.getLogText());
                                    System.out.println(jenkinsBuildResponseModel1.getStatus());
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

            return jenkinsBuildResponseModel;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/


}

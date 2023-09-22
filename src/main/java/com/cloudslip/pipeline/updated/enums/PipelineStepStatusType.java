package com.cloudslip.pipeline.updated.enums;

public enum PipelineStepStatusType {

    NONE,
    PENDING,
    RUNNING,
    SUCCESS,
    SUCCESS_BUT_INACTIVE,
    KUBE_INGRESS_SUCCESS,
    KUBE_SERVICE_SUCCESS,
    KUBE_DEPLOY_SUCCESS,
    KUBE_INGRESS_FAILED,
    KUBE_SERVICE_FAILED,
    KUBE_DEPLOY_FAILED,
    PIPELINE_STARTED,
    CLONING_GIT,
    GRADLE_BUILDING,
    MAVEN_BUILDING,
    BUILDING_IMAGE,
    DEPLOYING_IMAGE,
    REMOVING_UNUSED_DOCKER_IMAGE,
    PIPELINE_SUCCESS,
    PIPELINE_START_FAILED,
    GIT_CLONE_FAILED,
    GRADLE_BUILD_FAILED,
    MAVEN_BUILD_FAILED,
    BUILDING_IMAGE_FAILED,
    DEPLOY_IMAGE_FAILED,
    REMOVE_UNUSED_DOCKER_IMAGE_FAILED,
    PIPELINE_FAILED,
    FAILED

}

package com.cloudslip.pipeline.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.AppsV1beta1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class KubernetesService {

    private static final String DEFAULT_NAMESPACE = "default";

    private String kubeDeployBody;

    /*public String deleteService(String serviceName) {
        try {
            restTemplate.delete(kubernetesUrl + ":" + kubernetesPort + "/apis/extensions/v1beta1/namespaces/default/deployments/" + serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            restTemplate.delete(kubernetesUrl + ":" + kubernetesPort + "/api/v1/namespaces/default/services/" + serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExtensionsV1beta1Api apiInstance = new ExtensionsV1beta1Api();
        try {
            V1beta1ReplicaSetList replicaSetList =
                    apiInstance.listNamespacedReplicaSet(DEFAULT_NAMESPACE, null, null, "app", null, null, null);
            replicaSetList.getItems().forEach(replica -> {
                String replicaSetName = replica.getMetadata().getName();
                String replicaAppName = replica.getMetadata().getLabels().get("app");
                if (serviceName.equals(replicaAppName)) {
                    restTemplate.delete(kubernetesUrl + ":" + kubernetesPort + "/apis/extensions/v1beta1/namespaces/default/replicasets/" + replicaSetName);
                }
            });
        } catch (ApiException e) {
            e.printStackTrace();
            return "exception";
        }

        CoreV1Api coreV1Api = new CoreV1Api();
        try {
            V1PodList v1PodList =
                    coreV1Api.listNamespacedPod(DEFAULT_NAMESPACE, null, null, "app", null, null, null);
            v1PodList.getItems().forEach(pod -> {
                String podName = pod.getMetadata().getName();
                String appName = pod.getMetadata().getLabels().get("app");
                if (serviceName.equals(appName)) {
                    restTemplate.delete(kubernetesUrl + ":" + kubernetesPort + "/api/v1/namespaces/default/pods/" + podName);
                }
            });
        } catch (ApiException e) {
            e.printStackTrace();
            return "exception";
        }

        return "deleted";
    }*/

    public String getAppUrl(String serviceName) {
        CoreV1Api api = new CoreV1Api();
        try {
            V1Service service = api.readNamespacedService(serviceName, DEFAULT_NAMESPACE, null, null, null);
            int port = service.getSpec().getPorts().get(0).getNodePort();
            StringBuilder stringBuilder = new StringBuilder("" + ":");
            stringBuilder.append(port);
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDeployedGitCommitId(String serviceName) {
        ExtensionsV1beta1Api apiInstance = new ExtensionsV1beta1Api();
        try {
            ExtensionsV1beta1Deployment v1beta1Deployment =
                    apiInstance.readNamespacedDeployment(serviceName, DEFAULT_NAMESPACE, null, null, null);
            String gitCommitId = v1beta1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getValue();
            return gitCommitId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deployAppWithChangedVersion(String appName, String commitId) {
        AppsV1Api apiInstance = new AppsV1Api();
        /*String jsonPatchStr =
                "{\"op\":\"replace\",\"path\":\"/spec/template/spec/containers/0/image\",\"value\":\"dckreg:5000/" + appName + ":" + commitId + "\"}";*/
        String jsonPatchStr = String.format(kubeDeployBody, appName, commitId, commitId);
        System.out.println(jsonPatchStr);
        JsonArray arr = ((JsonArray) deserialize(jsonPatchStr, JsonArray.class)).getAsJsonArray();
        String pretty = "pretty_example"; // String | If 'true', then the output is pretty printed.
        try {
            V1Deployment result = apiInstance.patchNamespacedDeployment(appName, DEFAULT_NAMESPACE, arr, pretty);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppsV1Api#patchNamespacedDeployment");
            e.printStackTrace();
        }
    }

    public Object deserialize(String jsonStr, Class<?> targetClass) {
        Object obj = (new Gson()).fromJson(jsonStr, targetClass);
        return obj;
    }
}

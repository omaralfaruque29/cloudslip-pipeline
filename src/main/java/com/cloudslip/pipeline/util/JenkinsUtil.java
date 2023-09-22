package com.cloudslip.pipeline.util;

import java.util.List;
import java.util.Map;

public final class JenkinsUtil {

    private JenkinsUtil() {

    }

    public static String findJenkinsGitCommitFromActions(List actions) {
        if (actions == null || actions.size() == 0) {
            return null;
        }
        for (Object action : actions) {
            Map<String, Object> actionMap = (Map<String, Object>) action;
            if (actionMap.containsKey("lastBuiltRevision")) {
                Map<String, Object> revisionMap = (Map<String, Object>) actionMap.get("lastBuiltRevision");
                if (revisionMap.containsKey("branch")) {
                    List<Map<String, Object>> branches = (List<Map<String, Object>>) revisionMap.get("branch");
                    return String.valueOf(branches.get(0).get("SHA1"));
                }
                break;
            }
        }
        return null;
    }

    public static String findBuildParamName(List actions) {
        if (actions == null || actions.size() == 0) {
            return null;
        }
        for (Object action : actions) {
            Map<String, Object> actionMap = (Map<String, Object>) action;
            if (actionMap.containsKey("parameters")) {
                List<Map<String, Object>> parameters = (List<Map<String, Object>>) actionMap.get("parameters");
                if (parameters != null && parameters.size() > 0 && String.valueOf(parameters.get(0).get("name")).equals("appName")) {
                    return String.valueOf(parameters.get(0).get("value"));
                }
                break;
            }
        }
        return null;
    }

    public static List<String> findNextJobUrls() {
        return null;
    }
}

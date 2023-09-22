package com.cloudslip.pipeline.updated.util;

import com.cloudslip.pipeline.updated.core.YamlObjectMapper;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.UserType;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.model.universal.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.types.ObjectId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.security.*;
import java.time.ZonedDateTime;
import java.util.*;

public class Utils {

    private static JsonParser jsonParser = new JsonParser();

    public static UserInfo getCurrentUser() {
        return new UserInfo();
    }

    public static UserInfo getUser() {
        return new UserInfo();
    }

    public static User generateUserFromJsonStr(String userStr) {
        JsonObject currentUserJO = jsonParser.parse(userStr).getAsJsonObject();
        User currentUser = new User();
        try {
            currentUser.setId(currentUserJO.has("id") ? new ObjectId(currentUserJO.get("id").getAsString()) : null);
            currentUser.setUsername(currentUserJO.has("username") ? currentUserJO.get("username").getAsString() : null);
            currentUser.setEnabled(currentUserJO.has("isEnabled") ? currentUserJO.get("isEnabled").getAsBoolean() : false);
            String authoritiesStr = currentUserJO.has("authorities") ? currentUserJO.get("authorities").getAsString() : "";
            List<String> authorityStrList = Arrays.asList(authoritiesStr.split(","));
            List<Authority> authorities = new ArrayList<>();
            for (String authorityStr : authorityStrList) {
                if(authorityStr.equals(Authority.ROLE_SUPER_ADMIN.getAuthority())) {
                    authorities.add(Authority.ROLE_SUPER_ADMIN);
                } else if(authorityStr.equals(Authority.ROLE_ADMIN.getAuthority())) {
                    authorities.add(Authority.ROLE_ADMIN);
                } else if(authorityStr.equals(Authority.ROLE_DEV.getAuthority())) {
                    authorities.add(Authority.ROLE_DEV);
                } else if(authorityStr.equals(Authority.ROLE_OPS.getAuthority())) {
                    authorities.add(Authority.ROLE_OPS);
                } else if (authorityStr.equals(Authority.ROLE_AGENT_SERVICE.getAuthority())) {
                    authorities.add(Authority.ROLE_AGENT_SERVICE);
                } else if (authorityStr.equals(Authority.ROLE_GIT_AGENT.getAuthority())) {
                    authorities.add(Authority.ROLE_GIT_AGENT);
                }
            }
            currentUser.setAuthorities(authorities);
            currentUser.setUserType((currentUserJO.has("userType") && currentUserJO.get("userType").getAsString() != "") ? UserType.valueOf(currentUserJO.get("userType").getAsString()) : null);
            currentUser.setCompanyId((currentUserJO.has("companyId") && currentUserJO.get("companyId").getAsString() != "") ? new ObjectId(currentUserJO.get("companyId").getAsString()) : null);
            currentUser.setOrganizationId((currentUserJO.has("organizationId") && currentUserJO.get("organizationId").getAsString() != null && (!isStringEquals(currentUserJO.get("organizationId").getAsString(), ""))) ? new ObjectId(currentUserJO.get("organizationId").getAsString()) : null);

            String teamIdsStr = currentUserJO.has("teamIds") ? currentUserJO.get("teamIds").getAsString() : "";
            List<String> teamIdStrList = Arrays.asList(teamIdsStr.split(","));
            List<ObjectId> teamIdList = new ArrayList<>();
            for (String teamId : teamIdStrList) {
                teamIdList.add(new ObjectId(teamId));
            }
            currentUser.setTeamIdList(teamIdList);

        } catch (Exception ex) {
            return  currentUser;
        }
        return currentUser;
    }

    public static HttpHeaders generateHttpHeaders(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("current-user", user.toJsonString());
        return headers;
    }

    public static HttpHeaders generateHttpHeaders(User user, String actionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("current-user", user.toJsonString());
        headers.add("action-id", actionId);
        return headers;
    }

    public static HttpHeaders generateHttpHeaders(String userStr) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("current-user", userStr);
        return headers;
    }

    public static HttpHeaders generateHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("current-user", "");
        headers.add("action-id", "");
        return headers;
    }

    public static String removeAllSpaceWithUnderScore(String input) {
        return input.replaceAll("\\s+","_");
    }

    public static String removeAllSpaceWithDash(String input) {
        return input.replaceAll("\\s+","-");
    }

    public static String convertYamlToJson(String yaml, ObjectMapper jsonObjectMapper, YamlObjectMapper yamlObjectMapper){
        Object obj = null;
        try {
            obj = yamlObjectMapper.readValue(yaml, Object.class);
            return jsonObjectMapper.writeValueAsString(obj);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static boolean isStringEquals(String val1, String val2) {
        if(val1 == val2 || val1.equals(val2)) {
            return true;
        }
        return false;
    }

    public static String formatZonedDateTime(ZonedDateTime zonedDateTime) {
        if(zonedDateTime != null) {
            String zonedDateTimeStr = zonedDateTime.toString();
            int thridBraceOpenIndex = zonedDateTimeStr.indexOf('[');
            zonedDateTimeStr = zonedDateTimeStr.substring(0, thridBraceOpenIndex);
            return zonedDateTimeStr;
        }
        return null;
    }

    public static String getBase64EncodedString(String originalString){
        String encodedString = Base64.getEncoder().encodeToString(originalString.getBytes());
        return encodedString;
    }

    public static String getBase64DecodedString(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    public static String generateRandomString(int length) {
        String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder( length );
        for( int i = 0; i < length; i++ ) {
            sb.append(ALLOWED_CHARACTERS.charAt(rnd.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }

}

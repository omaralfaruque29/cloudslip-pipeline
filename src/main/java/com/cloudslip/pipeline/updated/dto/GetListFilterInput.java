package com.cloudslip.pipeline.updated.dto;


import com.cloudslip.pipeline.updated.constant.ListFetchMode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

public class GetListFilterInput extends BaseInputDTO {

    private String fetchMode;
    private String filterParams;
    private HashMap<String, String> filterParamsMap;

    public GetListFilterInput() {
        this.fetchMode = "PAGINATION";
        this.filterParams = "";
        this.filterParamsMap = new HashMap<String, String>();
    }

    public GetListFilterInput(String fetchMode, String filterParams) {
        this.fetchMode = fetchMode;
        this.filterParams = filterParams;
    }

    public GetListFilterInput(String fetchMode, String filterParams, HashMap<String, String> filterParamsMap) {
        this.fetchMode = fetchMode;
        this.filterParams = filterParams;
        this.filterParamsMap = filterParamsMap;
    }

    public String getFetchMode() {
        return fetchMode;
    }

    public void setFetchMode(String fetchMode) {
        this.fetchMode = fetchMode;
    }

    public String getFilterParams() {
        return filterParams == null ? "" : filterParams;
    }

    public void setFilterParams(String filterParams) {
        this.filterParams = filterParams;
        this.generateFilterParamsMap();
    }

    public HashMap<String, String> getFilterParamsMap() {
        return filterParamsMap;
    }

    public void setFilterParamMap(HashMap<String, String> filterParamsMap) {
        this.filterParamsMap = filterParamsMap;
    }

    private void generateFilterParamsMap() {
        if(this.filterParams ==  null || this.filterParams.equals("")) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.filterParamsMap = mapper.readValue(this.filterParams, new TypeReference<HashMap<String, String>>(){});
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateRequestParamUrl() {
        return "fetchMode=" + (this.fetchMode == null ? ListFetchMode.PAGINATION : this.fetchMode) + "&filterParams=" + (this.filterParams == null ? "" : this.filterParams);
    }
}

package com.cloudslip.pipeline.updated.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlObjectMapper extends ObjectMapper {

    public YamlObjectMapper(JsonFactory jsonFactory) {
        super(jsonFactory);
    }

}

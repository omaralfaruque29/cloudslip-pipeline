package com.cloudslip.pipeline.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static String convertYamlToJson(String yaml) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter jsonWriter = objectMapper.writer();
        YAMLParser yamlParser = new YAMLFactory().createParser(yaml);
        List<Object> obj = objectMapper.readValues(yamlParser, new TypeReference<Object>(){}).readAll();


        return jsonWriter.writeValueAsString(obj);
    }

    public static StringBuilder replaceVars(StringBuilder strBuilder, String var, String replacement) {
        Matcher matcher = Pattern.compile(var).matcher(strBuilder);
        int start = 0;
        while (matcher.find(start)) {
            strBuilder.replace(matcher.start(), matcher.end(), replacement);
            start = matcher.start() + replacement.length();
        }
        return strBuilder;
    }
}

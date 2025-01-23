package org.drools.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@Slf4j
public class Utils {

    private Utils() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static Map<String, Double> toMap(String object) {
        log.debug("From DRL ::::::::::::: " + object);
        object = object.replace('=', ':');
        if (object.equalsIgnoreCase("{}"))
            return new HashMap<>();
        HashMap<String, Double> hashMap = null;
        try {
            hashMap = mapper.readValue(object, HashMap.class);
        } catch (JsonProcessingException e) {
            log.error("converting object to hashmap failed ..", e);
        }
        log.debug("generated map:::::" + hashMap);
        return hashMap;
    }

    public static String formattedSecID(Object id) {
        String securityId = "SEC_" + id;
        securityId = securityId.concat("_");
        log.debug("formatted security ID :::: {}", securityId);
        return securityId;
    }
}

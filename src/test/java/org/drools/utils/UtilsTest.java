package org.drools.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

@RunWith(JUnit4.class)
public class UtilsTest {
    ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @Test
    public void toMapTest() {
        Map<String, Double> stateLevelMap = new HashMap<>();
        stateLevelMap.put("OH", 5000D);
        String input = "{OH:5000}";
        try {
            Map<String, Double> result = Utils.toMap(input);
            assertEquals(stateLevelMap.size(), result.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}

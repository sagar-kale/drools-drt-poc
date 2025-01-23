package org.drools.contollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.drools.controllers.ValidationController;
import org.drools.engine.RuleEngine;
import org.drools.engine.RulesCache;
import org.drools.model.Order;
import org.drools.service.RuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@RunWith(SpringRunner.class)
@WebMvcTest(ValidationController.class)
public class ValidationControllerTest {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    ObjectMapper mapper;
    ObjectWriter ow;

    static {

    }

    @InjectMocks ValidationController controller;
    @Autowired private MockMvc mockMvc;
    @MockBean private RuleService ruleService;
    @MockBean private RuleEngine engine;
    @MockBean private RulesCache ruleCache;

    @Before
    public void init() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ow = mapper.writer().withDefaultPrettyPrinter();
    }
    @Test
        public void testExecuteRules() {
        String result;
        try {
            Order order = new Order();

            String requestJson = ow.writeValueAsString(order);
            System.out.println(requestJson);
            result = mockMvc.perform(post("/validate/order").contentType(APPLICATION_JSON_UTF8)
                    .content(requestJson))
                    .andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();
            Object resultObj = mapper.readValue(result, Order.class);
            assertEquals("SUCCESS", ((Order) resultObj).getStatus());

        } catch (Exception e) {
            fail("Exception e" + e.getMessage());
        }

    }
}

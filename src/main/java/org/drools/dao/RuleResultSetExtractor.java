package org.drools.dao;


import lombok.extern.slf4j.Slf4j;
import org.drools.model.RuleInput;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Sagar Kale
 * @version 1.0
 * @CreatedAt May 07 , 2020
 */

@Slf4j
@Component
@ConfigurationProperties(prefix = "ignore-rule-list")
public class RuleResultSetExtractor implements ResultSetExtractor<HashMap<String, List<RuleInput>>> {

    private final String[] thresholdColumns = {"MINIMUM_PURCHASE_AT", "MIN_ANNUAL_INCOME_AT", "LIQUID_NET_WORTH_AT", "NET_WORTH_AT", "NET_WORTH_ALT_MINIMUM_AT"};
    private final Set<String> ruleTypesToIgnore = new HashSet<>();
    private final Set<String> securitiesToIgnore = new HashSet<>();

    public HashMap<String, List<RuleInput>> extractData(ResultSet rs) {

        log.debug("Ignored rule list ::: {}, Ignored security list :: {} and threshold columns:::: {}", ruleTypesToIgnore, securitiesToIgnore, thresholdColumns);

        HashMap<String, List<RuleInput>> rulesByType = new HashMap<>();
        HashMap<String, Map<String, RuleInput>> rulesByTypeBySecurity = new HashMap<>();

        initializeTypes(rulesByType, rulesByTypeBySecurity);

        try {
            while (rs.next()) {
                String security = rs.getString("ALT_IVT_INSTRUMENT_ID");
                if (!securitiesToIgnore.contains(security)) {
                    String state = rs.getString("COUNTRY_STATE_PROVINCE_ID");
                    if (state != null)
                        state = state.trim();

                    extractData(rs, rulesByType, rulesByTypeBySecurity, security, state);
                }
            }
        } catch (SQLException e) {
            // TODO: 5/12/2020 See what else needs to be logged
            log.error(e.getMessage());
            throw new RuntimeException("Error reading details from database.");
        }

        return rulesByType;
    }

    private void extractData(ResultSet rs, HashMap<String, List<RuleInput>> rulesByType, HashMap<String, Map<String, RuleInput>> rulesByTypeBySecurity, String security, String state) throws SQLException {
        for (String columnName : thresholdColumns) {
            if (isRuleTypeToBeEvaluated(columnName)) {
                /*// TODO: 5/12/2020  We need to confirm what needs to be data type for these */
                double value = rs.getDouble(columnName);
                if (value != 0) {
                    RuleInput rule = rulesByTypeBySecurity.get(columnName).get(security);
                    if (rule == null) {
                        rule = new RuleInput(security, columnName);
                        rulesByTypeBySecurity.get(columnName).put(security, rule);
                        rulesByType.get(columnName).add(rule);
                    }
                    rule.addValue(state, value);
                }
            }
        }
    }

    private void initializeTypes(HashMap<String, List<RuleInput>> rulesByType, HashMap<String, Map<String, RuleInput>> rulesByTypeBySecurity) {
        Arrays
                .stream(thresholdColumns)
                .filter(this::isRuleTypeToBeEvaluated)
                .forEach(column ->
                {
                    rulesByType.put(column, new LinkedList<>());
                    rulesByTypeBySecurity.put(column, new HashMap<>());
                });
    }

    private boolean isRuleTypeToBeEvaluated(String column) {
        return !ruleTypesToIgnore.contains(column);
    }
}

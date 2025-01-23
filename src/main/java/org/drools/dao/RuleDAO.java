package org.drools.dao;

import lombok.extern.slf4j.Slf4j;
import org.drools.model.RuleInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RuleDAO {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RuleResultSetExtractor extractor;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        final CustomSQLErrorCodeTranslator customSQLErrorCodeTranslator = new CustomSQLErrorCodeTranslator();
        jdbcTemplate.setExceptionTranslator(customSQLErrorCodeTranslator);

    }

    public Map<String, List<RuleInput>> getRules() {
        return getRules("", "");
    }

    public Map<String, List<RuleInput>> getRules(String security) {
        if (security == null) {
            throw new NullPointerException("Security id cannot be null");
        }
        String securityFilterGL = " AND GL.ALT_IVT_INSTRUMENT_ID =" + security;
        String securityFilterState = " AND STATE.ALT_IVT_INSTRUMENT_ID =" + security;
        return getRules(securityFilterState, securityFilterGL);
    }

    private HashMap<String, List<RuleInput>> getRules(String securityFilterState, String securityFilterGL) {
        String sql = "SELECT STATE.ALT_IVT_INSTRUMENT_ID, STATE.COUNTRY_STATE_PROVINCE_ID, " +
                "STATE.MINIMUM_PURCHASE_AT, STATE.MIN_ANNUAL_INCOME_AT, " +
                "STATE.LIQUID_NET_WORTH_AT, STATE.NET_WORTH_AT, STATE.NET_WORTH_ALT_MINIMUM_AT " +
                "FROM FM9 STATE " +
                "WHERE (COALESCE(STATE.MINIMUM_PURCHASE_AT,0)<>0 OR COALESCE(STATE.MIN_ANNUAL_INCOME_AT,0)<>0 OR " +
                "COALESCE(STATE.LIQUID_NET_WORTH_AT,0)<>0 OR COALESCE(STATE.NET_WORTH_AT,0)<>0 OR " +
                "COALESCE(STATE.NET_WORTH_ALT_MINIMUM_AT,0)<>0 ) " +
                securityFilterState + " " +
                "UNION " +
                "SELECT GL.ALT_IVT_INSTRUMENT_ID, NULL, " +
                "GL.MINIMUM_PURCHASE_AT, GL.MIN_ANNUAL_INCOME_AT, GL.LIQUID_NET_WORTH_AT, " +
                "GL.NET_WORTH_AT, GL.NET_WORTH_ALT_MINIMUM_AT " +
                "FROM FM8 GL " +
                "WHERE (COALESCE(GL.MINIMUM_PURCHASE_AT,0)<>0 OR " +
                "COALESCE(GL.MIN_ANNUAL_INCOME_AT,0)<>0 OR " +
                "COALESCE(GL.LIQUID_NET_WORTH_AT,0)<>0 OR " +
                "COALESCE(GL.NET_WORTH_AT,0)<>0 OR COALESCE(GL.NET_WORTH_ALT_MINIMUM_AT,0)<>0) " +
                securityFilterGL;

        return jdbcTemplate.query(sql, extractor);
    }

}

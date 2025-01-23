package org.drools.model;

import java.util.HashMap;
import java.util.Map;

public class RuleInput {

    private final String securityId;
    private double globalValue;
    private final String name;
    private final Map<String, Double> stateLevelMap;

    public RuleInput(String securityId, String name) {
        this.securityId = securityId;
        this.name = name;
        stateLevelMap = new HashMap<>();
    }

    public void addValue(String state, Double value) {
        if (state != null)
            stateLevelMap.put(state, value);
        else
            globalValue = value;
    }


    public String getSecurityId() {
        return securityId;
    }

    public String getName() {
        return name;
    }

    public Map<String, Double> getStateLevelMap() {
        return stateLevelMap;
    }

    public double getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(double globalValue) {
        this.globalValue = globalValue;
    }
}

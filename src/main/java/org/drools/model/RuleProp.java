package org.drools.model;

import lombok.ToString;

/**
 * @author - Sagar Kale dt225541
 * @version 1.0
 * @createddate - May 07 2020
 **/
@ToString
public class RuleProp {
    private Long delay;
    private Integer cacheSecuritySize;
    private Integer ruleHoldDuration;

    public Long getDelay() {
        return delay;
    }

    public RuleProp setDelay(Long delay) {
        this.delay = delay;
        return this;
    }

    public Integer getCacheSecuritySize() {
        return cacheSecuritySize;
    }

    public RuleProp setCacheSecuritySize(Integer cacheSecuritySize) {
        this.cacheSecuritySize = cacheSecuritySize;
        return this;
    }

    public Integer getRuleHoldDuration() {
        return ruleHoldDuration;
    }

    public RuleProp setRuleHoldDuration(Integer ruleHoldDuration) {
        this.ruleHoldDuration = ruleHoldDuration;
        return this;
    }
}

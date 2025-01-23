package org.drools.model;

import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author - Sagar Kale dt225541
 * @version 1.0
 * @createddate - May 07 2020
 **/

@Component
@ToString
public class DroolsProps {

    @Value("${drools.lazy.loading}")
    private boolean lazyLoading;

    @Value("${drools.cache.cleaner.enabled}")
    private boolean isCleanerEnabled;

    @Value("${drools.cache.cleaner.delay}")
    private Long delay;

    @Value("${drools.cache.memory.limit}")
    private Integer memLimit;
    @Value("${drools.cache.memory.rules.hold-duration}")
    private Integer ruleHoldDuration;

    public boolean isLazyLoading() {
        return lazyLoading;
    }

    public DroolsProps setLazyLoading(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;
        return this;
    }

    public boolean isCleanerEnabled() {
        return isCleanerEnabled;
    }

    public DroolsProps setCleanerEnabled(boolean cleanerEnabled) {
        isCleanerEnabled = cleanerEnabled;
        return this;
    }

    public Long getDelay() {
        return delay;
    }

    public DroolsProps setDelay(Long delay) {
        this.delay = delay;
        return this;
    }

    public Integer getMemLimit() {
        return memLimit;
    }

    public DroolsProps setMemLimit(Integer memLimit) {
        this.memLimit = memLimit;
        return this;
    }

    public Integer getRuleHoldDuration() {
        return ruleHoldDuration;
    }

    public DroolsProps setRuleHoldDuration(Integer ruleHoldDuration) {
        this.ruleHoldDuration = ruleHoldDuration;
        return this;
    }
}

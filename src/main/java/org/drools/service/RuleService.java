package org.drools.service;

import lombok.extern.slf4j.Slf4j;
import org.drools.engine.RuleEngine;
import org.drools.engine.RulesCache;
import org.drools.model.Action;
import org.drools.model.DroolsProps;
import org.drools.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@Service
@Slf4j
public class RuleService implements IRuleService {

    @Autowired
    private RuleEngine engine;
    @Autowired
    private RulesCache ruleCache;

    @Autowired
    private DroolsProps droolsProps;

    @Override
    public boolean removeRulesFromMemory(String securityId) {
        List<String> ruleNames = engine.getRuleNames();
        if (null == ruleNames) {
            return false;
        }
        String formattedSecID = Utils.formattedSecID(securityId.trim());
        List<String> toBeRemovedRules = ruleNames
                .stream()
                .filter(rule -> rule.contains(formattedSecID))
                .collect(Collectors.toList());
        engine.removeRule(toBeRemovedRules);
        log.info("removed rules from memory :: {}", toBeRemovedRules.size());
        return true;
    }

    @Override
    public void performAction(String action, Action type) {
        action = action.trim();
        if (type == Action.CLEANER) cleanerOnOff(action);
        else if (type == Action.LAZY_LOADING) lazyLoadingOnOff(action);
    }

    private void lazyLoadingOnOff(String action) {
        log.info("Acquiring write lock ...");

        try {
            ruleCache.writeLock();
            removeAllRulesFromMem();
            if (action.equalsIgnoreCase("on")) {
                droolsProps.setLazyLoading(true);
                log.info("lazy loading has been enabled....");
                return;
            }
            log.info("lazy loading has been disabled....");
            droolsProps.setLazyLoading(false);
            engine.loadAllRules();
        } finally {
            log.info("Releasing lock ...");
            ruleCache.unlockWriteLock();
        }
    }

    private void cleanerOnOff(String action) {
        droolsProps.setCleanerEnabled(action.equalsIgnoreCase("on"));
        log.info("Cleaner has been turned {}....", action);
    }


    @Override
    public void removeAllRulesFromMem() {
        ruleCache.emptyCache();
        if (engine.getRuleNames() != null && !engine.getRuleNames().isEmpty())
            engine.cleanAllRules();
    }

}


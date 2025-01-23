package org.drools.controllers;

import lombok.extern.slf4j.Slf4j;
import org.drools.engine.RuleEngine;
import org.drools.model.Action;
import org.drools.model.DroolsProps;
import org.drools.model.RuleProp;
import org.drools.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@RestController
@Slf4j
@RequestMapping("/config/rules")
public class RuleCacheController {
    @Autowired
    private DroolsProps droolsProps;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleEngine engine;

    @PostMapping(value = "cleaner/{action}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> turnOnOffCacheCleaner(@PathVariable String action) {
        if (!droolsProps.isLazyLoading())
            return ResponseEntity.status(HttpStatus.LOCKED).body("lazy loading is disabled, please enable it in order to use cache cleaner");

        log.info("In turnOnOffCacheCleaner method, action requested :: {}", action);

        if (droolsProps.isCleanerEnabled() && null != action && action.equalsIgnoreCase("on"))
            return ResponseEntity.ok("Cache cleaner already enabled");
        else if (!droolsProps.isCleanerEnabled() && null != action && action.equalsIgnoreCase("off"))
            return ResponseEntity.ok("Cache cleaner already disabled");
        else {
            ruleService.performAction(action, Action.CLEANER);
            return ResponseEntity.ok("Action " + action + " performed success");
        }
    }

    @PostMapping(value = "lazyloading/{action}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> turOnOffLazyLoading(@PathVariable String action) {

        log.info("In turOnOffLazyLoading method, action requested :: {}", action);

        if (droolsProps.isLazyLoading() && null != action && action.equalsIgnoreCase("on"))
            return ResponseEntity.ok("lazy loading already enabled");
        else if (!droolsProps.isLazyLoading() && null != action && action.equalsIgnoreCase("off"))
            return ResponseEntity.ok("lazy loading already disabled");
        else {
            ruleService.performAction(action, Action.LAZY_LOADING);
            return ResponseEntity.ok("Action " + action + " performed success");
        }
    }

    @PostMapping(value = "/properties", produces = MediaType.APPLICATION_JSON_VALUE)
    public DroolsProps updateProperties(@RequestBody RuleProp props) {
        if (null == props)
            return droolsProps;

        if (props.getDelay() != null)
            droolsProps.setDelay(props.getDelay());
        if (props.getCacheSecuritySize() != null)
            droolsProps.setMemLimit(props.getCacheSecuritySize());
        if (props.getRuleHoldDuration() != null)
            droolsProps.setRuleHoldDuration(props.getRuleHoldDuration());

        log.info("Updated properties ::: {}", droolsProps);
        return droolsProps;

    }

    @GetMapping(value = "/properties", produces = MediaType.APPLICATION_JSON_VALUE)
    public DroolsProps properties() {
        return droolsProps;
    }

    @GetMapping
    public Map<String, Object> listRules() {
        List<String> rulesList = engine.getRuleNames();
        HashMap<String, Object> map = new HashMap<>();
        if (rulesList == null)
            rulesList = new ArrayList<>();
        map.put("count", rulesList.size());
        map.put("rules", rulesList);
        return map;
    }
}

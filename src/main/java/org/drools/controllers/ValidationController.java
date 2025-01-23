package org.drools.controllers;

import lombok.extern.slf4j.Slf4j;
import org.drools.engine.RuleEngine;
import org.drools.engine.RulesCache;
import org.drools.model.Order;
import org.drools.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@RestController
@Slf4j
public class ValidationController {

    @Autowired
    private RuleEngine engine;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RulesCache ruleCache;

    @PostMapping("/validate/order")
    public Order executeRules(@RequestBody Order order) {


        try {
            ruleCache.readLock();
            engine.prepareCache(order.getSecurityId());
            engine.execute(order);
            order.setStatus(order.getErrors().isEmpty() ? "SUCCESS" : "FAILED");
        } finally {
            ruleCache.unlockReadLock();
        }
        return order;
    }
}

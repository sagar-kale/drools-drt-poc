package org.drools.service;

import org.drools.model.Action;

public interface IRuleService {
    boolean removeRulesFromMemory(String securityId);

    void performAction(String action, Action type);

    void removeAllRulesFromMem();
}

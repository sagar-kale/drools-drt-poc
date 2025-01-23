package org.drools.engine;

import lombok.extern.slf4j.Slf4j;
import org.drools.dao.RuleDAO;
import org.drools.model.DroolsProps;
import org.drools.model.Order;
import org.drools.model.RuleInput;
import org.drools.service.RuleService;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.conf.ConstraintJittingThresholdOption;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@Component
@Slf4j
@ConfigurationProperties(prefix = "drools")
public class RuleEngine {
    private KieBase kieBase = null;
    private KieFileSystem kieFileSystem = null;
    private static final String RESOURCE_FILE_PATH = "src/main/resources/rules/";
    private static final String RULE_FILE_EXTENSION = ".drl";
    private static final String PACKAGE_NAME = "org.drools";
    @Autowired
    private RuleDAO dao;
    @Autowired
    private RulesCache ruleCache;
    @Autowired
    private RuleService ruleService;
    @Autowired
    private DroolsProps droolsProps;
    private KieServices kieServices = null;

    List<String> ruleTypes = new ArrayList<>();

    public void setRuleTypes(List<String> ruleTypes) {
        this.ruleTypes = ruleTypes;
    }

    private static InputStream getRulesStream(String name) throws IOException {
        return new ClassPathResource(name.trim() + ".drt").getInputStream();
    }

    /**
     * initialize drools at the start of the app
     * Check weather lazy loading option is enabled or not
     **/
    @PostConstruct
    public void init() {
        log.debug("initializing Drools Kie Engine");
        if (droolsProps.isLazyLoading()) initKie();
        else loadAllRules();
        log.info("initializing rules ..... completed... ");
    }

    private void instantiateKsAndKfs() {
        if (kieServices == null || kieFileSystem == null) {
            kieServices = KieServices.Factory.get();
            kieFileSystem = kieServices.newKieFileSystem();
        }
    }

    private void initKie() {
        log.info("initializing engine.....");
        instantiateKsAndKfs();
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.WARNING, Message.Level.
                ERROR)) {
            List<Message> messages = results.getMessages(Message.
                    Level.WARNING, Message.Level.ERROR);
            for (Message message : messages) {
                log.info("Error: " + message.getText());
            }

            throw new IllegalStateException("Compilation errors were found. Check the logs.");
        }
        KieContainer kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        KieBaseConfiguration configuration = kieServices.newKieBaseConfiguration();
        configuration.setOption(ConstraintJittingThresholdOption.get(-1));
        kieBase = kieContainer.getKieBase();
        log.info("initializing rules ..... completed... ");
    }

    public void prepareCache(String securityId) {
        if (!droolsProps.isLazyLoading()) {
            return;
        }
        boolean ruleExistsInKieMemory = checkIfRuleExistsInKieMemory(securityId);
        if (!ruleExistsInKieMemory) {
            try {
                ruleCache.unlockReadLock();
                ruleCache.writeLock();
                if (!checkIfRuleExistsInKieMemory(securityId.trim())) addSecurityIdRules(securityId);
            } finally {
                ruleCache.unlockWriteLock();
            }
            ruleCache.readLock();
        }
        ruleCache.refreshLastAccessTime(securityId.trim());
    }

    public boolean checkIfRuleExistsInKieMemory(String securityId) {
        log.info("Checking if rule is exists for security id ={} ", securityId);
        boolean match = ruleCache.isSecurityAvailable(securityId);
        if (match) ruleCache.refreshLastAccessTime(securityId);
        return match;
    }

    private void addSecurityIdRules(String securityId) {
        Map<String, List<RuleInput>> rulesByType = dao.getRules(securityId);
        compileRules(rulesByType, securityId);
    }

    public void loadAllRules() {

        droolsProps.setCleanerEnabled(false);
        log.info("Lazy loading disabled .... Loading all rules ...");
        Map<String, List<RuleInput>> rulesByType = dao.getRules();
        compileRules(rulesByType, null);
    }

    private void compileRules(Map<String, List<RuleInput>> rulesByType, String securityId) {

        instantiateKsAndKfs();
        ObjectDataCompiler dataCompiler = new ObjectDataCompiler();

        boolean ruleExists = false;
        for (String ruleType : ruleTypes) {
            String type = ruleType.trim();
            List<RuleInput> rules = rulesByType.get(type);
            if (null != rules && !rules.isEmpty()) {
                ruleExists = true;
                String drl = null;
                try {
                    String ruleName = null;
                    drl = dataCompiler.compile(rules, getRulesStream(type.trim()));

                    if (null != securityId) {
                        ruleName = "SEC_".concat(securityId).concat("_").concat(type);
                    } else {
                        ruleName = type;
                    }

                    writeRulesInMemory(drl, ruleName);
                } catch (IOException e) {
                    log.error("some error occurred ", e);
                    throw new RuntimeException("error occurred while opening drt...", e);
                }
            }
        }
        if (!ruleExists) {
            log.info("No new rules compiled...");
            return;
        }
        initKie();
    }

    public void writeRulesInMemory(String drl, String rulename) {
        String filename = getFileName(rulename);
        log.info("rule Name ::::::::::::: {}", rulename);
        loadRule(drl, filename);
        log.info("new rule:  " + rulename + " loaded in kie file system");
    }

    public void removeRule(List<String> ruleNames) {
        ruleNames
                .stream()
                .filter(ruleName -> null != kieBase.getRule(PACKAGE_NAME, ruleName))
                .forEach(ruleName -> {
                    log.info("rule: {} removed.", ruleName);
                    kieBase.removeRule(PACKAGE_NAME, ruleName);

                });

        String[] fileNames = ruleNames.stream().map(this::getFileName).toArray(String[]::new);
        kieFileSystem.delete(fileNames);
    }

    public void cleanAllRules() {
        kieBase.removeKiePackage(PACKAGE_NAME);
        kieFileSystem = null;
        log.info("All rules has been cleaned...");
    }

    public List<String> getRuleNames() {

        KiePackage kpkg = kieBase.getKiePackage(PACKAGE_NAME);
        List<String> ruleNames = null;
        if (kpkg != null) ruleNames = kpkg.getRules().stream().map(Rule::getName).collect(Collectors.toList());
        return ruleNames;
    }

    /**
     * @apiNote As of now we will execute order object only
     * later when needed will implement per object - kie-session design
     **/
    public void execute(Order order) {
        KieSession ksession = null;
        try {
            ksession = kieBase.newKieSession();
            if (!getRuleNames().isEmpty()) {
                if (order != null) {
                    ksession.insert(order);
                    int rulesFired = ksession.fireAllRules();
                    log.debug("Number of rules fired on event: " + rulesFired);
                    log.debug("rule triggered {} rules: {}", rulesFired, order);
                    return;
                }
                log.error("Object is null");
            } else {
                log.error("No rules in the system - skipping firing rules");
                throw new RuntimeException("No rules present in the system");
            }

        } catch (Exception e) {
            log.error("Error during rules engine processing:  " + e.getMessage());
        } finally {
            if (ksession != null) {
                ksession.dispose();
                ksession.destroy();
            }
        }
    }

    private String getFileName(String ruleName) {
        String fileName = RESOURCE_FILE_PATH + ruleName + RULE_FILE_EXTENSION;
        log.debug("FileName :::: {}, RuleName :::: {}", fileName, ruleName);
        return fileName;
    }

    private void loadRule(String drl, String fileName) {
        kieFileSystem.write(fileName, ResourceFactory.newReaderResource(new StringReader(drl)));
    }
}

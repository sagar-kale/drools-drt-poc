package org.drools.engine;


import lombok.extern.slf4j.Slf4j;
import org.drools.model.DroolsProps;
import org.drools.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author - Sagar Kale dt225541
 * @createddate - May 07 2020
 **/

@Component
@EnableScheduling
@Slf4j
public class RulesCache {

    @Autowired
    private RuleEngine engine;
    @Autowired
    private RuleService ruleService;

    @Autowired
    private DroolsProps droolsProps;

    private final ReadWriteLock readWriteLock;

    private final Map<String, Date> rulesBySecurity;

    public RulesCache() {
        this.rulesBySecurity = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public void refreshLastAccessTime(String securityId) {
        rulesBySecurity.put(securityId, new Date());
    }

    private void clearCache(Date time) {

        if (rulesBySecurity.size() == 0) {
            log.debug("No rules present in memory ....");
            return;
        }

        if (rulesBySecurity.size() > droolsProps.getMemLimit()) {
            LinkedList<Map.Entry<String, Date>> entries = new LinkedList<>(rulesBySecurity.entrySet());
            Collections.sort(entries, Comparator.comparing(Map.Entry::getValue));
            List<Map.Entry<String, Date>> toBeRemovedEntries = entries.stream().limit(entries.size() - droolsProps.getMemLimit()).collect(Collectors.toList());
            toBeRemovedEntries.stream().forEach(entry -> {
                Date date = entry.getValue();
                long minutesBetween = ChronoUnit.MINUTES.between(date.toInstant(), time.toInstant());
                String securityId = entry.getKey();

                if (minutesBetween > droolsProps.getRuleHoldDuration()) {
                    log.info("removing an rule from memory as rule in not consumed for {} minutes", minutesBetween);
                    rulesBySecurity.remove(entry.getKey());
                    ruleService.removeRulesFromMemory(securityId);
                }
            });
        }
    }

    @Scheduled(fixedDelayString = "${drools.cache.cleaner.delay}")
    private void ruleCleaner() {
        if (!droolsProps.isCleanerEnabled())
            return;
        log.debug("Starting cache cleaner. Trying to acquire read write lock");
        readWriteLock.writeLock().lock();
        try {
            log.debug("executing scheduled task clear cache...{}", Instant.now());
            clearCache(new Date());
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public void readLock() {
        this.readWriteLock.readLock().lock();
    }

    public void unlockReadLock() {
        this.readWriteLock.readLock().unlock();
    }

    public void writeLock() {
        this.readWriteLock.writeLock().lock();
    }

    public void unlockWriteLock() {
        this.readWriteLock.writeLock().unlock();
    }

    public void emptyCache() {
        if (null != rulesBySecurity)
            rulesBySecurity.clear();
    }

    public boolean isSecurityAvailable(String securityId) {
        return rulesBySecurity.containsKey(securityId);
    }
}



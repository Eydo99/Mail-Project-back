package com.example.backend.StrategyPattern;

import com.example.backend.model.mail;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * sorting strategy hub that runs all the strategies
 * delegates the sorting to one of the sorting classes
 */

@Component
public class EmailSortContext {
    //Map to store available strategies(name,strategy)
    private final Map<String, EmailSortStrategy> strategies;

    //constructor to register our default strategies
    public EmailSortContext() {
        this.strategies = new HashMap<>();
        registerDefaultStrategies();
    }


    //Register all default sorting strategies
    private void registerDefaultStrategies() {
        // Date strategies
        registerStrategy(new SortByDateStrategy(true));   // date-asc
        registerStrategy(new SortByDateStrategy(false));  // date-desc

        // Sender strategies
        registerStrategy(new SortBySenderStrategy(true));  // sender-asc
        registerStrategy(new SortBySenderStrategy(false)); // sender-desc

        // Subject strategies
        registerStrategy(new SortBySubjectStrategy(true));  // subject-asc
        registerStrategy(new SortBySubjectStrategy(false)); // subject-desc

        // Priority strategies
        registerStrategy(new SortByPriorityStrategy(true));  // priority-asc
        registerStrategy(new SortByPriorityStrategy(false)); // priority-desc
    }

    //Register a new sorting strategy
    public void registerStrategy(EmailSortStrategy strategy) {
        strategies.put(strategy.getStrategyName(), strategy);
    }

    /**
      Sort emails using the specified strategy
      @param emails List of emails to sort
      @param strategyName Name of the strategy
      @return Sorted list of emails
     */
    public List<mail> sortEmails(List<mail> emails, String strategyName) {
        //get the strategy to work with
        EmailSortStrategy strategy = strategies.get(strategyName);
        //if null then default sort by descending date
        if (strategy == null) {
            System.err.println("Unknown sort strategy: " + strategyName + ". Using default date-desc");
            strategy = strategies.get("date-desc");
        }
        //apply the sorting strategy on list of mails
        strategy.sort(emails);
        return emails;
    }

    /*
      Get all available sorting strategies
     */
    public Map<String, EmailSortStrategy> getAllStrategies() {
        return new HashMap<>(strategies);
    }
}
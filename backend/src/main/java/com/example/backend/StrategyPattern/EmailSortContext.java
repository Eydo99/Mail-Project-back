package com.example.backend.StrategyPattern;

import com.example.backend.DTOS.mailDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class EmailSortContext {

    private final Map<String, EmailSortStrategy> strategies;

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

    /*
      Sort emails using the specified strategy
      @param emails List of emails to sort
      @param strategyName Name of the strategy (e.g., "date-desc", "sender-asc")
      @return Sorted list of emails
     */
    public List<mailDTO> sortEmails(List<mailDTO> emails, String strategyName) {
        EmailSortStrategy strategy = strategies.get(strategyName);

        if (strategy == null) {
            System.err.println("Unknown sort strategy: " + strategyName + ". Using default date-desc");
            strategy = strategies.get("date-desc");
        }

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
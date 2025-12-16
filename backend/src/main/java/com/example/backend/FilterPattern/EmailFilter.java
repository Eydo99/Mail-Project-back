package com.example.backend.FilterPattern;
import com.example.backend.model.mail;
import java.util.List;

/**
 * Chain of Responsibility Pattern for Email Filtering
 */
public interface EmailFilter {
    List<mail> apply(List<mail> emails);
    void setNext(EmailFilter next);
}
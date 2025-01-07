package fr.cnrs.opentheso.services;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class TranslationService {

    private final MessageSource messageSource;

    public TranslationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }
}
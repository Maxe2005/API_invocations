package com.imt.api_invocations.config;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

/**
 * Configuration pour intégrer Spring avec la validation Jakarta.
 * Permet aux ConstraintValidators d'utiliser l'injection de dépendances Spring.
 */
@Configuration
public class ValidationConfig {

    @Bean
    @Primary
    public LocalValidatorFactoryBean validator(AutowireCapableBeanFactory autowireCapableBeanFactory) {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setConstraintValidatorFactory(
                new SpringConstraintValidatorFactory(autowireCapableBeanFactory));
        return validatorFactoryBean;
    }
}

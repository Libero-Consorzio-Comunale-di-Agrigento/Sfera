package it.finmatica.atti.spring.transaction;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;

/**
 * Questa classe funziona quasi per caso:
 * 1) si affida al fatto che la classe grails ChainedTransactionManagerPostProcessor registri la definizione del bean con la classe ChainedTransactionManager
 * 2) si affida al fatto che la classe grails ChainedTransactionManagerPostProcessor registri la definizione del bean con la classe ChainedTransactionManager con il costruttore con i soli transactionManagers
 * 3) si affida al fatto che la classe grails ChainedTransactionManagerPostProcessor abbia come getOrder() HIGHEST_PRECEDENCE
 *
 * Dati questi assunti, cambia la classe del bean transactionManager e ci mette quella custom con l'event-listener per l'afterCommit di tutte le transazioni.
 *
 * Created by esasdelli on 23/03/2017.
 */
public class ChainedPlatformTransactionManagerPostProcessor implements BeanDefinitionRegistryPostProcessor, Ordered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition beanDefinition = registry.getBeanDefinition("transactionManager");
        if (beanDefinition.getBeanClassName() == org.codehaus.groovy.grails.transaction.ChainedTransactionManager.class.getCanonicalName()) {
            beanDefinition.setBeanClassName(ChainedPlatformTransactionManager.class.getCanonicalName());
        }
    }

    public int getOrder () {
        // eseguo immediatamente dopo il ChainedTransactionManagerPostProcessor
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}

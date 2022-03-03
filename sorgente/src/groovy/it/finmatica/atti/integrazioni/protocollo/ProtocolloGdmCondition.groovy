package it.finmatica.atti.integrazioni.protocollo

import org.apache.log4j.Logger
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class ProtocolloGdmCondition implements Condition {

    private static final Logger log = Logger.getLogger(ProtocolloGdmCondition)
    private static final List<String> CLASSI_JPROTOCOLLO = ["it.finmatica.segreteria.common.ParametriSegreteria",
                                                            "it.finmatica.segreteria.common.struttura.Classificazione",
                                                            "it.finmatica.segreteria.common.struttura.ParametriProtocollazione",
                                                            "it.finmatica.segreteria.common.struttura.Titolario",
                                                            "it.finmatica.segreteria.jprotocollo.struttura.Protocollo",
                                                            "it.finmatica.segreteria.jprotocollo.struttura.Rapporto",
                                                            "it.finmatica.segreteria.wkfSupport.ProtocolloUtil"]

    @Override
    boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        try {
            // tento di caricare le varie classi del protocollo. se le trovo tutte, ok. Altrimenti ritorno false.
            for (String classe : CLASSI_JPROTOCOLLO) {
                conditionContext.classLoader.loadClass(classe)
            }
            return true
        } catch (ClassNotFoundException c) {
            log.warn("Non ho trovato la classe ${c.message}, non istanzio i bean del protocollo Gdm.")
            return false
        }
    }
}

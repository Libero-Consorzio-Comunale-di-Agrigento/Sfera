package it.finmatica.atti.config

import it.finmatica.atti.integrazioni.protocollo.ProtocolloDOCArea
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdm
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmCondition
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGs4
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class ProtocolloEsternoConfig {

    @Conditional(ProtocolloGdmCondition)
    @Bean(name="protocolloEsternoGdmDocArea")
    @Lazy
    ProtocolloDOCArea protocolloEsternoGdmDocArea (ProtocolloGdm protocolloGdm) {
        ProtocolloDOCArea protocolloDOCArea = new ProtocolloDOCArea()
        protocolloDOCArea.protocolloEsterno = protocolloGdm
        return protocolloDOCArea
    }

    @Conditional(ProtocolloGs4.ProtocolloGs4Condition)
    @Bean(name="protocolloEsternoGs4DocArea")
    @Lazy
    ProtocolloDOCArea protocolloEsternoGs4DocArea (ProtocolloGs4 protocolloGs4) {
        ProtocolloDOCArea protocolloDOCArea = new ProtocolloDOCArea()
        protocolloDOCArea.protocolloEsterno = protocolloGs4
        return protocolloDOCArea
    }
}

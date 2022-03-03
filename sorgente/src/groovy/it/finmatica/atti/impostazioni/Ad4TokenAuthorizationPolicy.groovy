package it.finmatica.atti.impostazioni

import it.finmatica.atti.commons.TokenIntegrazioneService
import org.apache.cxf.configuration.security.AuthorizationPolicy

class Ad4TokenAuthorizationPolicy extends AuthorizationPolicy {

    private final TokenIntegrazioneService tokenIntegrazioneService

    Ad4TokenAuthorizationPolicy (TokenIntegrazioneService tokenIntegrazioneService, String nominativoUtente) {
        this.tokenIntegrazioneService = tokenIntegrazioneService
        this.userName = nominativoUtente
        this.authorizationType = "Basic"
    }

    @Override
    String getPassword () {
        return tokenIntegrazioneService.getTokenAutenticazioneAd4(this.getUserName())
    }
}

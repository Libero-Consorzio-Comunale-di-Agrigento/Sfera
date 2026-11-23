package it.finmatica.atti.integrazioni.pnd.classes

import org.apache.log4j.Logger

class PdnException extends Exception{

    private static final Logger log = Logger.getLogger(PdnException.class)

    PdnException(GenericResponse genericResponse){
        //log.debug("Errore nel metodo "+metodo+ " " +genericResponse.status+ ":" +genericResponse.message)
        super(genericResponse.status+ ":" +genericResponse.message)
    }

    PdnException(GenericError genericError){
        //log.debug("Errore nel metodo "+metodo+ " " +genericError.status+ ":" +genericError.message+ ":"+genericError.exception)
        super(genericError.status+ ":" +genericError.message+ ":"+genericError.exception)
    }
}

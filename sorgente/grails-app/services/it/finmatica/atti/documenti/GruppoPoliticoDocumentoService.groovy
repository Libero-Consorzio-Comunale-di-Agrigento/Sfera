package it.finmatica.atti.documenti

import grails.transaction.Transactional

@Transactional
class GruppoPoliticoDocumentoService {

    void elimina (GruppoPoliticoDocumento gruppoPoliticoDocumento, IDocumento documentoPrincipale) {
        if (documentoPrincipale instanceof Delibera){
            gruppoPoliticoDocumento.delibera=null
            gruppoPoliticoDocumento.save()
                    } else if (documentoPrincipale instanceof PropostaDelibera && gruppoPoliticoDocumento.delibera==null){
            gruppoPoliticoDocumento.delete()
        }

    }

    void salva (GruppoPoliticoDocumento gruppoPoliticoDocumento, IDocumento documentoPrincipale){
        if (documentoPrincipale instanceof Delibera){
            gruppoPoliticoDocumento.delibera=documentoPrincipale
            try{
                gruppoPoliticoDocumento.save()
            } catch(Exception ex){
                println ex.message
            }

        } else if (documentoPrincipale instanceof PropostaDelibera ){
            gruppoPoliticoDocumento.propostaDelibera=documentoPrincipale
            try{
                gruppoPoliticoDocumento.save()
            } catch(Exception ex){
                println ex.message
            }

        }
    }
}

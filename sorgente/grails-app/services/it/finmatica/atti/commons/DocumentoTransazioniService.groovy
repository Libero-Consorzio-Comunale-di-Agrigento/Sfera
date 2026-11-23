package it.finmatica.atti.commons

import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DeterminaService
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.TransactionDefinition

class DocumentoTransazioniService {

    PlatformTransactionManager transactionManager
    DeterminaService determinaService

    @Transactional  // La transazione principale
    void numeraDetermina(Long id) {
        assegnaNumeroDetermina(id) // Deve essere committato subito
    }

    //assegna il numero della determina in una transazione separata
    void assegnaNumeroDetermina(Long id) {
        def defTx = new DefaultTransactionDefinition()
        defTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW)

        new TransactionTemplate(transactionManager, defTx).execute { status ->
            try {
                Determina doc = Determina.get(id)
                determinaService.numeraDetermina(doc)
                doc.save(failOnError: true)
            } catch (Exception e) {
                status.setRollbackOnly()
                throw new RuntimeException("Errore durante l'assegnazione del numero: ${e.message}", e)
            }
        }
    }
}
package it.finmatica.atti.spring.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/**
 * Questa classe orribile serve per ovviare al fatto che la ChainedTransactionManager di default di grails opera l'inversione dei
 * transactionManager con il risultato che viene prima fatta commit su GDM, poi le altre connessioni ed infine sulla connessione principale AGSDE2.
 * Noi non vogliamo questo comportamento perché vogliamo che sia la connessione di SFERA ad avere priorità, inoltre, siccome il synchronizationManager è unico
 * per tutte le transazioni, viene invocato sulla commit della prima transazione (cioè quella di gdm) quando ancora i dati non sono stati committati su agsde2.
 * Siccome ci sono dei "listener" che agiscono sulla afterCommit (cioè il NotificheDispatcher per l'invio delle notifiche), questi considerano che la commit sia stata eseguita.
 * <p>
 * Created by esasdelli on 23/03/2017.
 */
public class ChainedPlatformTransactionManager implements PlatformTransactionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainedPlatformTransactionManager.class);
	private final List<PlatformTransactionManager> transactionManagers;

	public ChainedPlatformTransactionManager (PlatformTransactionManager... transactionManagers) {
		this.transactionManagers = new ArrayList<PlatformTransactionManager>();
		this.transactionManagers.addAll(reverse(Arrays.asList(transactionManagers)));
	}

	public MultiTransactionStatus getTransaction (TransactionDefinition definition) throws TransactionException {
		MultiTransactionStatus mts = new MultiTransactionStatus(transactionManagers.get(0));
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.initSynchronization();
			mts.setNewSynchonization();
		}

		try {
			for (PlatformTransactionManager transactionManager : transactionManagers) {
				mts.registerTransactionManager(definition, transactionManager);
			}
		} catch (Exception ex) {
			Map<PlatformTransactionManager, TransactionStatus> transactionStatuses = mts.getTransactionStatuses();
			for (PlatformTransactionManager transactionManager : transactionManagers) {
				try {
					if (transactionStatuses.get(transactionManager) != null) {
						transactionManager.rollback(transactionStatuses.get(transactionManager));
					}
				} catch (Exception ex2) {
					LOGGER.warn("Rollback exception (" + transactionManager + ") " + ex2.getMessage(), ex2);
				}
			}

			if (mts.isNewSynchonization()) {
				TransactionSynchronizationManager.clearSynchronization();
			}
			throw new CannotCreateTransactionException(ex.getMessage(), ex);
		}

		return mts;
	}

	public void commit (TransactionStatus status) throws TransactionException {
		MultiTransactionStatus multiTransactionStatus = (MultiTransactionStatus) status;
		boolean commit = true;
		Exception commitException = null;
		PlatformTransactionManager commitExceptionTransactionManager = null;
		List<TransactionSynchronization> synchronizations = new ArrayList<TransactionSynchronization>();
		if (multiTransactionStatus.isNewSynchonization()) {
			synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronizationManager.clearSynchronization();
			TransactionSynchronizationManager.initSynchronization();
		}

		int i = 1;
		for (PlatformTransactionManager transactionManager : transactionManagers) {
			if (i == transactionManagers.size() && multiTransactionStatus.isNewSynchonization()) {
				for (TransactionSynchronization synchronization : synchronizations) {
					TransactionSynchronizationManager.registerSynchronization(synchronization);
				}
			}
			i++;

			if (commit) {
				try {
					multiTransactionStatus.commit(transactionManager);
				} catch (Exception ex) {
					commit = false;
					commitException = ex;
					commitExceptionTransactionManager = transactionManager;
				}
			} else {
				// after unsucessfull commit we must try to rollback remaining transaction managers
				try {
					multiTransactionStatus.rollback(transactionManager);
				} catch (Exception ex) {
					LOGGER.warn("Rollback exception (after commit) (" + transactionManager + ") " + ex.getMessage(), ex);
				}
			}
		}

		if (multiTransactionStatus.isNewSynchonization()) {
			TransactionSynchronizationManager.clearSynchronization();
		}

		if (commitException != null) {
			boolean firstTransactionManagerFailed = commitExceptionTransactionManager.equals(getLastTransactionManager());
			int transactionState = firstTransactionManagerFailed ? HeuristicCompletionException.STATE_ROLLED_BACK : HeuristicCompletionException.STATE_MIXED;
			throw new HeuristicCompletionException(transactionState, commitException);
		}
	}

	public void rollback (TransactionStatus status) throws TransactionException {
		Exception rollbackException = null;
		PlatformTransactionManager rollbackExceptionTransactionManager = null;
		MultiTransactionStatus multiTransactionStatus = (MultiTransactionStatus) status;
		List<TransactionSynchronization> synchronizations = new ArrayList<TransactionSynchronization>();
		if (multiTransactionStatus.isNewSynchonization()) {
			synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronizationManager.clearSynchronization();
			TransactionSynchronizationManager.initSynchronization();
		}

		int i = 1;
		for (PlatformTransactionManager transactionManager : transactionManagers) {
			if (i == transactionManagers.size() && multiTransactionStatus.isNewSynchonization()) {
				for (TransactionSynchronization synchronization : synchronizations) {
					TransactionSynchronizationManager.registerSynchronization(synchronization);
				}
			}
			i++;

			try {
				multiTransactionStatus.rollback(transactionManager);
			} catch (Exception ex) {
				if (rollbackException == null) {
					rollbackException = ex;
					rollbackExceptionTransactionManager = transactionManager;
				} else {
					LOGGER.warn("Rollback exception (" + transactionManager + ") " + ex.getMessage(), ex);
				}
			}
		}

		if (multiTransactionStatus.isNewSynchonization()) {
			TransactionSynchronizationManager.clearSynchronization();
		}

		if (rollbackException != null) {
			throw new UnexpectedRollbackException("Rollback exception, originated at (" + rollbackExceptionTransactionManager + ") " + rollbackException.getMessage(), rollbackException);
		}
	}

	private <T> List<T> reverse (List<T> list) {
		Collections.reverse(list);
		return list;
	}

	private PlatformTransactionManager getLastTransactionManager () {
		return transactionManagers.get(lastTransactionManagerIndex());
	}

	private int lastTransactionManagerIndex () {
		return transactionManagers.size() - 1;
	}

	public List<PlatformTransactionManager> getTransactionManagers () {
		return transactionManagers;
	}
}

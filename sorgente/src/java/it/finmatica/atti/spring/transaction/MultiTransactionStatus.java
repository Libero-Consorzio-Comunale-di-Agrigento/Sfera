package it.finmatica.atti.spring.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by esasdelli on 23/03/2017.
 */
public class MultiTransactionStatus implements TransactionStatus {
    /**
     * Creates a new {@link MultiTransactionStatus} for the given {@link PlatformTransactionManager}.
     *
     * @param mainTransactionManager must not be {@literal null}.
     */
    public MultiTransactionStatus (PlatformTransactionManager mainTransactionManager) {

        Assert.notNull(mainTransactionManager, "TransactionManager must not be null!");
        this.mainTransactionManager = mainTransactionManager;
    }

    public Map<PlatformTransactionManager, TransactionStatus> getTransactionStatuses () {
        return transactionStatuses;
    }

    public void setNewSynchonization () {
        this.newSynchonization = true;
    }

    public boolean isNewSynchonization () {
        return newSynchonization;
    }

    public void registerTransactionManager (TransactionDefinition definition, PlatformTransactionManager transactionManager) {
        getTransactionStatuses().put(transactionManager, transactionManager.getTransaction(definition));
    }

    public void commit (PlatformTransactionManager transactionManager) {
        TransactionStatus transactionStatus = getTransactionStatusInternal(transactionManager);
        transactionManager.commit(transactionStatus);
    }

    public TransactionStatus getTransactionStatus(PlatformTransactionManager transactionManager) {
        return transactionStatuses.get(transactionManager);
    }

    /**
     * Rolls back the {@link TransactionStatus} registered for the given {@link PlatformTransactionManager}.
     *
     * @param transactionManager must not be {@literal null}.
     */
    public void rollback (PlatformTransactionManager transactionManager) {
        transactionManager.rollback(getTransactionStatusInternal(transactionManager));
    }

    public boolean isRollbackOnly () {
        return getMainTransactionStatus().isRollbackOnly();
    }

    public boolean isCompleted () {
        return getMainTransactionStatus().isCompleted();
    }

    public boolean isNewTransaction () {
        return getMainTransactionStatus().isNewTransaction();
    }

    public boolean hasSavepoint () {
        return getMainTransactionStatus().hasSavepoint();
    }

    public void setRollbackOnly () {
        for (TransactionStatus ts : transactionStatuses.values()) {
            ts.setRollbackOnly();
        }
    }

    public Object createSavepoint () throws TransactionException {

        SavePoints savePoints = new SavePoints();

        for (TransactionStatus transactionStatus : transactionStatuses.values()) {
            savePoints.save(transactionStatus);
        }

        return savePoints;
    }

    public void rollbackToSavepoint (Object savepoint) throws TransactionException {
        SavePoints savePoints = (SavePoints) savepoint;
        savePoints.rollback();
    }

    public void releaseSavepoint (Object savepoint) throws TransactionException {
        ((SavePoints) savepoint).release();
    }

    public void flush () {
        for (TransactionStatus transactionStatus : transactionStatuses.values()) {
            transactionStatus.flush();
        }
    }

    private TransactionStatus getMainTransactionStatus () {
        return transactionStatuses.get(mainTransactionManager);
    }

    private TransactionStatus getTransactionStatusInternal (PlatformTransactionManager transactionManager) {
        return this.getTransactionStatuses().get(transactionManager);
    }

    private final PlatformTransactionManager mainTransactionManager;
    private final Map<PlatformTransactionManager, TransactionStatus> transactionStatuses = Collections.synchronizedMap(new HashMap<PlatformTransactionManager, TransactionStatus>());
    private boolean newSynchonization;

    private static class SavePoints {
        private void addSavePoint (TransactionStatus status, Object savepoint) {

            Assert.notNull(status, "TransactionStatus must not be null!");
            this.savepoints.put(status, savepoint);
        }

        private void save (TransactionStatus transactionStatus) {
            Object savepoint = transactionStatus.createSavepoint();
            addSavePoint(transactionStatus, savepoint);
        }

        public void rollback () {
            for (TransactionStatus transactionStatus : savepoints.keySet()) {
                transactionStatus.rollbackToSavepoint(savepointFor(transactionStatus));
            }

        }

        private Object savepointFor (TransactionStatus transactionStatus) {
            return savepoints.get(transactionStatus);
        }

        public void release () {
            for (TransactionStatus transactionStatus : savepoints.keySet()) {
                transactionStatus.releaseSavepoint(savepointFor(transactionStatus));
            }

        }

        private final Map<TransactionStatus, Object> savepoints = new HashMap<TransactionStatus, Object>();
    }
}

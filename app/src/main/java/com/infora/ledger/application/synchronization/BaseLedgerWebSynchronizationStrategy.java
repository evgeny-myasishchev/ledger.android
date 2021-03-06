package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.util.Log;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.support.LogUtil;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by mye on 9/24/2015.
 */
public abstract class BaseLedgerWebSynchronizationStrategy implements SynchronizationStrategy {
    private static final String TAG = BaseLedgerWebSynchronizationStrategy.class.getName();
    protected LedgerApiFactory apiFactory;

    public BaseLedgerWebSynchronizationStrategy(LedgerApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    protected LedgerApi createApi(Account account, SyncResult syncResult) {
        LedgerApi api;
        try {
            api = apiFactory.createApi(account);
        } catch (RetrofitError error) {
            syncResult.stats.numAuthExceptions++;
            throw error;
        }
        return api;
    }

    protected static void publishPendingTransaction(PendingTransaction localTran, SyncResult syncResult, LedgerApi api, EventBus bus) throws SynchronizationException {
        Log.d(TAG, "Publishing pending transaction: " + localTran.transactionId);
        try {
            api.reportPendingTransaction(localTran.transactionId, localTran.amount, localTran.timestamp, localTran.comment, localTran.accountId, localTran.typeId);
        } catch (RetrofitError ex) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to report pending transaction.", ex);
        }
        bus.post(new MarkTransactionAsPublishedCommand(localTran.id));
    }

    protected static void adjustPendingTransaction(LedgerApi api, PendingTransaction localTran, SyncResult syncResult) throws SynchronizationException {
        Log.d(TAG, "Local transaction id='" + localTran.id + "' has been changed. Adjusting remote.");
        try {
            api.adjustPendingTransaction(localTran.transactionId, localTran.amount, localTran.comment, localTran.accountId);
        } catch (RetrofitError ex) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to adjust pending transaction.", ex);
        }
    }

    protected static void rejectPendingTransaction(LedgerApi api, String transactionId, SyncResult syncResult) throws SynchronizationException {
        Log.d(TAG, "Local transaction '" + transactionId + "' removed. Rejecting remote.");
        try {
            api.rejectPendingTransaction(transactionId);
        } catch (RetrofitError ex) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to reject pending transaction." + ex);
        }
    }
}

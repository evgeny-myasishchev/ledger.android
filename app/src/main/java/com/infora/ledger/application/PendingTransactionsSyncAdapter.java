package com.infora.ledger.application;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.SettingsFragment;
import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.ApiAuthenticator;
import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.SharedPreferencesUtil;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Created by jenya on 13.03.15.
 */
public class PendingTransactionsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = PendingTransactionsSyncAdapter.class.getName();

    private ContentResolver resolver;
    private SynchronizationStrategy syncStrategy;
    private ApiAdapter apiAdapter;

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        onInit(context);
    }

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        onInit(context);
    }

    public SynchronizationStrategy getSyncStrategy() {
        return syncStrategy;
    }

    public void setSyncStrategy(SynchronizationStrategy syncStrategy) {
        this.syncStrategy = syncStrategy;
    }

    public ApiAdapter getApiAdapter() {
        return apiAdapter;
    }

    public void setApiAdapter(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    private void onInit(Context context) {
        Log.d(TAG, "Initializing sync adapter...");
        resolver = context.getContentResolver();
        LedgerApplication app = (LedgerApplication) context.getApplicationContext();
        syncStrategy = new FullSyncSynchronizationStrategy(app.getBus());
        SharedPreferences prefs = SharedPreferencesUtil.getDefaultSharedPreferences(context);
        String ledgerHost = prefs.getString(SettingsFragment.KEY_LEDGER_HOST, null);
        Log.d(TAG, "Using ledger host: " + ledgerHost);
        apiAdapter = new ApiAdapter(new AccountManagerWrapper(context), ledgerHost);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");
        LedgerApi api = apiAdapter.createApi();
        try {
            apiAdapter.authenticateApi(api, account);
        } catch (RetrofitError error) {
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "Authentication failed. Synchronization aborted.");
            return;
        }
        try {
            syncStrategy.synchronize(api, resolver, extras, syncResult);
        } catch (RetrofitError error) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "Synchronization aborted due to some network error.", error);
            return;
        }
        Log.i(TAG, "Synchronization completed.");
    }

    public static class PendingTransactionsSyncService extends Service {

        private static final Object initLock = new Object();
        private PendingTransactionsSyncAdapter syncAdapter;

        @Override
        public void onCreate() {
            synchronized (initLock) {
                if (syncAdapter == null)
                    syncAdapter = new PendingTransactionsSyncAdapter(this, true);
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return syncAdapter.getSyncAdapterBinder();
        }
    }
}

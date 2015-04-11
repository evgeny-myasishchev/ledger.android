package com.infora.ledger;

import android.accounts.Account;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.infora.ledger.application.commands.CreateSystemAccountCommand;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.SharedPreferencesUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class LedgerApplication extends Application {
    private static final String TAG = LedgerApplication.class.getName();
    public static final String PACKAGE = "com.infora.ledger";
    public static final String AUTH_TOKEN_TYPE = PACKAGE;
    public static final String ACCOUNT_TYPE = "ledger.infora-soft.com";
    private EventBus bus;

    private AccountManagerWrapper accountManager;

    public AccountManagerWrapper getAccountManager() {
        return accountManager == null ? (accountManager = new AccountManagerWrapper(this)) : accountManager;
    }

    public void setAccountManager(AccountManagerWrapper accountManager) {
        this.accountManager = accountManager;
    }

    public EventBus getBus() {
        return bus;
    }

    public void setBus(EventBus bus) {
        this.bus = bus;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
        bus = new EventBus();
        bus.register(this);

        PendingTransactionsService pendingTransactionsService = new PendingTransactionsService(getContentResolver(), bus);
        bus.register(pendingTransactionsService);

        registerActivityLifecycleCallbacks(new GlobalActivityLifecycleCallbacks(this));

        PreferenceManager.setDefaultValues(this, R.xml.app_prefs, false);
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getDefaultSharedPreferences(this);
        if(!sharedPreferences.contains(SettingsFragment.KEY_LEDGER_HOST)) {
            Log.d(TAG, "Ledger host preference not yet initialized. Assigning default value: " + BuildConfig.DEFAULT_LEDGER_HOST);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(SettingsFragment.KEY_LEDGER_HOST, BuildConfig.DEFAULT_LEDGER_HOST);
            edit.apply();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }

    public void onEvent(CreateSystemAccountCommand cmd) {
        Log.i(TAG, "Adding new account: " + cmd.getEmail());
        Account account = new Account(cmd.getEmail(), ACCOUNT_TYPE);
        getAccountManager().addAccountExplicitly(account, null);
    }
}
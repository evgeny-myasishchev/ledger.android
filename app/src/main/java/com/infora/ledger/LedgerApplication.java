package com.infora.ledger;

import android.accounts.Account;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.infora.ledger.application.BankLinksService;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.commands.CreateSystemAccountCommand;
import com.infora.ledger.application.di.ApplicationModule;
import com.infora.ledger.application.di.DaggerApplicationComponent;
import com.infora.ledger.application.di.DependenciesInjector;
import com.infora.ledger.application.di.InjectorProvider;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.SharedPreferencesUtil;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class LedgerApplication extends Application implements InjectorProvider<DependenciesInjector> {
    private static final String TAG = LedgerApplication.class.getName();
    public static final String PACKAGE = "com.infora.ledger";
    public static final String AUTH_TOKEN_TYPE = PACKAGE;
    public static final String ACCOUNT_TYPE = "ledger.infora-soft.com";

    private DependenciesInjector injector;

    @Inject EventBus bus;
    @Inject DatabaseContext databaseContext;
    @Inject AccountManagerWrapper accountManager;
    @Inject PendingTransactionsService pendingTransactionsService;
    @Inject BankLinksService bankLinksService;

    //TODO: Remove
    public EventBus getBus() {
        return bus;
    }

    //TODO: Remove
    public LedgerApplication setBus(EventBus bus) {
        this.bus = bus;
        return this;
    }

    public DependenciesInjector injector() {
        if (injector == null) {
            Log.d(TAG, "Initializing application injector...");
            injector = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return injector;
    }

    public DatabaseContext getDatabaseContext() {
        return databaseContext;
    }

    public void onCreate() {
        Log.d(TAG, "Initializing application...");
        injector().inject(this);

        bus.register(this);
        bus.register(pendingTransactionsService);
        bus.register(bankLinksService);

        registerActivityLifecycleCallbacks(new GlobalActivityLifecycleCallbacks(this));

        PreferenceManager.setDefaultValues(this, R.xml.app_prefs, false);
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(SettingsFragment.KEY_LEDGER_HOST)) {
            Log.d(TAG, "Ledger host preference not yet initialized. Assigning default value: " + BuildConfig.DEFAULT_LEDGER_HOST);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(SettingsFragment.KEY_LEDGER_HOST, BuildConfig.DEFAULT_LEDGER_HOST);
            edit.apply();
        }

        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }

    public void onEvent(CreateSystemAccountCommand cmd) {
        Log.i(TAG, "Adding new account: " + cmd.getEmail());
        Account account = new Account(cmd.getEmail(), ACCOUNT_TYPE);
        accountManager.addAccountExplicitly(account, null);
    }
}

package com.infora.ledger.application;

import android.accounts.Account;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.support.AccountManagerWrapper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by mye on 7/17/2015.
 */
@Singleton public class DeviceSecretProvider {
    private static final String TAG = DeviceSecretProvider.class.getName();
    private final Context context;
    private final AccountManagerWrapper accountManager;
    private LedgerApiFactory ledgerApiFactory;
    private DeviceSecret deviceSecret;

    @Inject
    public DeviceSecretProvider(Context context, AccountManagerWrapper accountManager, LedgerApiFactory ledgerApiFactory) {
        this.context = context;
        this.accountManager = accountManager;
        this.ledgerApiFactory = ledgerApiFactory;
        Log.d(TAG, "Provider instance created. Hash code: " + hashCode());
    }

    public boolean hasBeenRegistered() {
        return deviceSecret != null;
    }

    public DeviceSecretProvider ensureDeviceRegistered() {
        if (hasBeenRegistered()) return null;
        Log.d(TAG, "Registering device...");
        Account[] accounts = accountManager.getApplicationAccounts();
        if (accounts.length == 0) {
            Log.w(TAG, "There are no accounts yet. Device registration skipped.");
            return null;
        }
        LedgerApi api = ledgerApiFactory.createApi(accounts[0]);
        deviceSecret = api.registerDevice(getDeviceId(context), Build.MODEL);
        Log.d(TAG, "Device registered. Secret retrieved.");
        return this;
    }

    public DeviceSecret secret() {
        if (hasBeenRegistered()) return deviceSecret;
        throw new RuntimeException("The secret has not been loaded yet.");
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}

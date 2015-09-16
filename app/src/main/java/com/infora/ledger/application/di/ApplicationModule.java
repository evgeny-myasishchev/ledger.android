package com.infora.ledger.application.di;

import android.app.Application;
import android.content.Context;

import com.infora.ledger.application.BankLinksService;
import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.ui.BankLinkFragmentsFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/15/2015.
 */
@Module
public class ApplicationModule {
    private Application app;

    public ApplicationModule(Application app) {
        this.app = app;
    }

    @Provides @Singleton Context provideAppContext() {
        return app;
    }

    @Provides @Singleton EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides @Singleton
    PendingTransactionsService providePendingTransactionsService(Context context, EventBus bus) {
        return new PendingTransactionsService(context, bus);
    }

    @Provides @Singleton
    BankLinksService provideBankLinksService(EventBus bus, DatabaseContext db, DeviceSecretProvider secretProvider) {
        return new BankLinksService(bus, db, secretProvider);
    }

    @Provides BankLinkFragmentsFactory provideBankLinkFragmentsFactory() {
        return BankLinkFragmentsFactory.createDefault();
    }
}

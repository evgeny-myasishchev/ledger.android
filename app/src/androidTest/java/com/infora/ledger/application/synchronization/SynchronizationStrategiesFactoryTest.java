package com.infora.ledger.application.synchronization;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSharedPrefsProvider;
import com.infora.ledger.mocks.di.TestApplicationModule;

import javax.inject.Inject;

/**
 * Created by mye on 9/23/2015.
 */
public class SynchronizationStrategiesFactoryTest extends AndroidTestCase {
    @Inject SynchronizationStrategiesFactory subject;
    private MockSharedPrefsProvider sharedPrefsProvider;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sharedPrefsProvider = new MockSharedPrefsProvider();
        MockLedgerApplication app = new MockLedgerApplication(getContext())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.sharedPrefsProvider = sharedPrefsProvider;
                    }
                });
        app.injector().inject(this);
    }

    public void testCreateStrategyNoSpecialOptions() {
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), new Bundle());
        assertEquals(CompositeSynchronizationStrategy.class, strategy.getClass());
        CompositeSynchronizationStrategy compositeStrategy = (CompositeSynchronizationStrategy) strategy;
        assertEquals(2, compositeStrategy.strategies.length);
        assertEquals(FetchBankLinksSynchronizationStrategy.class, compositeStrategy.strategies[0].getClass());
        assertEquals(LedgerWebSynchronizationStrategy.class, compositeStrategy.strategies[1].getClass());
    }

    public void testCreateStrategyNoSpecialOptionsWithManualBankLinksFetch() {
        sharedPrefsProvider.manuallyFetchBankLinks = true;

        SynchronizationStrategy strategy = subject.createStrategy(getContext(), new Bundle());
        assertEquals(LedgerWebSynchronizationStrategy.class, strategy.getClass());
    }

    public void testCreateLedgerWebStrategy() {
        Bundle options = new Bundle();
        options.putBoolean(SynchronizationStrategiesFactory.OPTION_SYNCHRONIZE_LEDGER_WEB, true);
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), options);
        assertEquals(LedgerWebSynchronizationStrategy.class, strategy.getClass());
    }

    public void testCreateLedgerWebPublishReportedStrategy() {
        Bundle options = new Bundle();
        options.putInt(SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION, 100);
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), options);
        assertEquals(LedgerWebSingleTransactionSyncStrategy.class, strategy.getClass());
    }

    public void testCreateFetchBankLinksStrategy() {
        Bundle options = new Bundle();
        options.putBoolean(SynchronizationStrategiesFactory.OPTION_FETCH_BANK_LINKS, true);
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), options);
        assertEquals(FetchBankLinksSynchronizationStrategy.class, strategy.getClass());
    }
}
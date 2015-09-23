package com.infora.ledger.application.synchronization;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.di.TestApplicationModule;

import javax.inject.Inject;

/**
 * Created by mye on 9/23/2015.
 */
public class SynchronizationStrategiesFactoryTest extends AndroidTestCase {
    @Inject SynchronizationStrategiesFactory subject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockLedgerApplication app = new MockLedgerApplication(getContext())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {

                    }
                });
        app.injector().inject(this);
    }

    public void testCreateStrategyNoSpecialOptions() {
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), new Bundle());
        assertTrue(strategy instanceof CompositeSynchronizationStrategy);
        CompositeSynchronizationStrategy compositeStrategy = (CompositeSynchronizationStrategy) strategy;
        assertEquals(2, compositeStrategy.strategies.length);
        assertTrue(compositeStrategy.strategies[0] instanceof FetchBankLinksSynchronizationStrategy);
        assertTrue(compositeStrategy.strategies[1] instanceof LedgerWebSynchronizationStrategy);
    }

    public void testCreateLedgerWebStrategy() {
        Bundle options = new Bundle();
        options.putBoolean(SynchronizationStrategiesFactory.OPTION_SYNCHRONIZE_LEDGER_WEB, true);
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), options);
        assertTrue(strategy instanceof LedgerWebSynchronizationStrategy);
    }

    public void testCreateFetchBankLinksStrategy() {
        Bundle options = new Bundle();
        options.putBoolean(SynchronizationStrategiesFactory.OPTION_FETCH_BANK_LINKS, true);
        SynchronizationStrategy strategy = subject.createStrategy(getContext(), options);
        assertTrue(strategy instanceof FetchBankLinksSynchronizationStrategy);
    }
}
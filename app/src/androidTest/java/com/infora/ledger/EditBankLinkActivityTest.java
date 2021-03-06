package com.infora.ledger;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.data.LedgerDbHelper;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockBankLinkData;
import com.infora.ledger.mocks.MockBankLinkFragment;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApiFactory;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.mocks.di.TestDependenciesInjector;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.LogUtil;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.BankLinkFragmentsFactory;
import com.infora.ledger.ui.DatePickerFragment;

import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class EditBankLinkActivityTest extends android.test.ActivityUnitTestCase<EditBankLinkActivity> {

    @Inject EventBus bus;
    private MockDatabaseRepository mockBankLinksRepo;
    private BankLink bankLink;
    private LedgerAccountDto selectedAccount;
    @Inject BankLinkFragmentsFactory fragmentsFactory;
    private DeviceSecret secret;
    private TestDependenciesInjector injector;

    public EditBankLinkActivityTest() {
        super(EditBankLinkActivity.class);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Context baseContext = getInstrumentation().getTargetContext();
        secret = DeviceSecret.generateNew();

        MockBankLinkData bic1Data = new MockBankLinkData("login-1", "password-1");

        Instrumentation instrumentation = new Instrumentation() {
            @Override
            public Context getTargetContext() {
                return baseContext;
            }
        };
        injectInstrumentation(instrumentation);
        bus = new EventBus();
        final MockLedgerApplication app = new MockLedgerApplication(baseContext)
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.ledgerAccountsLoaderFactory = createAccountsLoaderFactory();
                        module.deviceSecretProvider = new MockDeviceSecretProvider(secret);
                        module.bankLinkFragmentsFactory = new BankLinkFragmentsFactory();
                    }
                });
        injector = app.injector();
        injector.inject(this);
        selectedAccount = new LedgerAccountDto("account-1", "Account 1");
        bankLink = new BankLink()
                .setId(332)
                .setBic("bic-1")
                .setAccountId(selectedAccount.id)
                .setLinkData(bic1Data, secret);
        mockBankLinksRepo = new MockDatabaseRepository(BankLink.class);
        mockBankLinksRepo.entitiesToGetById.add(bankLink);

        MockBankLinkFragment.registerMockFragment(fragmentsFactory, "bic-1", null, secret);
        MockBankLinkFragment.registerMockFragment(fragmentsFactory, "bic-2", null, secret);
        MockBankLinkFragment.registerMockFragment(fragmentsFactory, "bic-3", null, secret);

        Intent intent = new Intent();
        intent.putExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, (long) bankLink.id);
        setActivityContext(app);
        startActivity(intent, null, null);
        getActivity().setBankLinksRepo(mockBankLinksRepo);
    }

    public void testLoadBankLinkAndAssignFragment() {
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> loadedSubscriber = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        bus.register(loadedSubscriber);
        getInstrumentation().callActivityOnStart(getActivity());
        loadedSubscriber.await();
        getActivity().getSupportFragmentManager().executePendingTransactions();

        MockBankLinkFragment bankLinkFragment = (MockBankLinkFragment) getActivity()
                .getSupportFragmentManager().findFragmentByTag(EditBankLinkActivity.BANK_LINK_FRAGMENT);
        assertTrue(bankLinkFragment.beforeAddCalled);
        assertEquals(BankLinkFragment.Mode.Edit, bankLinkFragment.getMode());
        MockBankLinkData bankLinkData = bankLinkFragment.getBankLinkData();
        assertEquals(bankLink.getLinkData(MockBankLinkData.class, secret), bankLinkData);
        assertEquals(bankLink.bic, ((EditText) getActivity().findViewById(R.id.bic)).getText().toString());
    }

    public void testLoadAndSelectAccount() {
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> bankLinkBarrier = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        BarrierSubscriber<EditBankLinkActivity.AccountsLoaded> accountsBarrier = new BarrierSubscriber<>(EditBankLinkActivity.AccountsLoaded.class);
        bus.register(bankLinkBarrier);
        bus.register(accountsBarrier);
        LogUtil.d(this, "Calling activity onStart");
        getInstrumentation().callActivityOnStart(getActivity());
        accountsBarrier.await();
        bankLinkBarrier.await();

        Spinner accountsSpinner = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        assertEquals(3, accountsSpinner.getAdapter().getCount());
        Cursor selectedItem = (Cursor) accountsSpinner.getSelectedItem();
        assertNotNull(selectedItem);
        assertEquals(selectedAccount.id, selectedItem.getString(selectedItem.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID)));
    }

    public void testUpdateBankLink() {
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> bankLinkBarrier = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        bus.register(bankLinkBarrier);
        LogUtil.d(this, "Calling activity onStart");
        getInstrumentation().callActivityOnStart(getActivity());
        bankLinkBarrier.await();
        getActivity().getSupportFragmentManager().executePendingTransactions();

        MockBankLinkFragment bankLinkFragment = (MockBankLinkFragment) getActivity()
                .getSupportFragmentManager().findFragmentByTag(EditBankLinkActivity.BANK_LINK_FRAGMENT);

        MockBankLinkData newLinkData = new MockBankLinkData("new-login-100", "new-password-100");
        bankLinkFragment.setBankLinkData(new BankLink().setLinkData(newLinkData, secret), secret);
        Spinner accountsSpinner = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        accountsSpinner.setSelection(2);

        MockSubscriber<UpdateBankLinkCommand> commandSubscriber = new MockSubscriber<>(UpdateBankLinkCommand.class);
        bus.register(commandSubscriber);

        getActivity().updateBankLink(null);

        assertEquals(1, commandSubscriber.getEvents().size());
        UpdateBankLinkCommand command = commandSubscriber.getEvent();
        assertEquals("a-200", command.accountId);
        assertEquals("A 200", command.accountName);
        assertEquals(newLinkData, command.bankLinkData);
        assertNull(command.fetchStartingFrom);

        Button updateButton = (Button) getActivity().findViewById(R.id.action_update_bank_link);
        assertFalse(updateButton.isEnabled());
    }

    public void testUpdateBankLinkWithChangedFetchFromDate() {
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> bankLinkBarrier = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        bus.register(bankLinkBarrier);
        LogUtil.d(this, "Calling activity onStart");
        getInstrumentation().callActivityOnStart(getActivity());
        bankLinkBarrier.await();

        MockSubscriber<UpdateBankLinkCommand> commandSubscriber = new MockSubscriber<>(UpdateBankLinkCommand.class);
        bus.register(commandSubscriber);
        Date fetchFrom = Dates.startOfDay(TestHelper.randomDate());
        LogUtil.d(this, "Raising DateChanged event to assign fetchFrom to: " + fetchFrom);
        getActivity().onEvent(new DatePickerFragment.DateChanged(fetchFrom));

        getActivity().updateBankLink(null);

        assertEquals(1, commandSubscriber.getEvents().size());
        UpdateBankLinkCommand command = commandSubscriber.getEvent();
        assertEquals(LedgerDbHelper.toISO8601(fetchFrom), LedgerDbHelper.toISO8601(command.fetchStartingFrom));
    }

    public void testBankLinkUpdated() {
        Button updateButton = (Button) getActivity().findViewById(R.id.action_update_bank_link);
        updateButton.setEnabled(false);
        getActivity().onEventMainThread(new BankLinkUpdated(bankLink.id));
        assertTrue(updateButton.isEnabled());
    }

    public void testUpdateBankLinkFailed() {
        Button updateButton = (Button) getActivity().findViewById(R.id.action_update_bank_link);
        updateButton.setEnabled(false);
        getActivity().onEventMainThread(new UpdateBankLinkFailed(bankLink.id, new Exception()));
        assertTrue(updateButton.isEnabled());
    }

    private LedgerAccountsLoader.Factory createAccountsLoaderFactory() {
        return new LedgerAccountsLoader.Factory() {
            @Override
            public LedgerAccountsLoader createLoader(Context context) {
                MockLedgerApi mockApi = new MockLedgerApi();
                mockApi.setAccounts(Arrays.asList(
                        new LedgerAccountDto("a-100", "A 100"),
                        selectedAccount,
                        new LedgerAccountDto("a-200", "A 200")
                ));
                return new LedgerAccountsLoader(context, new MockLedgerApiFactory(mockApi));
            }
        };
    }
}
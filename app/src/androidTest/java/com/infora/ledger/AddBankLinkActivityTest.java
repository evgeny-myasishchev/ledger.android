package com.infora.ledger;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.mocks.MockBankLinkData;
import com.infora.ledger.mocks.MockBankLinkFragment;
import com.infora.ledger.mocks.MockLedgerAccountsLoader;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.support.LogUtil;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.BankLinkFragmentsFactory;
import com.infora.ledger.ui.DatePickerFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkActivityTest extends android.test.ActivityUnitTestCase<AddBankLinkActivity> {

    @Inject EventBus bus;
    @Inject BankLinkFragmentsFactory fragmentsFactory;
    private DeviceSecret secret;

    public AddBankLinkActivityTest() {
        super(AddBankLinkActivity.class);
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
                        module.bankLinkFragmentsFactory = new BankLinkFragmentsFactory();
                    }
                });
        app.injector().inject(this);

        MockBankLinkFragment.registerMockFragment(fragmentsFactory, "bic-1", new BankLink().setLinkData(new MockBankLinkData("login-1", "password-1"), secret), secret);
        MockBankLinkFragment.registerMockFragment(fragmentsFactory, "bic-2", new BankLink().setLinkData(new MockBankLinkData("login-2", "password-2"), secret), secret);
        MockBankLinkFragment.registerMockFragment(fragmentsFactory, "bic-3", new BankLink().setLinkData(new MockBankLinkData("login-3", "password-3"), secret), secret);

        setActivityContext(app);
        startActivity(new Intent(), null, null);
    }

    public void testInitialFetchDateIsSetToNow() {
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        assertEquals(dateOnlyFormat.format(now), dateOnlyFormat.format(getActivity().getInitialFetchDate()));
    }

    public void testAddBankLinkNoBankSelected() {
        Spinner bic = (Spinner) getActivity().findViewById(R.id.bic);
        bic.setSelection(0);

        MockSubscriber<AddBankLinkCommand> addHandler = new MockSubscriber<>(AddBankLinkCommand.class);
        bus.register(addHandler);

        getActivity().addBankLink(null);

        assertEquals(0, addHandler.getEvents().size());
    }

    public void testAddBankLinkNoAccountSelected() {
        Spinner bic = (Spinner) getActivity().findViewById(R.id.bic);
        bic.setSelection(1);

        Spinner ledgerAccountId = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        populateLedgerAccounts(ledgerAccountId, new LedgerAccountDto(null, "Please select..."), new LedgerAccountDto("a-1", "Account 1"), new LedgerAccountDto("a-2", "Account 2"));

        MockSubscriber<AddBankLinkCommand> addHandler = new MockSubscriber<>(AddBankLinkCommand.class);
        bus.register(addHandler);

        getActivity().addBankLink(null);

        assertEquals(0, addHandler.getEvents().size());
    }

    public void testPopulateBankLinks() {
        Spinner bic = (Spinner) getActivity().findViewById(R.id.bic);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) bic.getAdapter();
        Set<String> knownBics = fragmentsFactory.knownBics();
        assertEquals(knownBics.size() + 1, adapter.getCount());
        assertEquals("", adapter.getItem(0));
        for (int i = 1; i < adapter.getCount(); i++) {
            assertTrue(knownBics.contains(adapter.getItem(i)));
        }
    }

    public void testSelectBankLinkShouldInjectCorrespondingFragment() {
        Spinner bic = (Spinner) getActivity().findViewById(R.id.bic);
        bic.setSelection(1);
        bic.getOnItemSelectedListener().onItemSelected(null, null, 1, 1);
        LogUtil.d(this, "BIC item selection set to 1.");
        String selectedBic = (String) bic.getAdapter().getItem(1);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.executePendingTransactions();
        MockBankLinkFragment fragment1 = (MockBankLinkFragment) fragmentManager.findFragmentByTag("bank-link-fragment");
        assertEquals(fragmentsFactory.get(selectedBic).getBankLinkData(), fragment1.getBankLinkData());
        assertTrue(fragment1.beforeAddCalled);
        assertEquals(BankLinkFragment.Mode.Add, fragment1.getMode());

        bic.setSelection(2);
        bic.getOnItemSelectedListener().onItemSelected(null, null, 2, 2);
        fragmentManager.executePendingTransactions();
        LogUtil.d(this, "BIC item selection set to 2.");
        assertTrue(fragment1.beforeRemoveCalled);
        selectedBic = (String) bic.getAdapter().getItem(2);
        MockBankLinkFragment fragment2 = (MockBankLinkFragment) fragmentManager.findFragmentByTag("bank-link-fragment");
        assertEquals(fragmentsFactory.get(selectedBic).getBankLinkData(), fragment2.getBankLinkData());
        assertTrue(fragment2.beforeAddCalled);
        assertEquals(BankLinkFragment.Mode.Add, fragment2.getMode());
    }

    public void testAddBankLink() {
        Spinner bic = (Spinner) getActivity().findViewById(R.id.bic);
        bic.setSelection(1);
        bic.getOnItemSelectedListener().onItemSelected(null, null, 1, 1);
        String selectedBic = (String) bic.getAdapter().getItem(1);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.executePendingTransactions();

        Spinner ledgerAccountId = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        populateLedgerAccounts(ledgerAccountId, new LedgerAccountDto("a-1", "Account 1"), new LedgerAccountDto("a-2", "Account 2"));
        ledgerAccountId.setSelection(2);

        Button addButton = (Button) getActivity().findViewById(R.id.action_add_bank_link);

        MockSubscriber<AddBankLinkCommand> addHandler = new MockSubscriber<>(AddBankLinkCommand.class);
        bus.register(addHandler);

        getActivity().addBankLink(null);

        assertEquals(1, addHandler.getEvents().size());
        AddBankLinkCommand cmd = addHandler.getEvent();
        assertEquals(selectedBic, cmd.bic);
        assertEquals(getActivity().getInitialFetchDate(), cmd.initialFetchDate);
        assertEquals("a-2", cmd.accountId);
        assertEquals("Account 2", cmd.accountName);
        assertEquals(fragmentsFactory.get(selectedBic).getBankLinkData(), cmd.linkData);
        assertFalse(addButton.isEnabled());
    }

    public void testLinkAdded() {
        Spinner bic = (Spinner) getActivity().findViewById(R.id.bic);
        bic.setSelection(1);
        bic.getOnItemSelectedListener().onItemSelected(null, null, 1, 1);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.executePendingTransactions();

        Spinner ledgerAccountId = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        populateLedgerAccounts(ledgerAccountId, new LedgerAccountDto("a-1", "Account 1"), new LedgerAccountDto("a-2", "Account 2"));
        ledgerAccountId.setSelection(1);

        Button addButton = (Button) getActivity().findViewById(R.id.action_add_bank_link);
        addButton.setEnabled(false);

        getActivity().onEventMainThread(new BankLinkAdded("a-2", "bic"));
        fragmentManager.executePendingTransactions();

        assertEquals(0, ledgerAccountId.getSelectedItemId());
        assertEquals(0, bic.getSelectedItemPosition());
        assertNull(fragmentManager.findFragmentByTag("bank-link-fragment"));
        assertTrue(addButton.isEnabled());
    }

    public void testAddBankLinkFailed() {
        Window wnd = getActivity().getWindow();

        Button addButton = (Button) wnd.findViewById(R.id.action_add_bank_link);
        addButton.setEnabled(false);

        getActivity().onEventMainThread(new AddBankLinkFailed(new Exception("Some exception")));

        assertTrue(addButton.isEnabled());
    }

    public void testChangeInitialFetchDate() {
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -5);

        getActivity().onEventMainThread(new DatePickerFragment.DateChanged(
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ));

        assertEquals(dateOnlyFormat.format(c.getTime()), dateOnlyFormat.format(getActivity().getInitialFetchDate()));

        TextView initialFetchDate = (TextView) getActivity().findViewById(R.id.initial_fetch_date);
        assertEquals(DateFormat.getDateInstance().format(c.getTime()), initialFetchDate.getText().toString());
    }

    private LedgerAccountsLoader.Factory createAccountsLoaderFactory() {
        return new LedgerAccountsLoader.Factory() {
            @Override
            public LedgerAccountsLoader createLoader(Context context) {
                return new MockLedgerAccountsLoader();
            }
        };
    }

    private void populateLedgerAccounts(Spinner spinner, LedgerAccountDto... dtos) {
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                null,
                new String[]{LedgerAccountsLoader.COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);

        final MatrixCursor cursor = new MatrixCursor(new String[]{
                LedgerAccountsLoader.COLUMN_ID,
                LedgerAccountsLoader.COLUMN_ACCOUNT_ID,
                LedgerAccountsLoader.COLUMN_NAME,
        });
        int id = 0;
        cursor.addRow(new Object[]{id++, null, null});
        for (LedgerAccountDto dto : dtos) {
            cursor.addRow(new Object[]{id++, dto.id, dto.name});
        }
        spinnerAdapter.swapCursor(cursor);
        spinner.setAdapter(spinnerAdapter);
    }
}
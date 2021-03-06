package com.infora.ledger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.commands.AdjustTransactionCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 14.04.15.
 */
public class EditTransactionDialogTest extends ActivityInstrumentationTestCase2<ReportActivity> {

    @Inject EventBus bus;

    public EditTransactionDialogTest() {
        super(ReportActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final DeviceSecret secret = DeviceSecret.generateNew();
        final MockLedgerApplication app = new MockLedgerApplication(getActivity())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.deviceSecretProvider = new MockDeviceSecretProvider(secret);
                    }
                });
        app.injector().inject(this);
        app.injector().inject((LedgerApplication) getActivity().getApplication());
    }

    private EditTransactionDialog startFragment(PendingTransaction t) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        EditTransactionDialog fragment = new EditTransactionDialog();
        fragment.transactionId = t.id;
        fragment.amount = t.amount;
        fragment.comment = t.comment;
        transaction.add(fragment, "tag");
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        return fragment;
    }

    public void testInitializeFields() {
        EditTransactionDialog subject = startFragment(new PendingTransaction(100, "100.01", "Comment 100.1"));
        AlertDialog dialog = (AlertDialog) subject.getDialog();
        EditText etAmount = (EditText) dialog.findViewById(R.id.amount);
        EditText etComment = (EditText) dialog.findViewById(R.id.comment);
        assertEquals("100.01", etAmount.getText().toString());
        assertEquals("Comment 100.1", etComment.getText().toString());
    }

    public void testPostAdjustCommandOnPositiveAction() {
        EditTransactionDialog subject = startFragment(new PendingTransaction(100, "100.01", "Comment 100.1"));
        final AlertDialog dialog = (AlertDialog) subject.getDialog();
        final EditText etAmount = (EditText) dialog.findViewById(R.id.amount);
        final EditText etComment = (EditText) dialog.findViewById(R.id.comment);

        MockSubscriber<AdjustTransactionCommand> subscriber = new MockSubscriber<>(AdjustTransactionCommand.class);
        bus.register(subscriber);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etAmount.setText("223");
                etComment.setText("Comment 223");
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();

        assertNotNull(subscriber.getEvent());
        assertEquals(100, subscriber.getEvent().id);
        assertEquals("223", subscriber.getEvent().amount);
        assertEquals("Comment 223", subscriber.getEvent().comment);
    }

    public void testDoNotPostAdjustCommandIfNoChanges() {
        EditTransactionDialog subject = startFragment(new PendingTransaction(100, "100.01", "Comment 100.1"));
        final AlertDialog dialog = (AlertDialog) subject.getDialog();
        MockSubscriber<AdjustTransactionCommand> subscriber = new MockSubscriber<>(AdjustTransactionCommand.class);
        bus.register(subscriber);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertNull("The command should not be posted if no changes", subscriber.getEvent());
    }
}
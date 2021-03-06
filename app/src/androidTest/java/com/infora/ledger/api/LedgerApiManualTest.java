package com.infora.ledger.api;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Build;
import android.test.AndroidTestCase;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.support.AccountManagerWrapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_EXPENSE;
import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_INCOME;

/**
 * Created by jenya on 12.03.15.
 */
public class LedgerApiManualTest extends AndroidTestCase {
    /**
     * Before running tests please specify api endpoint url
     */
    private String endpointUrl = "https://staging.my-ledger.com";

    private LedgerApiFactory adapter;
    private LedgerApi ledgerApi;
    private AccountManagerWrapper accountManager;
    private Account account;

    @Override
    protected void runTest() throws Throwable {
        boolean shouldRun = false;
//        shouldRun = true; //Uncomment this line to run tests
        if (shouldRun) {
            super.runTest();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountManager = new AccountManagerWrapper(getContext());
        account = accountManager.getApplicationAccounts()[0];
        adapter = new LedgerApiFactory(getContext(), accountManager, endpointUrl);
        ledgerApi = adapter.createApi(account);
    }

    public void testReportPendingTransaction() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        LedgerAccountDto accountDto = accounts.get(0);
        String expenseTranId = UUID.randomUUID().toString();
        String incomeTranId = UUID.randomUUID().toString();
        Date date = new Date();
        ledgerApi.reportPendingTransaction(expenseTranId, "100.00", date, "Comment for transaction 100", accountDto.id, TRANSACTION_TYPE_EXPENSE);
        ledgerApi.reportPendingTransaction(incomeTranId, "100.00", date, "Comment for transaction 100", accountDto.id, TransactionContract.TRANSACTION_TYPE_INCOME);
        List<PendingTransactionDto> pendingTransactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto pendingTransaction : pendingTransactions) {
            if (pendingTransaction.transactionId == expenseTranId) {
                assertEquals("100.00", pendingTransaction.amount);
                assertEquals("Comment for transaction 100", pendingTransaction.comment);
                assertEquals(accountDto.id, pendingTransaction.account_id);
                assertEquals(accounts.get(0).id, pendingTransaction.account_id);
                assertEquals(TRANSACTION_TYPE_EXPENSE, pendingTransaction.type_id);
                continue;
            }
            if (pendingTransaction.transactionId == incomeTranId) {
                assertEquals("100.00", pendingTransaction.amount);
                assertEquals("Comment for transaction 100", pendingTransaction.comment);
                assertEquals(accountDto.id, pendingTransaction.account_id);
                assertEquals(accounts.get(0).id, pendingTransaction.account_id);
                assertEquals(TransactionContract.TRANSACTION_TYPE_INCOME, pendingTransaction.type_id);
                continue;
            }
        }
    }

    public void testGetPendingTransactions() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        String t1id = UUID.randomUUID().toString();
        String t2id = UUID.randomUUID().toString();
        ledgerApi.reportPendingTransaction(t1id, "100.00", new Date(), "Comment for transaction 100", accounts.get(0).id, TRANSACTION_TYPE_EXPENSE);
        ledgerApi.reportPendingTransaction(t2id, "100.01", new Date(), "Comment for transaction 101", accounts.get(1).id, TRANSACTION_TYPE_INCOME);
        List<PendingTransactionDto> pendingTransactions = ledgerApi.getPendingTransactions();
        assertFalse("There should be some pending transactions for testing purposes", pendingTransactions.isEmpty());
        for (PendingTransactionDto pendingTransaction : pendingTransactions) {
            assertNotNull(pendingTransaction.transactionId);
            assertNotNull(pendingTransaction.amount);
            assertNotNull(pendingTransaction.comment);
            if (pendingTransaction.transactionId == t1id) {
                assertEquals(accounts.get(0).id, pendingTransaction.account_id);
                assertEquals(TRANSACTION_TYPE_EXPENSE, pendingTransaction.type_id);
            }
            if (pendingTransaction.transactionId == t2id) {
                assertEquals(accounts.get(1).id, pendingTransaction.account_id);
                assertEquals(TRANSACTION_TYPE_INCOME, pendingTransaction.type_id);
            }
        }
    }
    
    public void testAdjustPendingTransaction() {
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        List<PendingTransactionDto> transactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto transaction : transactions) {
            float newAmount = Float.parseFloat(transaction.amount) + 1;
            ledgerApi.adjustPendingTransaction(
                    transaction.transactionId,
                    String.valueOf(newAmount),
                    "Comment " + newAmount,
                    accounts.get(0).id);
        }
    }

    public void testRejectPendingTransactions() {
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", new Date(), "Comment for transaction 100", null, TRANSACTION_TYPE_EXPENSE);
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", new Date(), "Comment for transaction 100", null, TRANSACTION_TYPE_EXPENSE);
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", new Date(), "Comment for transaction 100", null, TRANSACTION_TYPE_EXPENSE);

        List<PendingTransactionDto> transactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto transaction : transactions) {
            ledgerApi.rejectPendingTransaction(transaction.transactionId);
        }
        assertEquals(0, ledgerApi.getPendingTransactions().size());
    }

    public void testGetDeviceSecret() {
        DeviceSecret deviceSecret = ledgerApi.registerDevice(DeviceSecretProvider.getDeviceId(getContext()), Build.MODEL);
        assertNotNull(deviceSecret.secret);
        assertEquals(deviceSecret.secret, ledgerApi.registerDevice(DeviceSecretProvider.getDeviceId(getContext()), Build.MODEL).secret);
    }
}

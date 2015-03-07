package com.infora.ledger;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransactionsDbUtilsTest extends AndroidTestCase {

    private LedgerDbHelper dbHelper;

    @Override
    public void setUp() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "-pending-transactions-repo-test");
        DbUtils.deleteAllDatabases(context);
        dbHelper = new LedgerDbHelper(context);
    }

    public void testGetById() {
        final int id = DbUtils.insertPendingTransaction(dbHelper, "t-1", "100.32", "Transaction t-1");
        PendingTransaction actual = PendingTransactionsDbUtils.getById(dbHelper, id);
        assertFalse("the ID was not generated or assigned.", actual.getId() == 0);
        assertEquals("t-1", actual.getTransactionId());
        assertEquals("100.32", actual.getAmount());
        assertEquals("Transaction t-1", actual.getComment());
        assertNotNull(actual.getTimestamp());
    }

    public void testGetByIdIfNoTransaction() {
        boolean thrown = false;
        try {
            PendingTransactionsDbUtils.getById(dbHelper, 443234);
        } catch (ObjectNotFoundException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testGetByTransactionId() {
        DbUtils.insertPendingTransaction(dbHelper, "t-1", "100.32", "Transaction t-1");

        PendingTransaction actual = PendingTransactionsDbUtils.getByTransactionId(dbHelper, "t-1");
        assertFalse("the ID was not generated or assigned.", actual.getId() == 0);
        assertEquals("t-1", actual.getTransactionId());
        assertEquals("100.32", actual.getAmount());
        assertEquals("Transaction t-1", actual.getComment());
        assertNotNull(actual.getTimestamp());
    }

    public void testGetByTransactionIdIfNoTransaction() {
        boolean thrown = false;
        try {
            PendingTransactionsDbUtils.getByTransactionId(dbHelper, "unknown-t1");
        } catch (ObjectNotFoundException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
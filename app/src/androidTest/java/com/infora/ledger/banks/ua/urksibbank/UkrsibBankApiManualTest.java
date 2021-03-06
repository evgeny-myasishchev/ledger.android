package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.LogUtil;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by mye on 7/8/2015.
 */
public class UkrsibBankApiManualTest extends TestCase {
    private BankApi<UkrsibBankTransaction> api;
    private BankLink bankLink;
    private DeviceSecret secret;

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
        api = new UkrsibBankApi();

        secret = DeviceSecret.generateNew();
        bankLink = new BankLink()
                .setLinkData(new UkrsibBankLinkData("TODO", "TODO", "TODO", "TODO", false), secret);
    }

    public void testGetTransactionsWrongLoginPassword() throws IOException, FetchException {
        bankLink = new BankLink()
                .setLinkData(new UkrsibBankLinkData("fake", "fake", "fake", "fake", false), secret);
        boolean errorRaised = false;
        try {
            api.getTransactions(new GetTransactionsRequest(bankLink, Dates.addDays(new Date(), 10), new Date()), secret);
        } catch (FetchException ex) {
            LogUtil.e(this, "Authentication failed.", ex);
            assertTrue(ex.getMessage().startsWith("Authentication failed:"));
            errorRaised = true;
        }
        assertTrue("Authentication error has not been raised", errorRaised);
    }

    public void testGetTransactionsWithingLastMonth() throws IOException, FetchException {
        List<UkrsibBankTransaction> transactions = api.getTransactions(
                new GetTransactionsRequest(bankLink, Dates.create(2015, 07-1, 01), new Date()), secret);
        for (UkrsibBankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }

    public void testGetTransactionsWithingLastMonthWithAccountTransactions() throws IOException, FetchException {
        UkrsibBankLinkData linkData = bankLink.getLinkData(UkrsibBankLinkData.class, secret);
        linkData.fetchAccountTransactions = true;
        bankLink.setLinkData(linkData, secret);
        List<UkrsibBankTransaction> transactions = api.getTransactions(
                new GetTransactionsRequest(bankLink, Dates.create(2015, 06-1, 15), Dates.create(2015, 06-1, 30)), secret);
        for (UkrsibBankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }

    public void testGetTransactionsForPreviousMonths() throws IOException, FetchException {
        List<UkrsibBankTransaction> transactions = api.getTransactions(
                new GetTransactionsRequest(bankLink, Dates.create(2014, 07-1, 01), Dates.create(2014, 9-1, 30)), secret);
        for (UkrsibBankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }
}
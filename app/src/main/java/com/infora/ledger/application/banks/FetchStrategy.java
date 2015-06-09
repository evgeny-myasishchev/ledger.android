package com.infora.ledger.application.banks;

import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;

import java.sql.SQLException;

/**
 * Created by jenya on 07.06.15.
 */
public abstract class FetchStrategy {
    public abstract void fetchBankTransactions(DatabaseContext db, BankLink bankLink) throws FetchException;
}

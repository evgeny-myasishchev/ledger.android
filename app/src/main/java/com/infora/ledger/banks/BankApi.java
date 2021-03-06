package com.infora.ledger.banks;

import com.infora.ledger.api.DeviceSecret;

import java.io.IOException;
import java.util.List;

/**
 * Created by jenya on 07.07.15.
 */
public interface BankApi<TBankTransaction extends BankTransaction> {
    List<TBankTransaction> getTransactions(GetTransactionsRequest request, DeviceSecret secret) throws IOException, FetchException;
}

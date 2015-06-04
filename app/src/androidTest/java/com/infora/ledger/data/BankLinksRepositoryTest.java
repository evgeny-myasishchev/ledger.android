package com.infora.ledger.data;

import android.test.RenamingDelegatingContext;

import static com.infora.ledger.TestHelper.randomBool;
import static com.infora.ledger.TestHelper.randomDate;
import static com.infora.ledger.TestHelper.randomString;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksRepositoryTest extends GenericDatabaseRepositoryTest<BankLink> {

    @Override
    protected int getId(BankLink rec1) {
        return rec1.id;
    }

    @Override
    protected BankLink setId(BankLink rec, int id) {
        rec.id = id;
        return rec;
    }

    @Override
    protected GenericDatabaseRepository<BankLink> createRepository(RenamingDelegatingContext context) {
        return RepositoryFactory.create(BankLink.class, context);
    }

    @Override
    protected BankLink buildRandomRecord() {
        return new BankLink()
                .setAccountId(randomString("account-"))
                .setAccountName(randomString("Account "))
                .setBic(randomString("bank-"))
                .setLinkDataValue(randomString("link-"))
                .setLastSyncDate(randomDate())
                .setInProgress(randomBool())
                .setHasSucceed(randomBool());
    }
}

package com.infora.ledger.mocks;

import android.content.Context;

import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.BankLinksRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 31.05.15.
 */
public class MockBankLinksRepository extends BankLinksRepository {

    public final ArrayList<BankLink> savedBankLinks;
    public long[] deletedIds;

    public MockBankLinksRepository() {
        super(null);
        savedBankLinks = new ArrayList<>();
    }

    public SQLException saveException;

    @Override
    public BankLink save(BankLink bankLink) throws SQLException {
        if(saveException != null) throw saveException;
        savedBankLinks.add(bankLink);
        return bankLink;
    }

    public BankLink bankLinkToGetById;

    @Override
    public BankLink getById(long id) throws SQLException {
        if(bankLinkToGetById == null)
            throw new AssertionError("BankLink was not assigned.");

        if(bankLinkToGetById.id != id)
            throw new AssertionError("Wrong BankLink id provided. Expected '" + bankLinkToGetById.id + "', was '" + id + "'.");
        return bankLinkToGetById;
    }

    @Override
    public List<BankLink> getAll() throws SQLException {
        return super.getAll();
    }

    @Override
    public void deleteAll(long[] ids) {
        deletedIds = ids;
    }
}

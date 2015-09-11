package com.infora.ledger.application;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.AddBankLinkStrategiesFactory;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.FetchStrategiesFactory;
import com.infora.ledger.banks.FetchStrategy;
import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.FetchBankTransactionsCommand;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeletedEvent;
import com.infora.ledger.application.events.BankTransactionsFetched;
import com.infora.ledger.application.events.FetchBankTransactionsFailed;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.support.Dates;

import java.sql.SQLException;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksService {
    private static final String TAG = BankLinksService.class.getName();
    private final EventBus bus;
    private DatabaseContext db;
    private DeviceSecretProvider secretProvider;
    private DatabaseRepository<BankLink> repository;

    private FetchStrategiesFactory fetchStrategies;
    private AddBankLinkStrategiesFactory addStrategies;

    public BankLinksService(EventBus bus, DatabaseContext db, DeviceSecretProvider secretProvider) {
        this.bus = bus;
        this.db = db;
        this.secretProvider = secretProvider;
        this.repository = db.createRepository(BankLink.class);
    }

    public FetchStrategiesFactory getFetchStrategies() {
        return fetchStrategies == null ? (fetchStrategies = FetchStrategiesFactory.createDefault()) : fetchStrategies;
    }

    public void setFetchStrategies(FetchStrategiesFactory fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }

    public AddBankLinkStrategiesFactory getAddStrategies() {
        return addStrategies == null ? (addStrategies = AddBankLinkStrategiesFactory.createDefault()) : addStrategies;
    }

    public void setAddStrategies(AddBankLinkStrategiesFactory addStrategies) {
        this.addStrategies = addStrategies;
    }

    public void onEventBackgroundThread(AddBankLinkCommand command) {
        Log.d(TAG, "Inserting new bank link for bank: " + command.bic + ", account: " + command.accountName);
        secretProvider.ensureDeviceRegistered();

        if(command.linkData == null) throw new IllegalArgumentException("command.linkData can not be null.");

        DeviceSecret secret = secretProvider.secret();
        BankLink bankLink = new BankLink()
                .setAccountId(command.accountId)
                .setAccountName(command.accountName)
                .setBic(command.bic)
                .setLastSyncDate(command.initialFetchDate)
                .setInitialSyncDate(command.initialFetchDate)
                .setLinkData(command.linkData, secret);

        getAddStrategies().getStrategy(command.bic).addBankLink(bus, repository, bankLink, secret);
    }

    public void onEventBackgroundThread(UpdateBankLinkCommand command) {
        Log.d(TAG, "Updating bank link id='" + command.id + "'");
        secretProvider.ensureDeviceRegistered();
        try {
            BankLink bankLink = repository.getById(command.id);
            bankLink.accountId = command.accountId;
            bankLink.accountName = command.accountName;
            bankLink.setLinkData(command.bankLinkData, secretProvider.secret());
            if (command.fetchStartingFrom != null) {
                Log.d(TAG, "Fetch starting from assigned to: " + command.fetchStartingFrom + ". Setting lastSyncDate to previous day.");
                //Transactions are fetched from lastSyncDate + 1.day so setting it to the previous day
                bankLink.lastSyncDate = Dates.addDays(command.fetchStartingFrom, -1);
            }
            repository.save(bankLink);
            bus.post(new BankLinkUpdated(command.id));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update the bank link.", e);
            bus.post(new UpdateBankLinkFailed(command.id, e));
        }
    }

    public void onEventBackgroundThread(DeleteBankLinksCommand command) throws SQLException {
        Log.d(TAG, "Deleting bank links: " + Arrays.toString(command.ids));
        repository.deleteAll(command.ids);
        Log.d(TAG, "Bank links deleted. Posting deleted event...");
        bus.post(new BankLinksDeletedEvent(command.ids));
    }

    public void onEventBackgroundThread(FetchBankTransactionsCommand command) {
        Log.d(TAG, "Handling fetch bank transactions command. BankLink id=" + command.bankLinkId);
        try {
            BankLink bankLink = repository.getById(command.bankLinkId);
            fetchBankTransactions(bankLink);
            Log.e(TAG, "Bank transactions fetched. Posting success event.");
            bus.post(new BankTransactionsFetched(bankLink));
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch bank transactions. Posting failure event.", e);
            bus.post(new FetchBankTransactionsFailed(command.bankLinkId, e));
        }
    }

    public void fetchBankTransactions(BankLink bankLink) throws FetchException {
        secretProvider.ensureDeviceRegistered();
        FetchStrategy fetchStrategy = getFetchStrategies().getStrategy(bankLink.bic);
        fetchStrategy.fetchBankTransactions(db, bankLink, secretProvider.secret());
    }
}

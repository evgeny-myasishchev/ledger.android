package com.infora.ledger.application;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;

import com.infora.ledger.api.LedgerApi;

import java.sql.SQLException;

/**
 * Created by jenya on 25.03.15.
 */
public interface SynchronizationStrategy {
    void synchronize(LedgerApi api, Bundle options, SyncResult syncResult) throws SQLException;
}

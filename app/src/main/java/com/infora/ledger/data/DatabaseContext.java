package com.infora.ledger.data;

import android.content.Context;

import com.infora.ledger.LedgerApplication;

import java.util.Stack;

import javax.inject.Inject;

/**
 * Created by jenya on 05.06.15.
 */
public class DatabaseContext {
    private Context context;

    @Inject public DatabaseContext(Context context) {
        this.context = context;
    }

    public <TEntity extends Entity> DatabaseRepository<TEntity> createRepository(Class<TEntity> classOfTEntity) {
        return new DatabaseRepository<>(classOfTEntity, context);
    }

    public UnitOfWork newUnitOfWork() {
        return new UnitOfWork(context);
    }

    public static DatabaseContext getInstance(Context context) {
        return ((LedgerApplication) context.getApplicationContext()).getDatabaseContext();
    }

    public TransactionsReadModel getTransactionsReadModel() {
        return new TransactionsReadModel(context);
    }
}

package com.infora.ledger.banks.ua.privatbank;

import android.util.Base64;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jenya on 10.09.15.
 */
public class Privat24Transaction implements BankTransaction {
    public static final DateFormat DateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
    public static final String PRIVATBANK_BIC = "PBANUA2X";

    public String date;
    public String amount;
    public String originalAmount;
    public String description;

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        int typeId = amount.startsWith("-") ?
                TransactionContract.TRANSACTION_TYPE_EXPENSE : TransactionContract.TRANSACTION_TYPE_INCOME;
        Date timestamp;
        try {
            timestamp = DateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String absOriginalAmount = originalAmount.replace("-", "");
        String transactionId = bankLink.accountId + timestamp.getTime() + absOriginalAmount.replace(".", "P");
        //Using sha256 and Base64 to have transactionId no longer than 50 chars.
        transactionId = Base64.encodeToString(DigestUtils.sha256(transactionId), Base64.NO_PADDING + Base64.NO_WRAP + Base64.URL_SAFE);
        return new PendingTransaction()
                .setTypeId(typeId)
                .setAccountId(bankLink.accountId)
                .setTransactionId(transactionId)
                .setAmount(amount.replace("-", ""))
                .setComment(description)
                .setTimestamp(timestamp)
                .setBic(bankLink.bic);
    }

    @Override
    public String toString() {
        return "Privat24Transaction{" +
                "date=" + date +
                ", amount='" + amount + '\'' +
                ", originalAmount='" + originalAmount + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

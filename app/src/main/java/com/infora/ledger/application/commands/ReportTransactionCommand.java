package com.infora.ledger.application.commands;

/**
 * Created by jenya on 10.03.15.
 */
public class ReportTransactionCommand extends Command {
    public String accountId;
    public String amount;
    public String comment;

    public ReportTransactionCommand(String accountId, String amount, String comment) {
        this.accountId = accountId;
        this.amount = amount;
        this.comment = comment;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

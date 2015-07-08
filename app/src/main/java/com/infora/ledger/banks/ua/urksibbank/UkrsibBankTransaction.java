package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankTransaction implements BankTransaction {
    //http://www.theswiftcodes.com/ukraine/khabua2k/
    public static final String BIC = "KHABUA2K";

    public String trandate;
    public String commitDate;
    public String authCode;
    public String description;
    public String currency;
    public String amount;
    public String accountAmount;

    public UkrsibBankTransaction setTrandate(String trandate) {
        this.trandate = trandate;
        return this;
    }

    public UkrsibBankTransaction setCommitDate(String commitDate) {
        this.commitDate = commitDate;
        return this;
    }

    public UkrsibBankTransaction setAuthCode(String authCode) {
        this.authCode = authCode;
        return this;
    }

    public UkrsibBankTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public UkrsibBankTransaction setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public UkrsibBankTransaction setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public UkrsibBankTransaction setAccountAmount(String accountAmount) {
        this.accountAmount = accountAmount;
        return this;
    }

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UkrsibBankTransaction that = (UkrsibBankTransaction) o;

        if (trandate != null ? !trandate.equals(that.trandate) : that.trandate != null)
            return false;
        if (commitDate != null ? !commitDate.equals(that.commitDate) : that.commitDate != null)
            return false;
        if (authCode != null ? !authCode.equals(that.authCode) : that.authCode != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null)
            return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        return !(accountAmount != null ? !accountAmount.equals(that.accountAmount) : that.accountAmount != null);

    }

    @Override
    public int hashCode() {
        int result = trandate != null ? trandate.hashCode() : 0;
        result = 31 * result + (commitDate != null ? commitDate.hashCode() : 0);
        result = 31 * result + (authCode != null ? authCode.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (accountAmount != null ? accountAmount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UkrsibBankTransaction{" +
                "trandate='" + trandate + '\'' +
                ", commitDate='" + commitDate + '\'' +
                ", authCode='" + authCode + '\'' +
                ", description='" + description + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                ", accountAmount='" + accountAmount + '\'' +
                '}';
    }
}
package com.infora.ledger.banks.ua.privatbank.messages;

/**
 * Created by mye on 9/11/2015.
 */
public class AskPrivat24Otp {
    public int linkId;
    public String operationId;

    public AskPrivat24Otp(int linkId, String operationId) {
        this.linkId = linkId;
        this.operationId = operationId;
    }
}
package com.infora.ledger.banks;

/**
 * Created by jenya on 23.05.15.
 */
public interface PrivatBankRequestSignatureBuilder {
    String build(String data, String password);
}

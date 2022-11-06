package org.asamk.signal.manager.api;

import org.signal.libsignal.protocol.IdentityKey;

public class Identity {
    RecipientAddress recipient;
    IdentityKey identityKey;
    String safetyNumber;
    byte[] scannableSafetyNumber;
    TrustLevel trustLevel;
    long dateAddedTimestamp;

    public Identity(RecipientAddress recipient, IdentityKey identityKey, String safetyNumber,
            byte[] scannableSafetyNumber, TrustLevel trustLevel, long dateAddedTimestamp) {
        super();
        this.recipient = recipient;
        this.identityKey = identityKey;
        this.safetyNumber = safetyNumber;
        this.scannableSafetyNumber = scannableSafetyNumber;
        this.trustLevel = trustLevel;
        this.dateAddedTimestamp = dateAddedTimestamp;
    }

    public byte[] getFingerprint() {
        return identityKey.getPublicKey().serialize();
    }
}

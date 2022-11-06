package org.asamk.signal.manager.api;

import java.util.List;
import java.util.Map;

public class SendMessageResults {

    long timestamp;
    Map<RecipientIdentifier, List<SendMessageResult>> results;

    public SendMessageResults(long timestamp, Map<RecipientIdentifier, List<SendMessageResult>> results) {
        super();
        this.timestamp = timestamp;
        this.results = results;
    }

    public boolean hasSuccess() {
        return results.values().stream().flatMap(res -> res.stream().map(sr -> sr.isSuccess))
                .anyMatch(success -> success) || results.values().stream().mapToInt(List::size).sum() == 0;
    }

    public boolean hasOnlyUntrustedIdentity() {
        return results.values().stream().flatMap(res -> res.stream().map(sr -> sr.isIdentityFailure)).allMatch(
                identityFailure -> identityFailure) && results.values().stream().mapToInt(List::size).sum() > 0;
    }
}

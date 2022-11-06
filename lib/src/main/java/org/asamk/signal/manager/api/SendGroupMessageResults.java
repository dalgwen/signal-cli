package org.asamk.signal.manager.api;

import java.util.List;

public class SendGroupMessageResults {
    long timestamp;
    List<SendMessageResult> results;

    public SendGroupMessageResults(long timestamp, List<SendMessageResult> results) {
        super();
        this.timestamp = timestamp;
        this.results = results;
    }

}

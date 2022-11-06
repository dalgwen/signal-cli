package org.asamk.signal.manager.storage.sendLog;

import java.util.Optional;

import org.asamk.signal.manager.groups.GroupId;
import org.whispersystems.signalservice.api.crypto.ContentHint;
import org.whispersystems.signalservice.internal.push.SignalServiceProtos;
import org.whispersystems.signalservice.internal.push.SignalServiceProtos.Content;

public class MessageSendLogEntry {
    public Optional<GroupId> groupId;
    public SignalServiceProtos.Content content;
    public ContentHint contentHint;
    public boolean urgent;

    public MessageSendLogEntry(Optional<GroupId> groupId, Content content, ContentHint contentHint, boolean urgent) {
        super();
        this.groupId = groupId;
        this.content = content;
        this.contentHint = contentHint;
        this.urgent = urgent;
    }

}

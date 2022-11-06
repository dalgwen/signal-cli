package org.asamk.signal.manager.api;

public class ReceiveConfig {
    public boolean ignoreAttachments;
    public boolean ignoreStories;
    public boolean sendReadReceipts;

    public ReceiveConfig(boolean ignoreAttachments, boolean ignoreStories, boolean sendReadReceipts) {
        super();
        this.ignoreAttachments = ignoreAttachments;
        this.ignoreStories = ignoreStories;
        this.sendReadReceipts = sendReadReceipts;
    }

}

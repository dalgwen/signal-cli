package org.asamk.signal.manager.api;

import java.util.Optional;

import org.asamk.signal.manager.storage.configuration.ConfigurationStore;

public class Configuration {

    public Configuration(Optional<Boolean> readReceipts, Optional<Boolean> unidentifiedDeliveryIndicators,
            Optional<Boolean> typingIndicators, Optional<Boolean> linkPreviews) {
        super();
        this.readReceipts = readReceipts;
        this.unidentifiedDeliveryIndicators = unidentifiedDeliveryIndicators;
        this.typingIndicators = typingIndicators;
        this.linkPreviews = linkPreviews;
    }

    public Optional<Boolean> readReceipts;
    public Optional<Boolean> unidentifiedDeliveryIndicators;
    public Optional<Boolean> typingIndicators;
    public Optional<Boolean> linkPreviews;

    public static Configuration from(final ConfigurationStore configurationStore) {
        return new Configuration(Optional.ofNullable(configurationStore.getReadReceipts()),
                Optional.ofNullable(configurationStore.getUnidentifiedDeliveryIndicators()),
                Optional.ofNullable(configurationStore.getTypingIndicators()),
                Optional.ofNullable(configurationStore.getLinkPreviews()));
    }
}

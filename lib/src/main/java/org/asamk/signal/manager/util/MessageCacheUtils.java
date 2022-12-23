package org.asamk.signal.manager.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.push.ServiceId;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.api.util.UuidUtil;

public class MessageCacheUtils {

    private final static Logger logger = LoggerFactory.getLogger(MessageCacheUtils.class);

    final static int CURRENT_VERSION = 8;

    public static SignalServiceEnvelope loadEnvelope(File file) throws IOException {
        try (var f = new FileInputStream(file)) {
            var in = new DataInputStream(f);
            var version = in.readInt();
            logger.trace("Reading cached envelope file with version {} (current: {})", version, CURRENT_VERSION);
            if (version > CURRENT_VERSION) {
                logger.warn("Unsupported envelope version {} (current: {})", version, CURRENT_VERSION);
                // Unsupported envelope version
                return null;
            }
            var type = in.readInt();
            var source = in.readUTF();
            ServiceId sourceServiceId = null;
            if (version >= 3) {
                sourceServiceId = ServiceId.parseOrNull(in.readUTF());
            }
            var sourceDevice = in.readInt();
            if (version == 1) {
                // read legacy relay field
                in.readUTF();
            }
            String destinationUuid = null;
            if (version >= 5) {
                destinationUuid = in.readUTF();
            }
            var timestamp = in.readLong();
            byte[] content = null;
            var contentLen = in.readInt();
            if (contentLen > 0) {
                content = new byte[contentLen];
                in.readFully(content);
            }
            var legacyMessageLen = in.readInt();
            if (legacyMessageLen > 0) {
                byte[] legacyMessage = new byte[legacyMessageLen];
                in.readFully(legacyMessage);
            }
            long serverReceivedTimestamp = 0;
            String uuid = null;
            if (version >= 2) {
                serverReceivedTimestamp = in.readLong();
                uuid = in.readUTF();
                if ("".equals(uuid)) {
                    uuid = null;
                }
            }
            long serverDeliveredTimestamp = 0;
            if (version >= 4) {
                serverDeliveredTimestamp = in.readLong();
            }
            boolean isUrgent = true;
            if (version >= 6) {
                isUrgent = in.readBoolean();
            }
            boolean isStory = true;
            if (version >= 7) {
                isStory = in.readBoolean();
            }
            String updatedPni = null;
            if (version >= 8) {
                updatedPni = in.readUTF();
            }
            @SuppressWarnings("null")
            Optional<SignalServiceAddress> addressOptional = sourceServiceId == null ? Optional.empty()
                    : Optional.of(new SignalServiceAddress(sourceServiceId, source));
            return new SignalServiceEnvelope(type, addressOptional, sourceDevice, timestamp, content,
                    serverReceivedTimestamp, serverDeliveredTimestamp, uuid,
                    destinationUuid == null ? UuidUtil.UNKNOWN_UUID.toString() : destinationUuid, isUrgent,
                    updatedPni == null ? "" : updatedPni, isStory);
        }
    }

    public static void storeEnvelope(SignalServiceEnvelope envelope, File file) throws IOException {
        try (var f = new FileOutputStream(file)) {
            try (var out = new DataOutputStream(f)) {
                out.writeInt(CURRENT_VERSION); // version
                out.writeInt(envelope.getType());
                out.writeUTF(""); // legacy number
                out.writeUTF(envelope.getSourceUuid().isPresent() ? envelope.getSourceUuid().get() : "");
                out.writeInt(envelope.getSourceDevice());
                out.writeUTF(envelope.getDestinationUuid() == null ? "" : envelope.getDestinationUuid());
                out.writeLong(envelope.getTimestamp());
                if (envelope.hasContent()) {
                    out.writeInt(envelope.getContent().length);
                    out.write(envelope.getContent());
                } else {
                    out.writeInt(0);
                }
                out.writeInt(0); // legacy message
                out.writeLong(envelope.getServerReceivedTimestamp());
                var uuid = envelope.getServerGuid();
                out.writeUTF(uuid == null ? "" : uuid);
                out.writeLong(envelope.getServerDeliveredTimestamp());
                out.writeBoolean(envelope.isUrgent());
                out.writeBoolean(envelope.isStory());
                out.writeUTF(envelope.getUpdatedPni() == null ? "" : envelope.getUpdatedPni());
            }
        }
    }
}

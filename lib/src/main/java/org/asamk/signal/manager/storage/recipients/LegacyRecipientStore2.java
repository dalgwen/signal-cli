package org.asamk.signal.manager.storage.recipients;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.asamk.signal.manager.storage.Utils;
import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.profiles.ExpiringProfileKeyCredential;
import org.signal.libsignal.zkgroup.profiles.ProfileKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.signalservice.api.push.ServiceId;

public class LegacyRecipientStore2 {

    private final static Logger logger = LoggerFactory.getLogger(LegacyRecipientStore2.class);

    public static void migrate(File file, RecipientStore recipientStore) {
        final var objectMapper = Utils.createStorageObjectMapper();
        try (var inputStream = new FileInputStream(file)) {
            final var storage = objectMapper.readValue(inputStream, Storage.class);

            final var recipients = storage.recipients.stream().map(r -> {
                final var recipientId = new RecipientId(r.id, recipientStore);
                final var address = new RecipientAddress(Optional.ofNullable(r.uuid).map(ServiceId::parseOrThrow),
                        Optional.ofNullable(r.number));

                Contact contact = null;
                if (r.contact != null) {
                    contact = new Contact(r.contact.name, null, r.contact.color, r.contact.messageExpirationTime,
                            r.contact.blocked, r.contact.archived, r.contact.profileSharingEnabled);
                }

                ProfileKey profileKey = null;
                if (r.profileKey != null) {
                    try {
                        profileKey = new ProfileKey(Base64.getDecoder().decode(r.profileKey));
                    } catch (InvalidInputException ignored) {
                    }
                }

                ExpiringProfileKeyCredential expiringProfileKeyCredential = null;
                if (r.expiringProfileKeyCredential != null) {
                    try {
                        expiringProfileKeyCredential = new ExpiringProfileKeyCredential(
                                Base64.getDecoder().decode(r.expiringProfileKeyCredential));
                    } catch (Throwable ignored) {
                    }
                }

                Profile profile = null;
                if (r.profile != null) {
                    profile = new Profile(r.profile.lastUpdateTimestamp, r.profile.givenName, r.profile.familyName,
                            r.profile.about, r.profile.aboutEmoji, r.profile.avatarUrlPath,
                            r.profile.mobileCoinAddress == null ? null
                                    : Base64.getDecoder().decode(r.profile.mobileCoinAddress),
                            Profile.UnidentifiedAccessMode.valueOfOrUnknown(r.profile.unidentifiedAccessMode),
                            r.profile.capabilities.stream().map(Profile.Capability::valueOfOrNull)
                                    .filter(Objects::nonNull).collect(Collectors.toSet()));
                }

                return new Recipient(recipientId, address, contact, profileKey, expiringProfileKeyCredential, profile);
            }).collect(Collectors.toMap(Recipient::getRecipientId, r -> r));

            recipientStore.addLegacyRecipients(recipients);
        } catch (FileNotFoundException e) {
            // nothing to migrate
        } catch (IOException e) {
            logger.warn("Failed to load recipient store", e);
            throw new RuntimeException(e);
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            logger.warn("Failed to load recipient store", e);
            throw new RuntimeException(e);
        }
    }

    private static class Storage {

        List<Recipient> recipients;
        long lastId;

        public Storage(List<Recipient> recipients, long lastId) {
            super();
            this.recipients = recipients;
            this.lastId = lastId;
        }

        public static class Recipient {
            long id;
            String number;
            String uuid;
            String profileKey;
            String expiringProfileKeyCredential;
            Contact contact;
            Profile profile;

            public Recipient(long id, String number, String uuid, String profileKey,
                    String expiringProfileKeyCredential, Contact contact, Profile profile) {
                super();
                this.id = id;
                this.number = number;
                this.uuid = uuid;
                this.profileKey = profileKey;
                this.expiringProfileKeyCredential = expiringProfileKeyCredential;
                this.contact = contact;
                this.profile = profile;
            }

            static class Contact {
                String name;
                String color;
                int messageExpirationTime;
                boolean blocked;
                boolean archived;
                boolean profileSharingEnabled;

                public Contact(String name, String color, int messageExpirationTime, boolean blocked, boolean archived,
                        boolean profileSharingEnabled) {
                    super();
                    this.name = name;
                    this.color = color;
                    this.messageExpirationTime = messageExpirationTime;
                    this.blocked = blocked;
                    this.archived = archived;
                    this.profileSharingEnabled = profileSharingEnabled;
                }
            }

            static class Profile {

                long lastUpdateTimestamp;
                String givenName;
                String familyName;
                String about;
                String aboutEmoji;
                String avatarUrlPath;
                String mobileCoinAddress;
                String unidentifiedAccessMode;
                Set<String> capabilities;

                public Profile(long lastUpdateTimestamp, String givenName, String familyName, String about,
                        String aboutEmoji, String avatarUrlPath, String mobileCoinAddress,
                        String unidentifiedAccessMode, Set<String> capabilities) {
                    super();
                    this.lastUpdateTimestamp = lastUpdateTimestamp;
                    this.givenName = givenName;
                    this.familyName = familyName;
                    this.about = about;
                    this.aboutEmoji = aboutEmoji;
                    this.avatarUrlPath = avatarUrlPath;
                    this.mobileCoinAddress = mobileCoinAddress;
                    this.unidentifiedAccessMode = unidentifiedAccessMode;
                    this.capabilities = capabilities;
                }

            }
        }
    }
}

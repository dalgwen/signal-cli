package org.asamk.signal.manager.api;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.asamk.signal.manager.api.MessageEnvelope.Data.Attachment;
import org.asamk.signal.manager.groups.GroupId;
import org.asamk.signal.manager.groups.GroupUtils;
import org.asamk.signal.manager.helper.RecipientAddressResolver;
import org.asamk.signal.manager.storage.recipients.RecipientResolver;
import org.signal.libsignal.metadata.ProtocolException;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentRemoteId;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup;
import org.whispersystems.signalservice.api.messages.SignalServiceGroupContext;
import org.whispersystems.signalservice.api.messages.SignalServicePreview;
import org.whispersystems.signalservice.api.messages.SignalServiceReceiptMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceStoryMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceTextAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceTypingMessage;
import org.whispersystems.signalservice.api.messages.calls.AnswerMessage;
import org.whispersystems.signalservice.api.messages.calls.BusyMessage;
import org.whispersystems.signalservice.api.messages.calls.HangupMessage;
import org.whispersystems.signalservice.api.messages.calls.IceUpdateMessage;
import org.whispersystems.signalservice.api.messages.calls.OfferMessage;
import org.whispersystems.signalservice.api.messages.calls.OpaqueMessage;
import org.whispersystems.signalservice.api.messages.calls.SignalServiceCallMessage;
import org.whispersystems.signalservice.api.messages.multidevice.BlockedListMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ContactsMessage;
import org.whispersystems.signalservice.api.messages.multidevice.MessageRequestResponseMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SentTranscriptMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ViewOnceOpenMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ViewedMessage;

public class MessageEnvelope {

    public MessageEnvelope(Optional<RecipientAddress> sourceAddress, int sourceDevice, long timestamp,
            long serverReceivedTimestamp, long serverDeliveredTimestamp, boolean isUnidentifiedSender,
            Optional<Receipt> receipt, Optional<Typing> typing, Optional<Data> data, Optional<Sync> sync,
            Optional<Call> call, Optional<Story> story) {
        super();
        this.sourceAddress = sourceAddress;
        this.sourceDevice = sourceDevice;
        this.timestamp = timestamp;
        this.serverReceivedTimestamp = serverReceivedTimestamp;
        this.serverDeliveredTimestamp = serverDeliveredTimestamp;
        this.isUnidentifiedSender = isUnidentifiedSender;
        this.receipt = receipt;
        this.typing = typing;
        this.data = data;
        this.sync = sync;
        this.call = call;
        this.story = story;
    }

    public Optional<RecipientAddress> sourceAddress;
    public int sourceDevice;
    public long timestamp;
    public long serverReceivedTimestamp;
    public long serverDeliveredTimestamp;
    public boolean isUnidentifiedSender;
    public Optional<Receipt> receipt;
    public Optional<Typing> typing;
    public Optional<Data> data;
    public Optional<Sync> sync;
    public Optional<Call> call;
    public Optional<Story> story;

    public static class Receipt {

        public long when;
        public Type type;
        public List<Long> timestamps;

        public Receipt(long when, Type type, List<Long> timestamps) {
            super();
            this.when = when;
            this.type = type;
            this.timestamps = timestamps;
        }

        static Receipt from(final SignalServiceReceiptMessage receiptMessage) {
            return new Receipt(receiptMessage.getWhen(), Type.from(receiptMessage.getType()),
                    receiptMessage.getTimestamps());
        }

        public enum Type {
            DELIVERY,
            READ,
            VIEWED,
            UNKNOWN;

            static Type from(SignalServiceReceiptMessage.Type type) {
                switch (type) {
                    case DELIVERY:
                        return DELIVERY;
                    case READ:
                        return READ;
                    case VIEWED:
                        return VIEWED;
                    case UNKNOWN:
                    default:
                        return UNKNOWN;
                }
            }
        }
    }

    public static class Typing {
        long timestamp;
        Type type;
        Optional<GroupId> groupId;

        public Typing(long timestamp, Type type, Optional<GroupId> groupId) {
            super();
            this.timestamp = timestamp;
            this.type = type;
            this.groupId = groupId;
        }

        public static Typing from(final SignalServiceTypingMessage typingMessage) {
            return new Typing(typingMessage.getTimestamp(),
                    typingMessage.isTypingStarted() ? Type.STARTED : Type.STOPPED,
                    typingMessage.getGroupId().map(GroupId::unknownVersion));
        }

        public enum Type {
            STARTED,
            STOPPED,
        }
    }

    public static class Data {

        public long timestamp;
        public Optional<GroupContext> groupContext;
        public Optional<StoryContext> storyContext;
        public Optional<GroupCallUpdate> groupCallUpdate;
        public Optional<String> body;
        public int expiresInSeconds;
        public boolean isExpirationUpdate;
        public boolean isViewOnce;
        public boolean isEndSession;
        public boolean isProfileKeyUpdate;
        public boolean hasProfileKey;
        public Optional<Reaction> reaction;
        public Optional<Quote> quote;
        public Optional<Payment> payment;
        public List<Attachment> attachments;
        public Optional<Long> remoteDeleteId;
        public Optional<Sticker> sticker;
        public List<SharedContact> sharedContacts;
        public List<Mention> mentions;
        public List<Preview> previews;

        public Data(long timestamp, Optional<GroupContext> groupContext, Optional<StoryContext> storyContext,
                Optional<GroupCallUpdate> groupCallUpdate, Optional<String> body, int expiresInSeconds,
                boolean isExpirationUpdate, boolean isViewOnce, boolean isEndSession, boolean isProfileKeyUpdate,
                boolean hasProfileKey, Optional<Reaction> reaction, Optional<Quote> quote, Optional<Payment> payment,
                List<Attachment> attachments, Optional<Long> remoteDeleteId, Optional<Sticker> sticker,
                List<SharedContact> sharedContacts, List<Mention> mentions, List<Preview> previews) {
            super();
            this.timestamp = timestamp;
            this.groupContext = groupContext;
            this.storyContext = storyContext;
            this.groupCallUpdate = groupCallUpdate;
            this.body = body;
            this.expiresInSeconds = expiresInSeconds;
            this.isExpirationUpdate = isExpirationUpdate;
            this.isViewOnce = isViewOnce;
            this.isEndSession = isEndSession;
            this.isProfileKeyUpdate = isProfileKeyUpdate;
            this.hasProfileKey = hasProfileKey;
            this.reaction = reaction;
            this.quote = quote;
            this.payment = payment;
            this.attachments = attachments;
            this.remoteDeleteId = remoteDeleteId;
            this.sticker = sticker;
            this.sharedContacts = sharedContacts;
            this.mentions = mentions;
            this.previews = previews;
        }

        static Data from(final SignalServiceDataMessage dataMessage, RecipientResolver recipientResolver,
                RecipientAddressResolver addressResolver, final AttachmentFileProvider fileProvider) {
            return new Data(dataMessage.getTimestamp(), dataMessage.getGroupContext().map(GroupContext::from),
                    dataMessage.getStoryContext()
                            .map((SignalServiceDataMessage.StoryContext storyContext) -> StoryContext.from(storyContext,
                                    recipientResolver, addressResolver)),
                    dataMessage.getGroupCallUpdate().map(GroupCallUpdate::from), dataMessage.getBody(),
                    dataMessage.getExpiresInSeconds(), dataMessage.isExpirationUpdate(), dataMessage.isViewOnce(),
                    dataMessage.isEndSession(), dataMessage.isProfileKeyUpdate(),
                    dataMessage.getProfileKey().isPresent(),
                    dataMessage.getReaction().map(r -> Reaction.from(r, recipientResolver, addressResolver)),
                    dataMessage.getQuote().map(q -> Quote.from(q, recipientResolver, addressResolver, fileProvider)),
                    dataMessage.getPayment().map(p -> p.getPaymentNotification().isPresent() ? Payment.from(p) : null),
                    dataMessage.getAttachments()
                            .map(a -> a.stream().map(as -> Attachment.from(as, fileProvider))
                                    .collect(Collectors.toList()))
                            .orElse(List.of()),
                    dataMessage.getRemoteDelete().map(SignalServiceDataMessage.RemoteDelete::getTargetSentTimestamp),
                    dataMessage.getSticker().map(Sticker::from),
                    dataMessage.getSharedContacts()
                            .map(a -> a.stream().map(sharedContact -> SharedContact.from(sharedContact, fileProvider))
                                    .collect(Collectors.toList()))
                            .orElse(List.of()),
                    dataMessage.getMentions()
                            .map(a -> a.stream().map(m -> Mention.from(m, recipientResolver, addressResolver))
                                    .collect(Collectors.toList()))
                            .orElse(List.of()),
                    dataMessage.getPreviews().map(a -> a.stream().map(preview -> Preview.from(preview, fileProvider))
                            .collect(Collectors.toList())).orElse(List.of()));
        }

        public static class GroupContext {

            GroupId groupId;
            boolean isGroupUpdate;
            int revision;

            public GroupContext(GroupId groupId, boolean isGroupUpdate, int revision) {
                super();
                this.groupId = groupId;
                this.isGroupUpdate = isGroupUpdate;
                this.revision = revision;
            }

            static GroupContext from(SignalServiceGroupContext groupContext) {
                if (groupContext.getGroupV1().isPresent()) {
                    return new GroupContext(GroupId.v1(groupContext.getGroupV1().get().getGroupId()),
                            groupContext.getGroupV1Type() == SignalServiceGroup.Type.UPDATE, 0);
                } else if (groupContext.getGroupV2().isPresent()) {
                    final var groupV2 = groupContext.getGroupV2().get();
                    return new GroupContext(GroupUtils.getGroupIdV2(groupV2.getMasterKey()),
                            groupV2.hasSignedGroupChange(), groupV2.getRevision());
                } else {
                    throw new RuntimeException("Invalid group context state");
                }
            }
        }

        public static class StoryContext {
            RecipientAddress author;
            long sentTimestamp;

            public StoryContext(RecipientAddress author, long sentTimestamp) {
                super();
                this.author = author;
                this.sentTimestamp = sentTimestamp;
            }

            static StoryContext from(SignalServiceDataMessage.StoryContext storyContext,
                    RecipientResolver recipientResolver, RecipientAddressResolver addressResolver) {
                return new StoryContext(addressResolver
                        .resolveRecipientAddress(recipientResolver.resolveRecipient(storyContext.getAuthorServiceId()))
                        .toApiRecipientAddress(), storyContext.getSentTimestamp());
            }
        }

        public static class GroupCallUpdate {
            String eraId;

            public GroupCallUpdate(String eraId) {
                super();
                this.eraId = eraId;
            }

            static GroupCallUpdate from(SignalServiceDataMessage.GroupCallUpdate groupCallUpdate) {
                return new GroupCallUpdate(groupCallUpdate.getEraId());
            }
        }

        public static class Reaction {

            long targetSentTimestamp;
            RecipientAddress targetAuthor;
            String emoji;
            boolean isRemove;

            public Reaction(long targetSentTimestamp, RecipientAddress targetAuthor, String emoji, boolean isRemove) {
                super();
                this.targetSentTimestamp = targetSentTimestamp;
                this.targetAuthor = targetAuthor;
                this.emoji = emoji;
                this.isRemove = isRemove;
            }

            static Reaction from(SignalServiceDataMessage.Reaction reaction, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver) {
                return new Reaction(reaction.getTargetSentTimestamp(),
                        addressResolver
                                .resolveRecipientAddress(recipientResolver.resolveRecipient(reaction.getTargetAuthor()))
                                .toApiRecipientAddress(),
                        reaction.getEmoji(), reaction.isRemove());
            }
        }

        public static class Quote {
            long id;
            RecipientAddress author;
            Optional<String> text;
            List<Mention> mentions;
            List<Attachment> attachments;

            public Quote(long id, RecipientAddress author, Optional<String> text, List<Mention> mentions,
                    List<Attachment> attachments) {
                super();
                this.id = id;
                this.author = author;
                this.text = text;
                this.mentions = mentions;
                this.attachments = attachments;
            }

            static Quote from(SignalServiceDataMessage.Quote quote, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver, final AttachmentFileProvider fileProvider) {
                return new Quote(quote.getId(),
                        addressResolver.resolveRecipientAddress(recipientResolver.resolveRecipient(quote.getAuthor()))
                                .toApiRecipientAddress(),
                        Optional.ofNullable(quote.getText()),
                        quote.getMentions() == null ? List.of()
                                : quote.getMentions().stream()
                                        .map(m -> Mention.from(m, recipientResolver, addressResolver))
                                        .collect(Collectors.toList()),
                        quote.getAttachments() == null ? List.of()
                                : quote.getAttachments().stream().map(a -> Attachment.from(a, fileProvider))
                                        .collect(Collectors.toList()));
            }
        }

        public static class Payment {

            String note;
            byte[] receipt;

            public Payment(String note, byte[] receipt) {
                super();
                this.note = note;
                this.receipt = receipt;
            }

            static Payment from(SignalServiceDataMessage.Payment payment) {
                return new Payment(payment.getPaymentNotification().get().getNote(),
                        payment.getPaymentNotification().get().getReceipt());
            }
        }

        public static class Mention {
            RecipientAddress recipient;
            int start;
            int length;

            public Mention(RecipientAddress recipient, int start, int length) {
                super();
                this.recipient = recipient;
                this.start = start;
                this.length = length;
            }

            static Mention from(SignalServiceDataMessage.Mention mention, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver) {
                return new Mention(addressResolver
                        .resolveRecipientAddress(recipientResolver.resolveRecipient(mention.getServiceId()))
                        .toApiRecipientAddress(), mention.getStart(), mention.getLength());
            }
        }

        public static class Attachment {
            Optional<String> id;
            Optional<File> file;
            Optional<String> fileName;
            String contentType;
            Optional<Long> uploadTimestamp;
            Optional<Long> size;
            Optional<byte[]> preview;
            Optional<Attachment> thumbnail;
            Optional<String> caption;
            Optional<Integer> width;
            Optional<Integer> height;
            boolean isVoiceNote;
            boolean isGif;
            boolean isBorderless;

            public Attachment(Optional<String> id, Optional<File> file, Optional<String> fileName, String contentType,
                    Optional<Long> uploadTimestamp, Optional<Long> size, Optional<byte[]> preview,
                    Optional<Attachment> thumbnail, Optional<String> caption, Optional<Integer> width,
                    Optional<Integer> height, boolean isVoiceNote, boolean isGif, boolean isBorderless) {
                super();
                this.id = id;
                this.file = file;
                this.fileName = fileName;
                this.contentType = contentType;
                this.uploadTimestamp = uploadTimestamp;
                this.size = size;
                this.preview = preview;
                this.thumbnail = thumbnail;
                this.caption = caption;
                this.width = width;
                this.height = height;
                this.isVoiceNote = isVoiceNote;
                this.isGif = isGif;
                this.isBorderless = isBorderless;
            }

            static Attachment from(SignalServiceAttachment attachment, AttachmentFileProvider fileProvider) {
                if (attachment.isPointer()) {
                    final var a = attachment.asPointer();
                    return new Attachment(Optional.of(a.getRemoteId().toString()),
                            Optional.of(fileProvider.getFile(a.getRemoteId())), a.getFileName(), a.getContentType(),
                            a.getUploadTimestamp() == 0 ? Optional.empty() : Optional.of(a.getUploadTimestamp()),
                            a.getSize().map(Integer::longValue), a.getPreview(), Optional.empty(), a.getCaption(),
                            a.getWidth() == 0 ? Optional.empty() : Optional.of(a.getWidth()),
                            a.getHeight() == 0 ? Optional.empty() : Optional.of(a.getHeight()), a.getVoiceNote(),
                            a.isGif(), a.isBorderless());
                } else {
                    final var a = attachment.asStream();
                    return new Attachment(Optional.empty(), Optional.empty(), a.getFileName(), a.getContentType(),
                            a.getUploadTimestamp() == 0 ? Optional.empty() : Optional.of(a.getUploadTimestamp()),
                            Optional.of(a.getLength()), a.getPreview(), Optional.empty(), a.getCaption(),
                            a.getWidth() == 0 ? Optional.empty() : Optional.of(a.getWidth()),
                            a.getHeight() == 0 ? Optional.empty() : Optional.of(a.getHeight()), a.getVoiceNote(),
                            a.isGif(), a.isBorderless());
                }
            }

            static Attachment from(SignalServiceDataMessage.Quote.QuotedAttachment a,
                    final AttachmentFileProvider fileProvider) {
                return new Attachment(Optional.empty(), Optional.empty(), Optional.ofNullable(a.getFileName()),
                        a.getContentType(), Optional.empty(), Optional.empty(), Optional.empty(),
                        a.getThumbnail() == null ? Optional.empty()
                                : Optional.of(Attachment.from(a.getThumbnail(), fileProvider)),
                        Optional.empty(), Optional.empty(), Optional.empty(), false, false, false);
            }
        }

        public static class Sticker {

            StickerPackId packId;
            byte[] packKey;
            int stickerId;

            public Sticker(StickerPackId packId, byte[] packKey, int stickerId) {
                super();
                this.packId = packId;
                this.packKey = packKey;
                this.stickerId = stickerId;
            }

            static Sticker from(SignalServiceDataMessage.Sticker sticker) {
                return new Sticker(StickerPackId.deserialize(sticker.getPackId()), sticker.getPackKey(),
                        sticker.getStickerId());
            }
        }

        public static class SharedContact {
            Name name;
            Optional<Avatar> avatar;
            List<Phone> phone;
            List<Email> email;
            List<Address> address;
            Optional<String> organization;

            public SharedContact(Name name, Optional<Avatar> avatar, List<Phone> phone, List<Email> email,
                    List<Address> address, Optional<String> organization) {
                super();
                this.name = name;
                this.avatar = avatar;
                this.phone = phone;
                this.email = email;
                this.address = address;
                this.organization = organization;
            }

            static SharedContact from(org.whispersystems.signalservice.api.messages.shared.SharedContact sharedContact,
                    final AttachmentFileProvider fileProvider) {
                return new SharedContact(Name.from(sharedContact.getName()),
                        sharedContact.getAvatar().map(avatar1 -> Avatar.from(avatar1, fileProvider)),
                        sharedContact.getPhone().map(p -> p.stream().map(Phone::from).collect(Collectors.toList()))
                                .orElse(List.of()),
                        sharedContact.getEmail().map(p -> p.stream().map(Email::from).collect(Collectors.toList()))
                                .orElse(List.of()),
                        sharedContact.getAddress().map(p -> p.stream().map(Address::from).collect(Collectors.toList()))
                                .orElse(List.of()),
                        sharedContact.getOrganization());
            }

            public static class Name {
                Optional<String> display;
                Optional<String> given;
                Optional<String> family;
                Optional<String> prefix;
                Optional<String> suffix;
                Optional<String> middle;

                public Name(Optional<String> display, Optional<String> given, Optional<String> family,
                        Optional<String> prefix, Optional<String> suffix, Optional<String> middle) {
                    super();
                    this.display = display;
                    this.given = given;
                    this.family = family;
                    this.prefix = prefix;
                    this.suffix = suffix;
                    this.middle = middle;
                }

                static Name from(org.whispersystems.signalservice.api.messages.shared.SharedContact.Name name) {
                    return new Name(name.getDisplay(), name.getGiven(), name.getFamily(), name.getPrefix(),
                            name.getSuffix(), name.getMiddle());
                }
            }

            public static class Avatar {

                Attachment attachment;
                boolean isProfile;

                public Avatar(Attachment attachment, boolean isProfile) {
                    super();
                    this.attachment = attachment;
                    this.isProfile = isProfile;
                }

                static Avatar from(org.whispersystems.signalservice.api.messages.shared.SharedContact.Avatar avatar,
                        final AttachmentFileProvider fileProvider) {
                    return new Avatar(Attachment.from(avatar.getAttachment(), fileProvider), avatar.isProfile());
                }
            }

            public static class Phone {
                String value;
                Type type;
                Optional<String> label;

                public Phone(String value, Type type, Optional<String> label) {
                    super();
                    this.value = value;
                    this.type = type;
                    this.label = label;
                }

                static Phone from(org.whispersystems.signalservice.api.messages.shared.SharedContact.Phone phone) {
                    return new Phone(phone.getValue(), Type.from(phone.getType()), phone.getLabel());
                }

                public enum Type {
                    HOME,
                    WORK,
                    MOBILE,
                    CUSTOM;

                    static Type from(
                            org.whispersystems.signalservice.api.messages.shared.SharedContact.Phone.Type type) {
                        switch (type) {
                            case HOME:
                                return HOME;
                            case WORK:
                                return WORK;
                            case MOBILE:
                                return MOBILE;
                            case CUSTOM:
                            default:
                                return CUSTOM;
                        }
                    }
                }
            }

            public static class Email {

                String value;
                Type type;
                Optional<String> label;

                public Email(String value, Type type, Optional<String> label) {
                    super();
                    this.value = value;
                    this.type = type;
                    this.label = label;
                }

                static Email from(org.whispersystems.signalservice.api.messages.shared.SharedContact.Email email) {
                    return new Email(email.getValue(), Type.from(email.getType()), email.getLabel());
                }

                public enum Type {
                    HOME,
                    WORK,
                    MOBILE,
                    CUSTOM;

                    static Type from(
                            org.whispersystems.signalservice.api.messages.shared.SharedContact.Email.Type type) {
                        switch (type) {
                            case HOME:
                                return HOME;
                            case WORK:
                                return WORK;
                            case MOBILE:
                                return MOBILE;
                            case CUSTOM:
                            default:
                                return CUSTOM;
                        }
                    }
                }
            }

            public static class Address {
                Type type;
                Optional<String> label;
                Optional<String> street;
                Optional<String> pobox;
                Optional<String> neighborhood;
                Optional<String> city;
                Optional<String> region;
                Optional<String> postcode;
                Optional<String> country;

                public Address(Type type, Optional<String> label, Optional<String> street, Optional<String> pobox,
                        Optional<String> neighborhood, Optional<String> city, Optional<String> region,
                        Optional<String> postcode, Optional<String> country) {
                    super();
                    this.type = type;
                    this.label = label;
                    this.street = street;
                    this.pobox = pobox;
                    this.neighborhood = neighborhood;
                    this.city = city;
                    this.region = region;
                    this.postcode = postcode;
                    this.country = country;
                }

                static Address from(
                        org.whispersystems.signalservice.api.messages.shared.SharedContact.PostalAddress address) {
                    return new Address(Address.Type.from(address.getType()), address.getLabel(), address.getLabel(),
                            address.getLabel(), address.getLabel(), address.getLabel(), address.getLabel(),
                            address.getLabel(), address.getLabel());
                }

                public enum Type {
                    HOME,
                    WORK,
                    CUSTOM;

                    static Type from(
                            org.whispersystems.signalservice.api.messages.shared.SharedContact.PostalAddress.Type type) {
                        switch (type) {
                            case HOME:
                                return HOME;
                            case WORK:
                                return WORK;
                            case CUSTOM:
                            default:
                                return CUSTOM;
                        }
                    }
                }
            }
        }

        public static class Preview {

            String title;
            String description;
            long date;
            String url;
            Optional<Attachment> image;

            public Preview(String title, String description, long date, String url, Optional<Attachment> image) {
                super();
                this.title = title;
                this.description = description;
                this.date = date;
                this.url = url;
                this.image = image;
            }

            static Preview from(SignalServicePreview preview, final AttachmentFileProvider fileProvider) {
                return new Preview(preview.getTitle(), preview.getDescription(), preview.getDate(), preview.getUrl(),
                        preview.getImage().map(as -> Attachment.from(as, fileProvider)));
            }
        }
    }

    public static class Sync {
        Optional<Sent> sent;
        Optional<Blocked> blocked;
        List<Read> read;
        List<Viewed> viewed;
        Optional<ViewOnceOpen> viewOnceOpen;
        Optional<Contacts> contacts;
        Optional<Groups> groups;
        Optional<MessageRequestResponse> messageRequestResponse;

        public Sync(Optional<Sent> sent, Optional<Blocked> blocked, List<Read> read, List<Viewed> viewed,
                Optional<ViewOnceOpen> viewOnceOpen, Optional<Contacts> contacts, Optional<Groups> groups,
                Optional<MessageRequestResponse> messageRequestResponse) {
            super();
            this.sent = sent;
            this.blocked = blocked;
            this.read = read;
            this.viewed = viewed;
            this.viewOnceOpen = viewOnceOpen;
            this.contacts = contacts;
            this.groups = groups;
            this.messageRequestResponse = messageRequestResponse;
        }

        public static Sync from(final SignalServiceSyncMessage syncMessage, RecipientResolver recipientResolver,
                RecipientAddressResolver addressResolver, final AttachmentFileProvider fileProvider) {
            return new Sync(
                    syncMessage.getSent().map(s -> Sent.from(s, recipientResolver, addressResolver, fileProvider)),
                    syncMessage.getBlockedList().map(b -> Blocked.from(b, recipientResolver, addressResolver)),
                    syncMessage.getRead()
                            .map(r -> r.stream().map(rm -> Read.from(rm, recipientResolver, addressResolver))
                                    .collect(Collectors.toList()))
                            .orElse(List.of()),
                    syncMessage.getViewed()
                            .map(r -> r.stream().map(rm -> Viewed.from(rm, recipientResolver, addressResolver))
                                    .collect(Collectors.toList()))
                            .orElse(List.of()),
                    syncMessage.getViewOnceOpen().map(rm -> ViewOnceOpen.from(rm, recipientResolver, addressResolver)),
                    syncMessage.getContacts().map(Contacts::from), syncMessage.getGroups().map(Groups::from),
                    syncMessage.getMessageRequestResponse()
                            .map(m -> MessageRequestResponse.from(m, recipientResolver, addressResolver)));
        }

        public static class Sent {
            long timestamp;
            long expirationStartTimestamp;
            Optional<RecipientAddress> destination;
            Set<RecipientAddress> recipients;
            Optional<Data> message;
            Optional<Story> story;

            public Sent(long timestamp, long expirationStartTimestamp, Optional<RecipientAddress> destination,
                    Set<RecipientAddress> recipients, Optional<Data> message, Optional<Story> story) {
                super();
                this.timestamp = timestamp;
                this.expirationStartTimestamp = expirationStartTimestamp;
                this.destination = destination;
                this.recipients = recipients;
                this.message = message;
                this.story = story;
            }

            static Sent from(SentTranscriptMessage sentMessage, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver, final AttachmentFileProvider fileProvider) {
                return new Sent(sentMessage.getTimestamp(), sentMessage.getExpirationStartTimestamp(),
                        sentMessage.getDestination()
                                .map(d -> addressResolver.resolveRecipientAddress(recipientResolver.resolveRecipient(d))
                                        .toApiRecipientAddress()),
                        sentMessage.getRecipients().stream()
                                .map(d -> addressResolver.resolveRecipientAddress(recipientResolver.resolveRecipient(d))
                                        .toApiRecipientAddress())
                                .collect(Collectors.toSet()),
                        sentMessage.getDataMessage()
                                .map(message -> Data.from(message, recipientResolver, addressResolver, fileProvider)),
                        sentMessage.getStoryMessage().map(s -> Story.from(s, fileProvider)));
            }
        }

        public static class Blocked {

            List<RecipientAddress> recipients;
            List<GroupId> groupIds;

            public Blocked(List<RecipientAddress> recipients, List<GroupId> groupIds) {
                super();
                this.recipients = recipients;
                this.groupIds = groupIds;
            }

            static Blocked from(BlockedListMessage blockedListMessage, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver) {
                return new Blocked(
                        blockedListMessage.getAddresses().stream()
                                .map(d -> addressResolver.resolveRecipientAddress(recipientResolver.resolveRecipient(d))
                                        .toApiRecipientAddress())
                                .collect(Collectors.toList()),
                        blockedListMessage.getGroupIds().stream().map(GroupId::unknownVersion)
                                .collect(Collectors.toList()));
            }
        }

        public static class Read {

            RecipientAddress sender;
            long timestamp;

            public Read(RecipientAddress sender, long timestamp) {
                super();
                this.sender = sender;
                this.timestamp = timestamp;
            }

            static Read from(ReadMessage readMessage, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver) {
                return new Read(addressResolver
                        .resolveRecipientAddress(recipientResolver.resolveRecipient(readMessage.getSender()))
                        .toApiRecipientAddress(), readMessage.getTimestamp());
            }
        }

        public static class Viewed {

            RecipientAddress sender;
            long timestamp;

            public Viewed(RecipientAddress sender, long timestamp) {
                super();
                this.sender = sender;
                this.timestamp = timestamp;
            }

            static Viewed from(ViewedMessage readMessage, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver) {
                return new Viewed(addressResolver
                        .resolveRecipientAddress(recipientResolver.resolveRecipient(readMessage.getSender()))
                        .toApiRecipientAddress(), readMessage.getTimestamp());
            }
        }

        public static class ViewOnceOpen {
            RecipientAddress sender;
            long timestamp;

            public ViewOnceOpen(RecipientAddress sender, long timestamp) {
                super();
                this.sender = sender;
                this.timestamp = timestamp;
            }

            static ViewOnceOpen from(ViewOnceOpenMessage readMessage, RecipientResolver recipientResolver,
                    RecipientAddressResolver addressResolver) {
                return new ViewOnceOpen(addressResolver
                        .resolveRecipientAddress(recipientResolver.resolveRecipient(readMessage.getSender()))
                        .toApiRecipientAddress(), readMessage.getTimestamp());
            }
        }

        public static class Contacts {
            boolean isComplete;

            public Contacts(boolean isComplete) {
                super();
                this.isComplete = isComplete;
            }

            static Contacts from(ContactsMessage contactsMessage) {
                return new Contacts(contactsMessage.isComplete());
            }
        }

        public static class Groups {

            static Groups from(SignalServiceAttachment groupsMessage) {
                return new Groups();
            }
        }

        public static class MessageRequestResponse {
            Type type;
            Optional<GroupId> groupId;
            Optional<RecipientAddress> person;

            public MessageRequestResponse(Type type, Optional<GroupId> groupId, Optional<RecipientAddress> person) {
                super();
                this.type = type;
                this.groupId = groupId;
                this.person = person;
            }

            static MessageRequestResponse from(MessageRequestResponseMessage messageRequestResponse,
                    RecipientResolver recipientResolver, RecipientAddressResolver addressResolver) {
                return new MessageRequestResponse(Type.from(messageRequestResponse.getType()),
                        messageRequestResponse.getGroupId().map(GroupId::unknownVersion),
                        messageRequestResponse.getPerson()
                                .map(p -> addressResolver.resolveRecipientAddress(recipientResolver.resolveRecipient(p))
                                        .toApiRecipientAddress()));
            }

            public enum Type {
                UNKNOWN,
                ACCEPT,
                DELETE,
                BLOCK,
                BLOCK_AND_DELETE,
                UNBLOCK_AND_ACCEPT;

                static Type from(MessageRequestResponseMessage.Type type) {
                    switch (type) {
                        case UNKNOWN:
                            return UNKNOWN;
                        case ACCEPT:
                            return ACCEPT;
                        case DELETE:
                            return DELETE;
                        case BLOCK:
                            return BLOCK;
                        case BLOCK_AND_DELETE:
                            return BLOCK_AND_DELETE;
                        case UNBLOCK_AND_ACCEPT:
                        default:
                            return UNBLOCK_AND_ACCEPT;
                    }
                }
            }
        }
    }

    public static class Call {
        Optional<Integer> destinationDeviceId;
        Optional<GroupId> groupId;
        Optional<Long> timestamp;
        Optional<Offer> offer;
        Optional<Answer> answer;
        Optional<Hangup> hangup;
        Optional<Busy> busy;
        List<IceUpdate> iceUpdate;
        Optional<Opaque> opaque;

        public Call(Optional<Integer> destinationDeviceId, Optional<GroupId> groupId, Optional<Long> timestamp,
                Optional<Offer> offer, Optional<Answer> answer, Optional<Hangup> hangup, Optional<Busy> busy,
                List<IceUpdate> iceUpdate, Optional<Opaque> opaque) {
            super();
            this.destinationDeviceId = destinationDeviceId;
            this.groupId = groupId;
            this.timestamp = timestamp;
            this.offer = offer;
            this.answer = answer;
            this.hangup = hangup;
            this.busy = busy;
            this.iceUpdate = iceUpdate;
            this.opaque = opaque;
        }

        public static Call from(final SignalServiceCallMessage callMessage) {
            return new Call(callMessage.getDestinationDeviceId(), callMessage.getGroupId().map(GroupId::unknownVersion),
                    callMessage.getTimestamp(), callMessage.getOfferMessage().map(Offer::from),
                    callMessage.getAnswerMessage().map(Answer::from), callMessage.getHangupMessage().map(Hangup::from),
                    callMessage.getBusyMessage().map(Busy::from),
                    callMessage.getIceUpdateMessages()
                            .map(m -> m.stream().map(IceUpdate::from).collect(Collectors.toList())).orElse(List.of()),
                    callMessage.getOpaqueMessage().map(Opaque::from));
        }

        public static class Offer {

            long id;
            String sdp;
            Type type;
            byte[] opaque;

            public Offer(long id, String sdp, Type type, byte[] opaque) {
                super();
                this.id = id;
                this.sdp = sdp;
                this.type = type;
                this.opaque = opaque;
            }

            static Offer from(OfferMessage offerMessage) {
                return new Offer(offerMessage.getId(), offerMessage.getSdp(), Type.from(offerMessage.getType()),
                        offerMessage.getOpaque());
            }

            public enum Type {
                AUDIO_CALL,
                VIDEO_CALL;

                static Type from(OfferMessage.Type type) {
                    switch (type) {
                        case AUDIO_CALL:
                            return AUDIO_CALL;
                        case VIDEO_CALL:
                        default:
                            return VIDEO_CALL;
                    }
                }
            }
        }

        public static class Answer {

            long id;
            String sdp;
            byte[] opaque;

            public Answer(long id, String sdp, byte[] opaque) {
                super();
                this.id = id;
                this.sdp = sdp;
                this.opaque = opaque;
            }

            static Answer from(AnswerMessage answerMessage) {
                return new Answer(answerMessage.getId(), answerMessage.getSdp(), answerMessage.getOpaque());
            }
        }

        public static class Busy {
            long id;

            public Busy(long id) {
                super();
                this.id = id;
            }

            static Busy from(BusyMessage busyMessage) {
                return new Busy(busyMessage.getId());
            }
        }

        public static class Hangup {
            long id;
            Type type;
            int deviceId;
            boolean isLegacy;

            public Hangup(long id, Type type, int deviceId, boolean isLegacy) {
                super();
                this.id = id;
                this.type = type;
                this.deviceId = deviceId;
                this.isLegacy = isLegacy;
            }

            static Hangup from(HangupMessage hangupMessage) {
                return new Hangup(hangupMessage.getId(), Type.from(hangupMessage.getType()),
                        hangupMessage.getDeviceId(), hangupMessage.isLegacy());
            }

            public enum Type {
                NORMAL,
                ACCEPTED,
                DECLINED,
                BUSY,
                NEED_PERMISSION;

                static Type from(HangupMessage.Type type) {
                    switch (type) {
                        case NORMAL:
                            return NORMAL;
                        case ACCEPTED:
                            return ACCEPTED;
                        case DECLINED:
                            return DECLINED;
                        case BUSY:
                            return BUSY;
                        case NEED_PERMISSION:
                        default:
                            return NEED_PERMISSION;
                    }
                }
            }
        }

        public static class IceUpdate {

            long id;
            String sdp;
            byte[] opaque;

            public IceUpdate(long id, String sdp, byte[] opaque) {
                super();
                this.id = id;
                this.sdp = sdp;
                this.opaque = opaque;
            }

            static IceUpdate from(IceUpdateMessage iceUpdateMessage) {
                return new IceUpdate(iceUpdateMessage.getId(), iceUpdateMessage.getSdp(), iceUpdateMessage.getOpaque());
            }
        }

        public static class Opaque {
            byte[] opaque;
            Urgency urgency;

            public Opaque(byte[] opaque, Urgency urgency) {
                super();
                this.opaque = opaque;
                this.urgency = urgency;
            }

            static Opaque from(OpaqueMessage opaqueMessage) {
                return new Opaque(opaqueMessage.getOpaque(), Urgency.from(opaqueMessage.getUrgency()));
            }

            public enum Urgency {
                DROPPABLE,
                HANDLE_IMMEDIATELY;

                static Urgency from(OpaqueMessage.Urgency urgency) {
                    switch (urgency) {
                        case DROPPABLE:
                            return DROPPABLE;
                        case HANDLE_IMMEDIATELY:
                        default:
                            return HANDLE_IMMEDIATELY;
                    }
                }
            }
        }
    }

    public static class Story {
        boolean allowsReplies;
        Optional<GroupId> groupId;
        Optional<Data.Attachment> fileAttachment;
        Optional<TextAttachment> textAttachment;

        public Story(boolean allowsReplies, Optional<GroupId> groupId, Optional<Attachment> fileAttachment,
                Optional<TextAttachment> textAttachment) {
            super();
            this.allowsReplies = allowsReplies;
            this.groupId = groupId;
            this.fileAttachment = fileAttachment;
            this.textAttachment = textAttachment;
        }

        public static Story from(SignalServiceStoryMessage storyMessage, final AttachmentFileProvider fileProvider) {
            return new Story(storyMessage.getAllowsReplies().orElse(false),
                    storyMessage.getGroupContext().map(c -> GroupUtils.getGroupIdV2(c.getMasterKey())),
                    storyMessage.getFileAttachment().map(f -> Data.Attachment.from(f, fileProvider)),
                    storyMessage.getTextAttachment().map(t -> TextAttachment.from(t, fileProvider)));
        }

        public static class TextAttachment {
            Optional<String> text;
            Optional<Style> style;
            Optional<Color> textForegroundColor;
            Optional<Color> textBackgroundColor;
            Optional<Data.Preview> preview;
            Optional<Gradient> backgroundGradient;
            Optional<Color> backgroundColor;

            public TextAttachment(Optional<String> text, Optional<Style> style, Optional<Color> textForegroundColor,
                    Optional<Color> textBackgroundColor, Optional<Data.Preview> preview,
                    Optional<Gradient> backgroundGradient, Optional<Color> backgroundColor) {
                super();
                this.text = text;
                this.style = style;
                this.textForegroundColor = textForegroundColor;
                this.textBackgroundColor = textBackgroundColor;
                this.preview = preview;
                this.backgroundGradient = backgroundGradient;
                this.backgroundColor = backgroundColor;
            }

            static TextAttachment from(SignalServiceTextAttachment textAttachment,
                    final AttachmentFileProvider fileProvider) {
                return new TextAttachment(textAttachment.getText(), textAttachment.getStyle().map(Style::from),
                        textAttachment.getTextForegroundColor().map(Color::new),
                        textAttachment.getTextBackgroundColor().map(Color::new),
                        textAttachment.getPreview().map(p -> Data.Preview.from(p, fileProvider)),
                        textAttachment.getBackgroundGradient().map(Gradient::from),
                        textAttachment.getBackgroundColor().map(Color::new));
            }

            public enum Style {
                DEFAULT,
                REGULAR,
                BOLD,
                SERIF,
                SCRIPT,
                CONDENSED;

                static Style from(SignalServiceTextAttachment.Style style) {
                    switch (style) {
                        case DEFAULT:
                            return DEFAULT;
                        case REGULAR:
                            return REGULAR;
                        case BOLD:
                            return BOLD;
                        case SERIF:
                            return SERIF;
                        case SCRIPT:
                            return SCRIPT;
                        case CONDENSED:
                        default:
                            return CONDENSED;
                    }
                }
            }

            public static class Gradient {

                List<Color> colors;
                List<Float> positions;
                Optional<Integer> angle;

                public Gradient(List<Color> colors, List<Float> positions, Optional<Integer> angle) {
                    super();
                    this.colors = colors;
                    this.positions = positions;
                    this.angle = angle;
                }

                static Gradient from(SignalServiceTextAttachment.Gradient gradient) {
                    return new Gradient(gradient.getColors().stream().map(Color::new).collect(Collectors.toList()),
                            gradient.getPositions(), gradient.getAngle());
                }
            }
        }
    }

    public static MessageEnvelope from(SignalServiceEnvelope envelope, SignalServiceContent content,
            RecipientResolver recipientResolver, RecipientAddressResolver addressResolver,
            final AttachmentFileProvider fileProvider, Exception exception) {
        final var source = !envelope.isUnidentifiedSender() && envelope.hasSourceUuid()
                ? recipientResolver.resolveRecipient(envelope.getSourceAddress())
                : envelope.isUnidentifiedSender() && content != null
                        ? recipientResolver.resolveRecipient(content.getSender())
                        : exception instanceof ProtocolException
                                ? recipientResolver.resolveRecipient(((ProtocolException) exception).getSender())
                                : null;
        final var sourceDevice = envelope.hasSourceDevice() ? envelope.getSourceDevice()
                : content != null ? content.getSenderDevice()
                        : exception instanceof ProtocolException ? ((ProtocolException) exception).getSenderDevice()
                                : 0;

        Optional<Receipt> receipt;
        Optional<Typing> typing;
        Optional<Data> data;
        Optional<Sync> sync;
        Optional<Call> call;
        Optional<Story> story;
        if (content != null) {
            receipt = content.getReceiptMessage().map(Receipt::from);
            typing = content.getTypingMessage().map(Typing::from);
            data = content.getDataMessage()
                    .map(dataMessage -> Data.from(dataMessage, recipientResolver, addressResolver, fileProvider));
            sync = content.getSyncMessage().map(s -> Sync.from(s, recipientResolver, addressResolver, fileProvider));
            call = content.getCallMessage().map(Call::from);
            story = content.getStoryMessage().map(s -> Story.from(s, fileProvider));
        } else {
            receipt = envelope.isReceipt() ? Optional.of(new Receipt(envelope.getServerReceivedTimestamp(),
                    Receipt.Type.DELIVERY, List.of(envelope.getTimestamp()))) : Optional.empty();
            typing = Optional.empty();
            data = Optional.empty();
            sync = Optional.empty();
            call = Optional.empty();
            story = Optional.empty();
        }

        return new MessageEnvelope(
                source == null ? Optional.empty()
                        : Optional.of(addressResolver.resolveRecipientAddress(source).toApiRecipientAddress()),
                sourceDevice, envelope.getTimestamp(), envelope.getServerReceivedTimestamp(),
                envelope.getServerDeliveredTimestamp(), envelope.isUnidentifiedSender(), receipt, typing, data, sync,
                call, story);
    }

    public interface AttachmentFileProvider {

        File getFile(SignalServiceAttachmentRemoteId attachmentRemoteId);
    }
}

package org.asamk.signal.manager.api;

import java.util.List;
import java.util.Optional;

public class Message {

    public String messageText;
    public List<String> attachments;
    public List<Mention> mentions;
    public Optional<Quote> quote;
    public Optional<Sticker> sticker;
    public List<Preview> previews;

    public static class Mention {

        public RecipientIdentifier.Single recipient;
        public int start;
        public int length;
    }

    public class Quote {
        public long timestamp;
        public RecipientIdentifier.Single author;
        public String message;
        public List<Mention> mentions;
    }

    public static class Sticker {
        public byte[] packId;
        public int stickerId;
    }

    public static class Preview {
        public String url;
        public String title;
        public String description;
        public Optional<String> image;
    }
}

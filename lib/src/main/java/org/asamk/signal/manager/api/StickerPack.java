package org.asamk.signal.manager.api;

import java.util.List;
import java.util.Optional;

public class StickerPack {
    StickerPackId packId;
    StickerPackUrl url;
    boolean installed;
    String title;
    String author;
    Optional<Sticker> cover;
    List<Sticker> stickers;

    public StickerPack(StickerPackId packId, StickerPackUrl url, boolean installed, String title, String author,
            Optional<Sticker> cover, List<Sticker> stickers) {
        super();
        this.packId = packId;
        this.url = url;
        this.installed = installed;
        this.title = title;
        this.author = author;
        this.cover = cover;
        this.stickers = stickers;
    }

    public StickerPack(final StickerPackId packId, final byte[] packKey, final boolean installed) {
        this(packId, new StickerPackUrl(packId, packKey), installed, "", "", Optional.empty(), List.of());
    }

    public static class Sticker {
        int id;
        String emoji;
        String contentType;

        public Sticker(int id, String emoji, String contentType) {
            super();
            this.id = id;
            this.emoji = emoji;
            this.contentType = contentType;
        }

    }
}

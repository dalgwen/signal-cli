package org.asamk.signal.manager.storage.stickerPacks;

import java.util.List;

import org.asamk.signal.manager.api.StickerPack;

public class JsonStickerPack {

    public String title;
    public String author;
    public JsonSticker cover;
    public List<JsonSticker> stickers;

    public JsonStickerPack(String title, String author, JsonSticker cover, List<JsonSticker> stickers) {
        super();
        this.title = title;
        this.author = author;
        this.cover = cover;
        this.stickers = stickers;
    }

    public static class JsonSticker {
        public Integer id;
        public String emoji;
        public String file;
        public String contentType;

        public JsonSticker(Integer id, String emoji, String file, String contentType) {
            super();
            this.id = id;
            this.emoji = emoji;
            this.file = file;
            this.contentType = contentType;
        }

        public StickerPack.Sticker toApi() {
            return new StickerPack.Sticker(id == null ? Integer.parseInt(file) : id, emoji, contentType);
        }
    }
}

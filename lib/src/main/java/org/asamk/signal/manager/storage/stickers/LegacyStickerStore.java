package org.asamk.signal.manager.storage.stickers;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.asamk.signal.manager.api.StickerPackId;

public class LegacyStickerStore {

    public static void migrate(Storage storage, StickerStore stickerStore) {
        final var packIds = new HashSet<StickerPackId>();
        final var stickers = storage.stickers.stream().map(s -> {
            var packId = StickerPackId.deserialize(Base64.getDecoder().decode(s.packId));
            if (packIds.contains(packId)) {
                // Remove legacy duplicate packIds ...
                return null;
            }
            packIds.add(packId);
            var packKey = Base64.getDecoder().decode(s.packKey);
            var installed = s.installed;
            return new StickerPack(-1, packId, packKey, installed);
        }).filter(Objects::nonNull).collect(Collectors.toList());

        stickerStore.addLegacyStickers(stickers);
    }

    public static class Storage {

        List<Sticker> stickers;

        public Storage(List<Sticker> stickers) {
            super();
            this.stickers = stickers;
        }

        private static class Sticker {
            String packId;
            String packKey;
            boolean installed;

            public Sticker(String packId, String packKey, boolean installed) {
                super();
                this.packId = packId;
                this.packKey = packKey;
                this.installed = installed;
            }

        }
    }
}

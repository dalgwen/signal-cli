package org.asamk.signal.manager.storage.stickers;

import org.asamk.signal.manager.api.StickerPackId;

public class StickerPack {

    public long internalId;
    public StickerPackId packId;
    public byte[] packKey;
    public boolean isInstalled;

    public StickerPack(long internalId, StickerPackId packId, byte[] packKey, boolean isInstalled) {
        super();
        this.internalId = internalId;
        this.packId = packId;
        this.packKey = packKey;
        this.isInstalled = isInstalled;
    }

    public StickerPack(final StickerPackId packId, final byte[] packKey) {
        this(-1, packId, packKey, false);
    }
}

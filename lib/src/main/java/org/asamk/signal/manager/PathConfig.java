package org.asamk.signal.manager;

import java.io.File;

public class PathConfig {

    File dataPath;
    File attachmentsPath;
    File avatarsPath;
    File stickerPacksPath;

    public PathConfig(File dataPath, File attachmentsPath, File avatarsPath, File stickerPacksPath) {
        super();
        this.dataPath = dataPath;
        this.attachmentsPath = attachmentsPath;
        this.avatarsPath = avatarsPath;
        this.stickerPacksPath = stickerPacksPath;
    }

    static PathConfig createDefault(final File settingsPath) {
        return new PathConfig(new File(settingsPath, "data"), new File(settingsPath, "attachments"),
                new File(settingsPath, "avatars"), new File(settingsPath, "stickers"));
    }
}

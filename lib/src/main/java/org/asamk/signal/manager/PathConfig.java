package org.asamk.signal.manager;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class PathConfig {

    private final File dataPath;
    private final File attachmentsPath;
    private final File avatarsPath;
    private final File stickerPacksPath;

    public PathConfig(@JsonProperty("dataPath") File dataPath, @JsonProperty("attachmentsPath") File attachmentsPath, @JsonProperty("avatarsPath") File avatarsPath, @JsonProperty("stickerPacksPath") File stickerPacksPath) {
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

    public File dataPath() {
        return dataPath;
    }

    public File attachmentsPath() {
        return attachmentsPath;
    }

    public File avatarsPath() {
        return avatarsPath;
    }

    public File stickerPacksPath() {
        return stickerPacksPath;
    }
}

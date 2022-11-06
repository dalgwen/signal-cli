package org.asamk.signal.manager.api;

import java.util.UUID;

public class UserStatus {
    String number;
    UUID uuid;
    boolean unrestrictedUnidentifiedAccess;

    public UserStatus(String number, UUID uuid, boolean unrestrictedUnidentifiedAccess) {
        super();
        this.number = number;
        this.uuid = uuid;
        this.unrestrictedUnidentifiedAccess = unrestrictedUnidentifiedAccess;
    }

}

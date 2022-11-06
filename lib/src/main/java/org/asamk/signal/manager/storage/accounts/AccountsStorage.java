package org.asamk.signal.manager.storage.accounts;

import java.util.List;

public class AccountsStorage {

    List<Account> accounts;
    Integer version;

    public AccountsStorage(List<Account> accounts, Integer version) {
        super();
        this.accounts = accounts;
        this.version = version;
    }

    public static class Account {
        public String path;
        public String environment;
        public String number;
        public String uuid;

        public Account(String path, String environment, String number, String uuid) {
            super();
            this.path = path;
            this.environment = environment;
            this.number = number;
            this.uuid = uuid;
        }

    }
}

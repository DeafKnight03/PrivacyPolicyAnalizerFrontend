package com.myapp.client.util;

import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class RefreshStore {
    private static final String NODE = "com.example.myapp.auth";
    private static final String KEY  = "refreshToken";
    private static final Preferences PREFS = Preferences.userRoot().node(NODE);

    private RefreshStore() {}

    public static synchronized void save(String refreshToken) {
        PREFS.put(KEY, refreshToken); // se vuoi: Base64.encode per sicurezza da caratteri speciali
        try { PREFS.flush(); } catch (BackingStoreException ignored) {}
    }

    public static synchronized Optional<String> load() {
        String v = PREFS.get(KEY, null);
        return Optional.ofNullable(v);
    }

    public static synchronized void clear() {
        PREFS.remove(KEY);
        try { PREFS.flush(); } catch (BackingStoreException ignored) {}
    }
}

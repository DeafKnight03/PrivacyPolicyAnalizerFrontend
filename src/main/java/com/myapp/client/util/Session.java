package com.myapp.client.util;

public final class Session {
    private static volatile Session INSTANCE;   // single static reference
    private String accessToken;                 // your JWT

    private Session() {}                        // private constructor = no "new" outside

    // Thread-safe lazy init with double-checked locking
    public static Session get() {
        if (INSTANCE == null) {
            synchronized (Session.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Session();
                }
            }
        }
        return INSTANCE;
    }

    public void setAccessToken(String token) { this.accessToken = token; }
    public String getAccessToken() { return accessToken; }
    public void clear() { accessToken = null; }
}

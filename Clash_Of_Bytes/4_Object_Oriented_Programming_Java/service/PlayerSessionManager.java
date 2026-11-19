package service;

import model.PlayerSession;

/**
 * Thread-safe manager maintaining separate in-memory authentication sessions
 * for Player 1 and Player 2 on the SAME computer.
 *
 * Enforces:
 * 1. Independent authentication sessions (player1Session and player2Session).
 * 2. Independent logout (logging out Player 1 NEVER affects Player 2).
 * 3. Prevention of the same Supabase account (UUID) being logged in as both players simultaneously.
 */
public class PlayerSessionManager {

    private static final PlayerSessionManager INSTANCE = new PlayerSessionManager();

    private volatile PlayerSession player1Session = null;
    private volatile PlayerSession player2Session = null;

    private PlayerSessionManager() {}

    public static PlayerSessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Sets Player 1 session after authenticating.
     * Throws IllegalArgumentException if the account is already active as Player 2.
     */
    public synchronized void setPlayer1Session(PlayerSession session) {
        if (session != null && player2Session != null && session.getUserId().equalsIgnoreCase(player2Session.getUserId())) {
            throw new IllegalArgumentException("Account '" + session.getUsername() + "' is already authenticated as Player 2! Two distinct accounts are required.");
        }
        this.player1Session = session;
    }

    public synchronized PlayerSession getPlayer1Session() {
        return player1Session;
    }

    public synchronized void clearPlayer1Session() {
        this.player1Session = null;
    }

    /**
     * Sets Player 2 session after authenticating.
     * Throws IllegalArgumentException if the account is already active as Player 1.
     */
    public synchronized void setPlayer2Session(PlayerSession session) {
        if (session != null && player1Session != null && session.getUserId().equalsIgnoreCase(player1Session.getUserId())) {
            throw new IllegalArgumentException("Account '" + session.getUsername() + "' is already authenticated as Player 1! Two distinct accounts are required.");
        }
        this.player2Session = session;
    }

    public synchronized PlayerSession getPlayer2Session() {
        return player2Session;
    }

    public synchronized void clearPlayer2Session() {
        this.player2Session = null;
    }

    /**
     * True only if both distinct players are currently authenticated and active.
     */
    public synchronized boolean isBothLoggedIn() {
        return player1Session != null && player2Session != null;
    }

    /**
     * Clears both sessions upon full application reset.
     */
    public synchronized void clearAll() {
        this.player1Session = null;
        this.player2Session = null;
    }
}

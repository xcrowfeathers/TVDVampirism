package org.kuro.tvdvampirism.player;

/** Narrow access to Vampirism's private DBNO setter, supplied by its Mixin. */
public interface DbnoAccess {
    void tvd$setDbnoTimer(int ticks);
}

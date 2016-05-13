package org.nukkitmc.plugin;

/**
 * Created by Snake1999 on 2016/5/13.
 * Package org.nukkitmc.plugin in project nukkit.
 */
public interface Plugin {

    default void onLoad() {}

    default void onEnable() {}

    default void onDisable() {}
}

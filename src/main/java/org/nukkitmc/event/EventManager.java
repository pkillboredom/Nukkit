package org.nukkitmc.event;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.event in project nukkit.
 */
public interface EventManager {

    void addListener(VanillaListener listener);

    void removeListener(VanillaListener listener);

    void processEvent(VanillaEvent event);
}

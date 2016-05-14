package org.nukkitmc.event;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.event in project nukkit.
 *
 * TODO 2016/5/14 REMOVE THIS: THIS IS A TEST LISTENER INTERFACE FOR EVENT SYSTEM
 */
public interface ConsoleInputListener extends VanillaListener {

    default void onConsoleInput(ConsoleInputEvent event){}
}

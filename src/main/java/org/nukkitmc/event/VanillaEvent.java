package org.nukkitmc.event;

import java.util.EventObject;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.event in project nukkit.
 */
abstract class VanillaEvent extends EventObject {

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public VanillaEvent(Object source) {
        super(source);
    }
}

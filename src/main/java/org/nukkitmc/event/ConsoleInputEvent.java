package org.nukkitmc.event;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.event in project nukkit.
 *
 * TODO 2016/5/14 REMOVE THIS: THIS IS A TEST LISTENER EVENT FOR EVENT SYSTEM
 */
public class ConsoleInputEvent extends VanillaEvent {

    String input;

    public ConsoleInputEvent(Object source, String input) {
        super(source);
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}

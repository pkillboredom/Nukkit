package org.nukkitmc.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.event in project nukkit.
 */
public class SimpleEventManager implements EventManager {

    private List<VanillaListener> listeners = new LinkedList<>();

    @Override
    public void addListener(VanillaListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public void removeListener(VanillaListener listener) {
        if (listeners.contains(listener)) listeners.remove(listener);
    }

    @Override
    public void processEvent(VanillaEvent event) {
        for (VanillaListener listener: listeners) {
            for (Method m: listener.getClass().getMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (!Objects.equals(m.getParameters()[0].getParameterizedType().getTypeName(), event.getClass().getTypeName())) continue;
                if (Modifier.isStatic(m.getModifiers())) continue;
                if (!m.isAccessible()) m.setAccessible(true);
                try {
                    m.invoke(listener, event);
                } catch (IllegalAccessException | InvocationTargetException ignore) {}
            }
        }
    }

}

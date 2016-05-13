package org.nukkitmc.module;

/**
 * Created by Snake1999 on 2016/5/13.
 * Package org.nukkitmc.module in project Nukkit.
 */
public interface Module {

    ModuleInfo getModuleInfo();

    void load();

    void unload();
}

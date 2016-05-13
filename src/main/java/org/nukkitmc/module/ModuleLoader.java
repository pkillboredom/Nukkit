package org.nukkitmc.module;

/**
 * Created by Snake1999 on 2016/5/13.
 * Package org.nukkitmc.module in project Nukkit.
 */
public interface ModuleLoader {

    ModuleInfo[] getModuleList();

    Module loadModule(ModuleInfo info); //// TODO: 2016/5/13 throws ....

    void unloadModule(ModuleInfo info); //// TODO: 2016/5/13 throws ....
}

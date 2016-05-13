package org.nukkitmc.module;

import java.util.*;

/**
 * Created by Snake1999 on 2016/5/13.
 * Package org.nukkitmc.module in project Nukkit.
 */
public class SimpleModuleManager implements ModuleManager {

    List<ModuleLoader> loaders = new ArrayList<>();

    @Override
    public void addLoader(ModuleLoader loader) {
        loaders.add(loader);
    }

    @Override
    public ModuleLoader[] getAllLoaders() {
        return loaders.toArray(new ModuleLoader[loaders.size()]);
    }

    @Override
    public Module getModule(ModuleInfo info) {
        final Module[] ans = {null};
        loaders.forEach((l) -> {
            if (ans[0] != null) return;
            ans[0] = l.loadModule(info);
        });
        return ans[0];
    }

    @Override
    public ModuleInfo[] getModuleList() {
        List<ModuleInfo> allModules = new LinkedList<>();
        loaders.forEach((l) -> Collections.addAll(allModules, l.getModuleList()));
        return allModules.toArray(new ModuleInfo[allModules.size()]);
    }
}

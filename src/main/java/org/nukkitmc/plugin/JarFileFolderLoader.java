package org.nukkitmc.plugin;

import org.nukkitmc.module.Module;
import org.nukkitmc.module.ModuleInfo;
import org.nukkitmc.module.ModuleLoader;
import org.nukkitmc.module.ModuleManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.plugin in project nukkit.
 */
abstract class JarFileFolderLoader implements ModuleLoader {

    private File folder;
    private ModuleManager manager;
    protected Map<ModuleInfo, ClassLoader> classLoaders = new HashMap<>();
    protected Map<ModuleInfo, Module> loaded = new HashMap<>();
    protected Map<ModuleInfo, File> files = new HashMap<>();

    JarFileFolderLoader(ModuleManager manager, File folder) {
        this.manager = manager;
        this.folder = folder;
    }

    @Override
    public final ModuleInfo[] getModuleList() {
        if (!folder.exists()) {
            boolean v = folder.mkdir();
            if (!v) return new ModuleInfo[0];
        }
        if (!folder.isDirectory()) return new ModuleInfo[0];
        File[] listFiles = folder.listFiles();
        if (listFiles == null) return new ModuleInfo[0];
        List<ModuleInfo> listInfo = new ArrayList<>();
        for (File aFile: listFiles) {
            if (aFile.isDirectory()) continue;
            if (!aFile.getName().endsWith(".jar")) continue;
            Collections.addAll(listInfo, acceptFile(aFile));
            for (ModuleInfo info: listInfo) files.put(info, aFile);
        }
        return listInfo.toArray(new ModuleInfo[listFiles.length]);
    }

    @Override
    public final Module loadModule(ModuleInfo info) {
        if (info == null) return null;
        if (loaded.containsKey(info)) return loaded.get(info);
        if (!files.keySet().contains(info)) return null;
        File file = files.get(info);
        if (file == null) return null;
        if (!file.exists()) return null;
        ClassLoader cl = initClassLoader(file);
        if (classLoaders.keySet().contains(info)) {
            // unload all classes by set ClassLoader null
            classLoaders.put(info, null);
        }
        classLoaders.put(info, cl);
        if (info.getDepends() != null) if (info.getDepends().length != 0) for (ModuleInfo depend: info.getDepends()){
            final ModuleInfo[] found = {null};
            Arrays.asList(manager.getModuleList()).forEach((i) -> {
                if (found[0] != null) return;
                if (Objects.equals(i, depend)) found[0] = i;
            });
            if (found[0] != null) manager.getModule(found[0]);
        }
        Module ans = acceptJavaModuleLoad(info, file, cl);
        return ans == null?null:ans;
    }

    @Override
    public final void unloadModule(ModuleInfo info) {
        acceptJavaModuleUnload(info, files.get(info));
        loaded.put(info, null);
        classLoaders.put(info, null);
    }

    protected abstract ModuleInfo[] acceptFile(File file);

    protected abstract Module acceptJavaModuleLoad(ModuleInfo info, File file, ClassLoader cl);

    protected abstract void acceptJavaModuleUnload(ModuleInfo info, File file);

    protected ClassLoader initClassLoader(File file) {
        ClassLoader cl = null;
        try {
            cl = new JarFileClassLoader(file, this.getClass().getClassLoader());
        } catch (MalformedURLException ignore) {}
        return cl;
    }

    private class JarFileClassLoader extends URLClassLoader {

        public JarFileClassLoader(File file, ClassLoader parent) throws MalformedURLException {
            super(new URL[]{file.toURI().toURL()}, parent);
        }

    }

    protected abstract class SimpleModuleInfo implements ModuleInfo {
        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof ModuleInfo)) return false;
            ModuleInfo target = (ModuleInfo) obj;
            return Objects.equals(getName(), target.getName())
                    && Objects.equals(getVersion(), target.getVersion());
        }
    }

}

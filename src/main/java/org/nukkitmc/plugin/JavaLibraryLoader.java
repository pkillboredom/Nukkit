package org.nukkitmc.plugin;

import org.nukkitmc.module.Module;
import org.nukkitmc.module.ModuleInfo;
import org.nukkitmc.module.ModuleLoader;
import org.nukkitmc.module.ModuleManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by Snake1999 on 2016/5/14.
 * Package org.nukkitmc.library in project nukkit.
 */
public class JavaLibraryLoader extends JarFileFolderLoader {

    public JavaLibraryLoader(ModuleManager moduleManager) {
        super(moduleManager, new File(System.getProperty("user.dir"), "libraries"));
    }


    /******************* Internal Part *********************/

    @Override
    protected ModuleInfo[] acceptFile(File file) {
        ModuleInfo info = new JavaLibraryInfo(file.getName());
        files.put(info, file);
        return new ModuleInfo[]{info};
    }

    @Override
    protected Module acceptJavaModuleLoad(ModuleInfo info, File file, ClassLoader cl) {
        if (!(info instanceof JavaLibraryInfo)) return null;
        Module m = new JavaLibraryModule((JavaLibraryInfo) info, this);
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            boolean accessible = method.isAccessible();
            if (!accessible) {
                method.setAccessible(true);
            }
            URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            URL url = file.toURI().toURL();
            method.invoke(classLoader, url);
            method.setAccessible(accessible);
        } catch (Exception ignore){}
        loaded.put(info, m);
        return m;
    }

    @Override
    protected void acceptJavaModuleUnload(ModuleInfo info, File file) {
        // TODO: 2016/5/14 check depend
    }

    class JavaLibraryModule implements Module {
        JavaLibraryInfo info;
        ModuleLoader loader;

        JavaLibraryModule(JavaLibraryInfo info, ModuleLoader loader) {
            this.info = info;
            this.loader = loader;
        }

        @Override
        public ModuleLoader getLoader() { return loader; }

        @Override
        public ModuleInfo getModuleInfo() { return info; }

        @Override
        public void load() {}

        @Override
        public void unload() {}

        @Override
        public String toString() {
            return "JavaLibraryModule Name: `"+info.getName()+"` Version: `"+info.getVersion()+
                    "`  Loader: "+loader.toString();
        }
    }

    class JavaLibraryInfo extends SimpleModuleInfo {
        String name;
        String version;

        JavaLibraryInfo(String jarFileName) {
            name = jarFileName.replaceAll("^(.*)(-\\d+(\\.\\d+){0,2})(.*)$", "$1");
            version = jarFileName.replaceAll("^.*-(\\d+(\\.\\d+){0,2}).*$", "$1");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() { return version; }

        @Override
        public ModuleInfo[] getDepends() {
            return new ModuleInfo[0];  // TODO: 2016/5/14 ?
        }

        @Override
        public String toString() {
            return "JavaLibraryInfo Name: `"+name+"` Version: `"+version+"`";
        }
    }


}

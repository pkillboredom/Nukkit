package org.nukkitmc.plugin;

import org.nukkitmc.configuration.Configuration;
import org.nukkitmc.configuration.file.YamlConfiguration;
import org.nukkitmc.module.Module;
import org.nukkitmc.module.ModuleInfo;
import org.nukkitmc.module.ModuleLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Created by Snake1999 on 2016/5/13.
 * Package org.nukkitmc.plugin in project Nukkit.
 *
 * A simple hot-swap-supported plugin loader
 * for plugins as jar file in '/plugins' folder and a 'plugin.yml' inside.
 */
public class JavaPluginLoader implements ModuleLoader {

    private File pluginFolder = new File(System.getProperty("user.dir"), "plugins");
    private Map<ModuleInfo, ClassLoader> classLoaders = new HashMap<>();
    private Map<ModuleInfo, File> files = new HashMap<>();
    private Map<ModuleInfo, JavaPluginModule> loaded = new HashMap<>();

    @Override
    public ModuleInfo[] getModuleList() {
        if (!pluginFolder.exists()) {
            boolean v = pluginFolder.mkdir();
            if (!v) return new ModuleInfo[0];
        }
        if (!pluginFolder.isDirectory()) return new ModuleInfo[0];
        File[] listFiles = pluginFolder.listFiles();
        if (listFiles == null) return new ModuleInfo[0];
        List<ModuleInfo> listInfo = new ArrayList<>();
        files.clear();
        for (File aFile : listFiles) {
            if (aFile.isDirectory()) continue;
            if (!aFile.getName().endsWith(".jar")) continue;
            ModuleInfo info;
            Configuration config;
            try {
                config = readPluginYaml(aFile);
            } catch (IOException e) {
                // TODO: 2016/5/13 Exception log
                continue;
            }
            if (config == null) continue;
            String name = config.getString("name");
            String version = config.getString("version");
            info = new JavaPluginInfo(name, version, null); //// TODO: 2016/5/13 depends
            listInfo.add(info);
            files.put(info, aFile);
        }
        return listInfo.toArray(new ModuleInfo[listInfo.size()]);
    }

    @Override
    public Module loadModule(ModuleInfo info) {
        if (loaded.containsKey(info)) return loaded.get(info);
        if (!files.keySet().contains(info)) return dummyJavaPlugin();
        File file = files.get(info);
        if (file == null) return dummyJavaPlugin();
        if (!file.exists()) return dummyJavaPlugin();
        ClassLoader cl = initClassLoader(file);
        if (classLoaders.keySet().contains(info)) {
            // unload all classes by set ClassLoader null
            classLoaders.put(info, null);
        }
        classLoaders.put(info, cl);
        Configuration config;
        try {
            config = readPluginYaml(file);
        } catch (IOException e) {
            // TODO: 2016/5/13 Exception log
            return dummyJavaPlugin();
        }
        if (config == null) return dummyJavaPlugin();
        String mainClassName = config.getString("main");
        try {
            Class<? extends Plugin> pluginClass = cl.loadClass(mainClassName).asSubclass(Plugin.class);
            Plugin plugin = pluginClass.newInstance();
            JavaPluginModule module = new JavaPluginModule(info, plugin, this);
            loaded.put(info, module);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
            //// TODO: 2016/5/13 Exception log
            return dummyJavaPlugin();
        }
    }

    @Override
    public void unloadModule(ModuleInfo info) {
        // TODO: 2016/5/14 check depend
        loaded.put(info, null);
        classLoaders.put(info, null);
    }

    /******************** Internal Part ********************/

    private ClassLoader initClassLoader(File file) {
        ClassLoader cl = null;
        try {
            cl = new JavaPluginClassLoader(file, this.getClass().getClassLoader());
        } catch (MalformedURLException ignore) {}
        return cl;
    }

    private Configuration readPluginYaml(File file) throws IOException {
        JarFile jarFile = new JarFile(file);
        JarEntry jarEntry = jarFile.getJarEntry("plugin.yml");
        if (jarEntry == null) return null;
        InputStream is = jarFile.getInputStream(jarEntry);
        InputStreamReader reader = new InputStreamReader(is);
        return YamlConfiguration.loadConfiguration(reader);
    }

    DummyJavaPlugin dummyJavaPlugin = new DummyJavaPlugin(this);
    private DummyJavaPlugin dummyJavaPlugin() {
        return dummyJavaPlugin;
    }

    // A wrapper that wraps a plugin into a module
    private class JavaPluginModule implements Module {
        ModuleInfo info;
        Plugin plugin;
        ModuleLoader loader;
        JavaPluginModule(ModuleInfo info, Plugin plugin, ModuleLoader loader) {
            this.info = info;
            this.plugin = plugin;
            this.loader = loader;
        }

        @Override
        public ModuleLoader getLoader() {
            return loader;
        }

        @Override
        public ModuleInfo getModuleInfo() { return info; }

        @Override
        public void load() {

        }

        @Override
        public void unload() {
            //// TODO: 2016/5/13 unload all plugin constants
            plugin = null;
            getLoader().unloadModule(this.getModuleInfo());
        }

        @Override
        public String toString() {
            return "JavaPluginModule "+info.toString()+"  Loader: "+loader.toString()+" Plugin: "+plugin.toString();
        }
    }

    private class DummyJavaPlugin implements Module {
        ModuleLoader loader;
        DummyJavaPlugin(ModuleLoader loader) {
            this.loader = loader;
        }
        @Override
        public ModuleLoader getLoader() { return null; }

        @Override
        public ModuleInfo getModuleInfo() { return new JavaPluginInfo(null, null, null); }

        @Override
        public void load() {}

        @Override
        public void unload() {}
    }

    private class JavaPluginClassLoader extends URLClassLoader {
        // Class names like:  org.nukkitmc.*   net.nukkitmc.*   com.nukkitmc.*   net.minecraft.*
        // is disallowed in a java plugin
        Pattern disallowedName = Pattern.compile("(^(com|org|net)\\.nukkitmc\\.|^net\\.minecraft\\.)");

        public JavaPluginClassLoader(File file, ClassLoader parent) throws MalformedURLException {
            super(new URL[]{file.toURI().toURL()}, parent);
        }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (disallowedName.matcher(name).matches())
                throw new ClassNotFoundException("Class name `"+name+"` is disallowed in a java plugin.");
            return super.findClass(name);
        }
    }

    private class JavaPluginInfo implements ModuleInfo {
        String name;
        String version;
        ModuleInfo[] depends;
        JavaPluginInfo (String name, String version, ModuleInfo[] depends) {
            this.name = name;
            this.version = version;
            this.depends = depends;
        }
        @Override
        public String getName() { return name; }

        @Override
        public String getVersion() { return version; }

        @Override
        public ModuleInfo[] getDepends() { return depends ;}

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof ModuleInfo)) return false;
            ModuleInfo target = (ModuleInfo) obj;
            return Objects.equals(getName(), target.getName())
                    && Objects.equals(getVersion(), target.getVersion());
        }

        @Override
        public String toString() {
            return "Name: `"+name +"` Version: `"+version+"`";
        }
    }

}

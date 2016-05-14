package org.nukkitmc.plugin;

import org.nukkitmc.configuration.Configuration;
import org.nukkitmc.configuration.file.YamlConfiguration;
import org.nukkitmc.module.Module;
import org.nukkitmc.module.ModuleInfo;
import org.nukkitmc.module.ModuleLoader;
import org.nukkitmc.module.ModuleManager;

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
public class JavaPluginLoader extends JarFileFolderLoader {

    // Class names like:  org.nukkitmc.*   net.nukkitmc.*   com.nukkitmc.*   net.minecraft.*
    // is disallowed in a java plugin
    private Pattern disallowedName = Pattern.compile("(^(com|org|net)\\.nukkitmc\\.|^net\\.minecraft\\.)");

    public JavaPluginLoader(ModuleManager manager) {
        super(manager, new File(System.getProperty("user.dir"), "plugins"));
    }

    /******************** Internal Part ********************/

    @Override
    protected ModuleInfo[] acceptFile(File aFile) {
        ModuleInfo info;
        Configuration config;
        try {
            config = readPluginYaml(aFile);
        } catch (IOException e) {
            // TODO: 2016/5/13 Exception log
            return new ModuleInfo[0];
        }
        if (config == null) return new ModuleInfo[0];
        String name = config.getString("name");
        String version = config.getString("version");
        @SuppressWarnings("unchecked")
        Map<String, Object> depends = config.getConfigurationSection("depend").getValues(false);
        List<ModuleInfo> dependList = new ArrayList<>();
        depends.forEach((n, v) -> dependList.add(new ModuleInfo() {
            @Override
            public String getName() { return n; }
            @Override
            public String getVersion() { return String.valueOf(v); }
            @Override
            public ModuleInfo[] getDepends() { return new ModuleInfo[0]; }
        }));
        info = new JavaPluginInfo(name, version, dependList.toArray(new ModuleInfo[dependList.size()]));
        return new ModuleInfo[]{info};
    }

    @Override
    protected Module acceptJavaModuleLoad(ModuleInfo info, File file, ClassLoader cl) {
        Configuration config;
        try {
            config = readPluginYaml(file);
        } catch (IOException e) {
            // TODO: 2016/5/13 Exception log
            return null;
        }
        if (config == null) return null;
        String mainClassName = config.getString("main");
        if (disallowedName.matcher(mainClassName).matches()) return null;
        //todo throw new ClassNotFoundException("Class name `"+mainClassName+"` is disallowed in a java plugin.");
        try {
            Class<? extends Plugin> pluginClass = cl.loadClass(mainClassName).asSubclass(Plugin.class);
            Plugin plugin = pluginClass.newInstance();
            JavaPluginModule module = new JavaPluginModule(info, plugin, this);
            loaded.put(info, module);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
            //// TODO: 2016/5/13 Exception log
            return null;
        }
    }

    @Override
    protected void acceptJavaModuleUnload(ModuleInfo info, File file) {
        // TODO: 2016/5/14 check depend
    }

    private Configuration readPluginYaml(File file) throws IOException {
        JarFile jarFile = new JarFile(file);
        JarEntry jarEntry = jarFile.getJarEntry("plugin.yml");
        if (jarEntry == null) return null;
        InputStream is = jarFile.getInputStream(jarEntry);
        InputStreamReader reader = new InputStreamReader(is);
        return YamlConfiguration.loadConfiguration(reader);
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
            return "JavaPluginModule Name: `"+info.getName()+"` Version: `"+info.getVersion()+
                    "`  Loader: "+loader.toString()+" Plugin: "+plugin.toString();
        }
    }

    private class JavaPluginInfo extends SimpleModuleInfo {
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
        public String toString() {
            return "JavaPluginInfo Name: `"+name +"` Version: `"+version+"`";
        }
    }

}

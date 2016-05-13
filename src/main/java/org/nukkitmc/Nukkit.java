package org.nukkitmc;

import jline.console.ConsoleReader;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;
import org.nukkitmc.configuration.file.YamlConfiguration;
import org.nukkitmc.language.Language;
import org.nukkitmc.module.Module;
import org.nukkitmc.module.ModuleInfo;
import org.nukkitmc.module.ModuleManager;
import org.nukkitmc.module.SimpleModuleManager;
import org.nukkitmc.plugin.JavaPluginLoader;
import org.nukkitmc.util.Color;
import org.nukkitmc.util.ConsoleWriter;
import org.nukkitmc.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class Nukkit {

    public static boolean useJline = true;
    public static boolean useConsole = true;

    public static void main(String[] args) {

        OptionParser parser = new OptionParser() {
            {
                acceptsAll(asList("?", "help"), "Show the help");

                //TODO: SERVER CONFIGURATION ARGUMENTS

                acceptsAll(asList("nojline"), "Disables jline and emulates the vanilla console");

                acceptsAll(asList("noconsole"), "Disables the console");

                acceptsAll(asList("v", "version"), "Show the version of Nukkit");

                acceptsAll(asList("d", "debug"), "Show the debug logs");
            }
        };

        OptionSet optionSet = null;

        try {
            optionSet = parser.parse(args);
        } catch (OptionException e) {
            LogManager.getLogger(Nukkit.class.getName()).log(Level.FATAL, e.getLocalizedMessage());
        }

        if (optionSet == null || optionSet.has("?")) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                LogManager.getLogger(Nukkit.class.getName()).log(Level.FATAL, (String) null, e);
            }
        } else if (optionSet.has("v")) {
            System.out.println(Server.class.getPackage().getImplementationVersion());
        } else {
            String path = new File(".").getAbsolutePath();
            if (path.contains("!") || path.contains("+")) {
                System.err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
                return;
            }

            try {

                // This trick bypasses Maven Shade's clever rewriting of our getProperty call when using String literals
                String jline_UnsupportedTerminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 'U', 'n', 's', 'u', 'p', 'p', 'o', 'r', 't', 'e', 'd', 'T', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});
                String jline_terminal = new String(new char[]{'j', 'l', 'i', 'n', 'e', '.', 't', 'e', 'r', 'm', 'i', 'n', 'a', 'l'});

                useJline = !(jline_UnsupportedTerminal).equals(System.getProperty(jline_terminal));

                if (optionSet.has("nojline")) {
                    System.setProperty("user.language", "en");
                    useJline = false;
                }

                if (useJline) {
                    AnsiConsole.systemInstall();
                } else {
                    // This ensures the terminal literal will always match the jline implementation
                    System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, jline.UnsupportedTerminal.class.getName());
                }

                if (optionSet.has("noconsole")) {
                    useConsole = false;
                }

                if (optionSet.has("debug")) {
                    LoggerContext context = (LoggerContext) LogManager.getContext(false);
                    Configuration config = context.getConfiguration();
                    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                    loggerConfig.setLevel(Level.DEBUG);
                    context.updateLoggers();
                }

                ConsoleReader reader = new ConsoleReader(System.in, System.out);
                reader.setExpandEvents(false);

                (new ConsoleWriter(System.out, reader)).start();

                Logger logger = LogManager.getLogger(Nukkit.class.getName());

                logger.info("info");
                logger.info(Language.get("language.name"));

                YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("nukkit.yml"));
                config.addDefaults(new LinkedHashMap<String, Object>() {
                    {
                        put("boolean", true);
                        put("number", 3.1415926);
                        put("color", Color.RED);
                        put("vector", Vector.getRandom());
                    }
                });
                config.options().copyDefaults(true);
                config.save("nukkit.yml");
                //TODO start server

                //// TODO: 2016/5/13 Remove this, test ModuleManager
                ModuleManager moduleManager = new SimpleModuleManager();
                moduleManager.addLoader(new JavaPluginLoader());
                for (ModuleInfo info : moduleManager.getModuleList()) {
                    Module module = moduleManager.getModule(info);
                    logger.info("Name: `"+info.getName()+"` Version: `"+info.getVersion()+"` Module Class: `"+module.getClass().getName()+"`");
                    module.unload();
                    System.gc();
                }

                //TODO: REMOVE THIS: TEST JLINE
                if (useConsole) {
                    while (true) {
                        String line;
                        if (useJline) {
                            line = reader.readLine(">", null);
                        } else {
                            line = reader.readLine();
                        }

                        if (line != null && !line.trim().isEmpty()) {
                            logger.info("input: " + line);
                        }
                    }
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    public static Logger getLogger() {
        return Server.getLogger();
    }

    private static List<String> asList(String... params) {
        return Arrays.asList(params);
    }
}

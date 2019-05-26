package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Library.Exception.IllegalPluginException;
import net.ME1312.Galaxi.Library.ExtraDataHandler;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Map.ObjectMapValue;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Plugin Info Class
 *
 * @see Plugin
 */
@Plugin(name = "sauce", version = "xd", authors = "lmao", dependencies = @Dependency(name = "sauce"))
public class PluginInfo implements ExtraDataHandler {
    private static final String ID_PATTERN = ".*?([A-Za-z0-9!#$&+\\-_. ]*).*?";
    private static ArrayList<String> usedNames = new ArrayList<String>();
    private static HashMap<Class<?>, PluginInfo> pluginMap = new HashMap<Class<?>, PluginInfo>();

    private Object plugin;
    private Image icon;
    private String name;
    private String display;
    private Version version;
    private Version signature;
    private String state;
    private List<String> authors;
    private String desc;
    private URL website;
    private List<String> loadBefore;
    private List<Dependency> depend;

    private File dir = new File(System.getProperty("user.dir"));
    private Logger logger = null;
    private Runnable updateChecker = null;
    private boolean enabled = false;
    private ObjectMap<String> extra = new ObjectMap<String>();

    public static class Dependency {
        private String name;
        private Version minversion, maxversion;
        private boolean required;

        /**
         * Generate Plugin DependencyInfo
         *
         * @param name Dependency Name
         * @param minversion Minimum Version
         * @param maxversion Maximum Version
         * @param required Required Status
         */
        protected Dependency(String name, Version minversion, Version maxversion, boolean required) {
            if (Util.isNull(name, required)) throw new NullPointerException();
            this.name = name;
            this.minversion = minversion;
            this.maxversion = maxversion;
            this.required = required;
        }

        /**
         * Get the name of the Dependency
         *
         * @return Dependency name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the minimum required version of the Dependency
         *
         * @return Minimum required version (null for all)
         */
        public Version getMinVersion() {
            return minversion;
        }

        /**
         * Get the maximum allowed version of the Dependency
         *
         * @return Maximum allowed version (null for all)
         */
        public Version getMaxVersion() {
            return maxversion;
        }

        /**
         * Get if this Dependency is required
         *
         * @return Required Dependency Status
         */
        public boolean isRequired() {
            return required;
        }
    }

    /**
     * Get or register PluginInfo for objects tagged with @App/@Plugin
     *
     * @param main Class tagged with @App/@Plugin
     * @return PluginInfo
     * @throws InvocationTargetException
     */
    public static PluginInfo getPluginInfo(Object main) throws InvocationTargetException {
        Class<?> mainClass = main.getClass();
        if (!pluginMap.keySet().contains(mainClass)) {

            try {
                if (mainClass.isAnnotationPresent(Plugin.class)) {
                    String name = mainClass.getAnnotation(Plugin.class).name().replaceAll(ID_PATTERN, "$1");
                    String display = (mainClass.getAnnotation(Plugin.class).display().length() > 0)?mainClass.getAnnotation(Plugin.class).display():mainClass.getAnnotation(Plugin.class).name();
                    Version version = Version.fromString(mainClass.getAnnotation(Plugin.class).version());
                    Version signature = (mainClass.getAnnotation(Plugin.class).signature().length() > 0)?Version.fromString(mainClass.getAnnotation(Plugin.class).signature()):null;
                    String state = (mainClass.getAnnotation(Plugin.class).signature().length() > 0)?mainClass.getAnnotation(Plugin.class).state():null;
                    List<String> authors = Arrays.asList(mainClass.getAnnotation(Plugin.class).authors());
                    String description = (mainClass.getAnnotation(Plugin.class).description().length() > 0)?mainClass.getAnnotation(Plugin.class).description():null;
                    URL website = (mainClass.getAnnotation(Plugin.class).website().length() > 0)?new URL(mainClass.getAnnotation(Plugin.class).website()):null;
                    List<String> loadBefore = Arrays.asList(mainClass.getAnnotation(Plugin.class).loadBefore());
                    List<Dependency> dependencies = new LinkedList<Dependency>();
                    for (net.ME1312.Galaxi.Plugin.Dependency dependency : mainClass.getAnnotation(Plugin.class).dependencies()) {
                        String dname = dependency.name().replaceAll(ID_PATTERN, "$1");
                        Version dminversion = (dependency.minVersion().length() > 0)?Version.fromString(dependency.minVersion()):null;
                        Version dmaxversion = (dependency.maxVersion().length() > 0)?Version.fromString(dependency.maxVersion()):null;
                        boolean drequired = dependency.required();

                        if (dname.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty dependency name");
                        if (dminversion != null && dmaxversion != null && dminversion.equals(dmaxversion)) throw new IllegalArgumentException("Cannot use the same dependency version for min and max");
                        dependencies.add(new Dependency(dname, dminversion, dmaxversion, drequired));
                    }

                    PluginInfo plugin = new PluginInfo(main, name, version, authors, description, website, loadBefore, dependencies);
                    plugin.setDisplayName(display);
                    plugin.setSignature(signature);
                    plugin.setState(state);

                    pluginMap.put(mainClass, plugin);
                    usedNames.add(name.toLowerCase());
                } else if (mainClass.isAnnotationPresent(App.class)) {
                    String name = mainClass.getAnnotation(App.class).name().replaceAll(ID_PATTERN, "$1");
                    String display = (mainClass.getAnnotation(App.class).display().length() > 0)?mainClass.getAnnotation(App.class).display():mainClass.getAnnotation(App.class).name();
                    Version version = Version.fromString(mainClass.getAnnotation(App.class).version());
                    Version signature = (mainClass.getAnnotation(App.class).signature().length() > 0)?Version.fromString(mainClass.getAnnotation(App.class).signature()):null;
                    String state = (mainClass.getAnnotation(App.class).signature().length() > 0)?mainClass.getAnnotation(App.class).state():null;
                    List<String> authors = Arrays.asList(mainClass.getAnnotation(App.class).authors());
                    String description = (mainClass.getAnnotation(App.class).description().length() > 0)?mainClass.getAnnotation(App.class).description():null;
                    URL website = (mainClass.getAnnotation(App.class).website().length() > 0)?new URL(mainClass.getAnnotation(App.class).website()):null;

                    PluginInfo plugin = new PluginInfo(main, name, version, authors, description, website, Collections.emptyList(), Collections.emptyList());
                    plugin.setDisplayName(display);
                    plugin.setSignature(signature);
                    plugin.setState(state);

                    pluginMap.put(mainClass, plugin);
                    usedNames.add(name.toLowerCase());
                } else {
                    throw new IllegalStateException("Class not annotated by @App or @Plugin: " + mainClass.getCanonicalName());
                }
            } catch (Throwable e) {
                throw new IllegalPluginException(e, "Couldn't load plugin descriptor for main class: " + main);
            }
        }
        return pluginMap.get(mainClass);
    }

    /**
     * Generate PluginInfo
     *
     * @param plugin Plugin Object
     * @param name Plugin Name
     * @param version Plugin Version
     * @param authors Authors List
     * @param description Plugin Description
     * @param website Author's Website
     * @param loadBefore Load Before List
     * @param dependencies Dependencies List
     */
    protected PluginInfo(Object plugin, String name, Version version, List<String> authors, String description, URL website, List<String> loadBefore, List<Dependency> dependencies) {
        if (Util.isNull(plugin, name, version, authors)) throw new NullPointerException();
        if (name.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty name");
        if (version.toString().length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty version");
        if (authors.size() == 0) throw new ArrayIndexOutOfBoundsException("Cannot use an empty authors list");
        if (description != null && description.length() == 0) description = null;
        this.plugin = plugin;
        this.name = name;
        this.version = version;
        this.authors = authors;
        this.desc = description;
        this.website = website;
        this.loadBefore = (loadBefore == null)?Collections.emptyList():loadBefore;
        this.depend = (dependencies == null)?Collections.emptyList():dependencies;
    }

    /**
     * Get the Plugin's ClassLoader
     *
     * @return Plugin ClassLoader
     */
    public ClassLoader getLoader() {
        return plugin.getClass().getClassLoader();
    }

    /**
     * Get Plugin Object
     *
     * @return Plugin Object
     */
    public Object get() {
        return plugin;
    }

    /**
     * Get the Plugin's Icon
     *
     * @return Plugin Icon
     */
    public Image getIcon() {
        return icon;
    }

    /**
     * Set the Plugin's Icon
     *
     * @param stream Icon Stream
     */
    public void setIcon(InputStream stream) throws IOException {
        icon = ImageIO.read(stream);
    }

    /**
     * Get the Plugin's Name
     *
     * @return Plugin Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the Plugin's Display Name
     *
     * @return Display Name
     */
    public String getDisplayName() {
        return (display == null)?getName():display;
    }

    /**
     * Set the Plugin's Display Name
     *
     * @param value Value (or null to reset)
     */
    public void setDisplayName(String value) {
        if (value == null || value.length() == 0 || getName().equals(value)) {
            this.display = null;
        } else {
            this.display = value;
        }
    }

    /**
     * Get the Plugin's Version
     *
     * @return Plugin Version
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * Get the Plugin's Build Signature
     *
     * @return Plugin Build Signature
     */
    public Version getSignature() {
        return this.signature;
    }

    /**
     * Set the Plugin's Build Signature (may only be done once)
     *
     * @param value Plugin Build Signature
     */
    public void setSignature(Version value) {
        if (signature == null) {
            signature = value;
        }
    }

    /**
     * Get the Plugin's State
     *
     * @return Plugin State
     */
    public String getState() {
        return this.state;
    }

    /**
     * Set the Plugin's State
     *
     * @param value Plugin State
     */
    public void setState(String value) {
        state = value;
    }

    /**
     * Get Authors List
     *
     * @return Authors List
     */
    public List<String> getAuthors() {
        return this.authors;
    }

    /**
     * Get Plugin Description
     *
     * @return Plugin Description
     */
    public String getDescription() {
        return this.desc;
    }

    /**
     * Get Authors' Website
     *
     * @return Authors' Website
     */
    public URL getWebsite() {
        return this.website;
    }

    /**
     * Gets the Load Before Plugins List
     *
     * @return Load Before Plugins List
     */
    public List<String> getLoadBefore() {
        return this.loadBefore;
    }

    /**
     * Gets the Dependencies List
     *
     * @return Dependencies List
     */
    public List<Dependency> getDependancies() {
        return this.depend;
    }

    /**
     * Sets the Plugin's Enabled Status
     *
     * @return Enabled Status
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets if the Plugin is Enabled
     *
     * @param value Value
     */
    public void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * Replace this Plugin's Logger with a custom one
     *
     * @param value Value
     */
    public void setLogger(Logger value) {
        if (Util.isNull(value)) throw new NullPointerException();
        logger = value;
    }

    /**
     * Gets the default Logger for this Plugin
     *
     * @return Logger
     */
    public Logger getLogger() {
        if (logger == null) logger = new Logger(name);
        return logger;
    }

    /**
     * Get the Update Checker for this Plugin
     *
     * @return Update Checker
     */
    public Runnable getUpdateChecker() {
        return updateChecker;
    }

    /**
     * Set the Update Checker for this Plugin
     *
     * @param checker Value
     */
    public void setUpdateChecker(Runnable checker) {
        this.updateChecker = checker;
    }

    /**
     * Gets this Plugin's data folder
     *
     * @return Data Folder
     */
    public File getDataFolder() {
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    @Override
    public void addExtra(String handle, Object value) {
        if (Util.isNull(handle, value)) throw new NullPointerException();
        extra.set(handle, value);
    }

    @Override
    public boolean hasExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return extra.getKeys().contains(handle);
    }

    @Override
    public ObjectMapValue<String> getExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return extra.get(handle);
    }

    @Override
    public ObjectMap<String> getExtra() {
        return extra.clone();
    }

    @Override
    public void removeExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        extra.remove(handle);
    }
}

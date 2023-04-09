package net.ME1312.Galaxi.Library;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Operating System Platform Enum
 */
public enum Platform {
    WINDOWS("Windows", new File((System.getenv("APPDATALOCAL") != null)?System.getenv("APPDATALOCAL"):System.getenv("APPDATA"), "GalaxiEngine")),
    MAC_OS("macOS", new File(System.getProperty("user.home"), "Library/Application Support/GalaxiEngine")),
    UNIX("Unix-like", new File(System.getProperty("user.home"), ".GalaxiEngine")),
    ;
    private static final Platform OS;
    private static final String OS_NAME;
    private static final String OS_VERSION;
    private static final String OS_BUILD;
    private static final String OS_ARCH;

    private static final int JAVA_LANG;
    private static final String JAVA_ARCH;

    private final String name;
    private final File appdata;
    Platform(String name, File appdata) {
        this.name = name;
        this.appdata = appdata;
    }

    /**
     * Get the name of this known Operating System
     *
     * @return Operating System Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the appropriate location for storing user data for this Operating System
     *
     * @return AppData Directory
     */
    public File getAppDataDirectory() {
        return appdata;
    }

    /**
     * Get the Operating System that is currently being used
     *
     * @return Current Operating System
     */
    public static Platform getSystem() {
        return OS;
    }

    /**
     * Get the name of the Operating System that is currently being used
     *
     * @return Current Operating System Name
     */
    public static String getSystemName() {
        return OS_NAME;
    }

    /**
     * Get the version of the Operating System that is currently being used
     *
     * @return Current Operating System Version
     */
    public static String getSystemVersion() {
        return OS_VERSION;
    }

    /**
     * Get the build of the Operating System that is currently being used
     *
     * @return Current Operating System Build (or null if no further versioning information is available)
     */
    public static String getSystemBuild() {
        return OS_BUILD;
    }

    /**
     * Get the architecture of the Operating System that is currently being used
     *
     * @return Current Operating System Architecture
     */
    public static String getSystemArchitecture() {
        return OS_ARCH;
    }

    /**
     * Get the Java Version that is currently being used
     *
     * @return Current Java Version
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Get the Java Language Level that is currently being provided
     *
     * @return Current Java Language Level
     */
    public static int getJavaLanguageLevel() {
        return JAVA_LANG;
    }

    /**
     * Get the Architecture the current version of Java was designed for
     *
     * @return Current Java Architecture
     */
    public static String getJavaArchitecture() {
        return JAVA_ARCH;
    }

    static {
        final String osversion = System.getProperty("os.version");
        final String osname = System.getProperty("os.name", "Unknown OS");
        final String os = osname.toLowerCase(Locale.ENGLISH);
        if (os.startsWith("mac") || os.startsWith("darwin")) {
            OS = MAC_OS;
            OS_NAME = OS.name;
            OS_VERSION = osversion;
        } else if (os.startsWith("win")) {
            OS = WINDOWS;
            if (os.startsWith("windows server ")) {
                OS_NAME = "Windows Server";
                OS_VERSION = osname.substring(OS_NAME.length() + 1);
            } else if (os.startsWith("windows ")) {
                OS_NAME = OS.name;
                OS_VERSION = osname.substring(OS_NAME.length() + 1);
            } else {
                OS_NAME = osname;
                OS_VERSION = osversion;
            }
        } else {
            OS = UNIX;
            OS_NAME = osname;
            OS_VERSION = osversion;
        }

        String osarch;
        if (OS == WINDOWS) {
            String osbuild = null;
            try {
                Matcher build = Pattern.compile(Pattern.quote(osversion) + "(?:\\.\\d+)*").matcher(Util.readAll(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"cmd.exe", "/q", "/c", "ver"}).getInputStream())));
                if (build.find()) osbuild = build.group().substring(osversion.length() + 1);
            } catch (IOException e) {}

            OS_BUILD = osbuild;
            if ((osarch = System.getenv("PROCESSOR_ARCHITEW6432")) == null) {
                osarch = System.getenv("PROCESSOR_ARCHITECTURE");
            }
        } else {
            OS_BUILD = null;
            osarch = System.getProperty("os.arch");
        }


        boolean x86 = false;
        if (osarch != null) {
            osarch = osarch.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
            if (osarch.matches("^((amd|x86|x)64|em64t|ia32e)$")) {
                OS_ARCH = "x64"; x86 = true;
            } else if (osarch.matches("^((i[3-6]|x)86|(ia|x86|x)32)$")) {
                OS_ARCH = "x86"; x86 = true;
            } else if (osarch.equals("aarch64") || osarch.equals("arm64")) {
                OS_ARCH = "arm64";
            } else if (osarch.equals("aarch32") || osarch.startsWith("arm")) {
                OS_ARCH = "arm";
            } else {
                OS_ARCH = osarch;
            }
        } else {
            OS_ARCH = "unknown";
        }

        final String jarch = System.getProperty("sun.arch.data.model", "unknown");
        for (;;) {
            if (x86) {
                if (jarch.equals("64")) {
                    JAVA_ARCH = "x64";
                    break;
                } else if (jarch.equals("32")) {
                    JAVA_ARCH = "x86";
                    break;
                }
            }
            if (Try.all.run(() -> Long.parseLong(jarch))) {
                JAVA_ARCH = jarch + "-bit";
            } else {
                JAVA_ARCH = jarch;
            }
            break;
        }

        String jversion = System.getProperty("java.specification.version");
        Matcher regex = Pattern.compile("(?:1\\.)?(\\d+).*").matcher(jversion);
        if (regex.find()) {
            JAVA_LANG = Integer.parseInt(regex.group(1));
        } else {
            JAVA_LANG = Integer.MAX_VALUE;
        }
    }
}

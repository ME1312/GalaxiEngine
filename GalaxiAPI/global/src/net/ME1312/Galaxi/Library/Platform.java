package net.ME1312.Galaxi.Library;

import net.ME1312.Galaxi.Library.Callback.ReturnCallback;

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
    MAC_OS("Mac OS", new File(System.getProperty("user.home"), "Library/Application Support/GalaxiEngine")),
    OTHER("Other", new File(System.getProperty("user.home"), ".GalaxiEngine"))
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
     * Get the name of the Operating System that is currently being used as the system originally presented it
     *
     * @see #getSystemName() The resulting Display Name may be the same as the same as the standardized name on some operating systems
     * @see #getSystemVersion() The resulting Display Name may contain the Version string on some operating systems
     * @return Current Operating System Display Name
     */
    public static String getSystemDisplayName() {
        return System.getProperty("os.name");
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
     * @see #getSystemVersion() The resulting Build string may be the same as the Version string on some operating systems
     * @return Current Operating System Build
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
        String osversion = System.getProperty("os.version");
        String osname = System.getProperty("os.name", "");
        String os = osname.toLowerCase(Locale.ENGLISH);
        if (os.startsWith("mac") || os.contains("darwin")) {
            OS = MAC_OS;
            OS_NAME = "Mac OS";
            OS_VERSION = osversion;
        } else if (os.startsWith("win")) {
            OS = WINDOWS;
            if (os.startsWith("windows server ")) {
                OS_NAME = "Windows Server";
                OS_VERSION = osname.substring(OS_NAME.length() + 1);
            } else if (os.startsWith("windows ")) {
                OS_NAME = "Windows";
                OS_VERSION = osname.substring(OS_NAME.length() + 1);
            } else {
                OS_NAME = osname;
                OS_VERSION = "";
            }
        } else {
            OS = OTHER;
            OS_NAME = osname;
            OS_VERSION = osversion;
        }

        String[] osarch;
        if (OS == WINDOWS) {
            String osbuild = osversion;
            try {
                Matcher build = Pattern.compile(Pattern.quote(osversion) + "(?:.\\d+)*").matcher(Util.readAll(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"cmd.exe", "/q", "/c", "ver"}).getInputStream())));
                if (build.find()) osbuild = build.group().substring(osversion.length() + 1);
            } catch (IOException e) {
//              e.printStackTrace(); // enable for debugging only -- normal users don't care
            }

            OS_BUILD = osbuild;
            osarch = new String[] {
                    System.getenv("PROCESSOR_ARCHITECTURE"),
                    System.getenv("PROCESSOR_ARCHITEW6432")
            };
        } else {
            OS_BUILD = osversion;
            osarch = new String[] {
                    System.getProperty("os.arch")
            };
        }
        if (isArch(osarch, arch -> arch.endsWith("64"))) {
            OS_ARCH = "x64";
        } else if (isArch(osarch, arch -> arch.endsWith("86"))) {
            OS_ARCH = "x86";
        } else {
            OS_ARCH = (osarch[0] != null) ? osarch[0] : "unknown";
        }

        String jarch = System.getProperty("sun.arch.data.model", "unknown");
        switch (jarch) {
            case "32":
                JAVA_ARCH = "x86";
                break;
            case "64":
                JAVA_ARCH = "x64";
                break;
            default:
                JAVA_ARCH = jarch;
        }

        String jversion = getJavaVersion();
        Matcher regex = Pattern.compile("(?:1\\.)?(\\d+).*").matcher(jversion);
        if (regex.find()) {
            JAVA_LANG = Integer.parseInt(regex.group(1));
        } else {
            JAVA_LANG = Integer.MAX_VALUE;
        }
    }

    private static <T> boolean isArch(T[] array, ReturnCallback<T, Boolean> operation) {
        for (T object : array) if (object != null && operation.run(object)) {
            return true;
        }
        return false;
    }
}

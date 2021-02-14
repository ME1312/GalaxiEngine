package net.ME1312.Galaxi.Library.Version;

public enum VersionType {
    PRE_ALPHA(-7, "pa", "pre-alpha"),
    ALPHA(-6, "a", "alpha"),
    PRE_BETA(-5, "pb", "pre-beta"),
    BETA(-4, "b", "beta"),
    PREVIEW(-3, "pv", "preview"),
    SNAPSHOT(-3, "s", "snapshot"),
    PRE_RELEASE(-2, "pr", "pre-release"),
    RELEASE_CANDIDATE(-1, "rc", "release candidate"),
    RELEASE(1, "r", "release"),
    REVISION(0, "rv", "revision"),
    VERSION(1, "v", "version"),
    UPDATE(0, "u", "update"),
    ;
    final short stageid;
    final String shortname, longname;
    VersionType(int stageid, String shortname, String longname) {
        this.stageid = (short) stageid;
        this.shortname = shortname;
        this.longname = longname;
    }
}

package net.ME1312.Galaxi.Engine;

/**
 * GalaxiOption Behaviour Definition Enum
 *
 * This enum follows the following format:
 *   X_Y_Z = [X] overrides [Y] overrides [Z]
 */
enum GalaxiOptionMode {
    USR(null),
    APP(null),
    DEF(null),

    USR_DEF((usr, app, def) -> select(new Object[]{usr, def}, new GalaxiOptionMode[]{USR, DEF})),
    DEF_USR((usr, app, def) -> select(new Object[]{def, usr}, new GalaxiOptionMode[]{DEF, USR})),
    APP_DEF((usr, app, def) -> select(new Object[]{app, def}, new GalaxiOptionMode[]{APP, DEF})),
    DEF_APP((usr, app, def) -> select(new Object[]{def, app}, new GalaxiOptionMode[]{DEF, APP})),

    USR_APP_DEF((usr, app, def) -> select(new Object[]{usr, app, def}, new GalaxiOptionMode[]{USR, APP, DEF})),
    USR_DEF_APP((usr, app, def) -> select(new Object[]{usr, def, app}, new GalaxiOptionMode[]{USR, DEF, APP})),
    DEF_USR_APP((usr, app, def) -> select(new Object[]{def, usr, app}, new GalaxiOptionMode[]{DEF, USR, APP})),
    ;
    private final OptionSelector selector;
    private GalaxiOptionMode(OptionSelector selector) {
        this.selector = selector;
    }

    private interface OptionSelector {
        GalaxiOptionMode select(Object usr, Object app, Object def);
    }

    public GalaxiOptionMode select(Object usr, Object app, Object def) {
        return (selector == null)? this : selector.select(usr, app, def);
    }

    private static GalaxiOptionMode select(Object[] objects, GalaxiOptionMode[] results) {
        if (objects.length != results.length) throw new IllegalArgumentException();

        Object l = objects[0];
        GalaxiOptionMode lt = results[0];
        for (int i = 1; i < objects.length && (l == null || lt == DEF); ++i) {
            Object c = objects[i];
            GalaxiOptionMode ct = results[i];

            if (lt == DEF) {
                if (l instanceof Boolean && c instanceof Boolean) {
                    if (((Boolean) l).compareTo((Boolean) c) > 0) {
                        l = c;
                        lt = ct;
                        break;
                    }
                } else if (l != null) {
                    break;
                }
            }

            if (c == null) {
                // no input
            } else if (c instanceof Number && ((Number) c).longValue() < 0) {
                // invalid input
            } else {
                l = c;
                lt = ct;
            }
        }

        return lt;
    }
}

package net.ME1312.Galaxi.Engine;

/*
 * GalaxiOption Behaviour Definition Enum
 *
 * This enum follows the following format:
 *   X_Y_Z = [X] overrides [Y] overrides [Z]
 */
enum GalaxiOptionMode {
    USR(null),
    APP(null),
    DEF(null),

    USR_DEF((usr, app, def) -> new Object[] {usr, def}, USR, DEF),
    DEF_USR((usr, app, def) -> new Object[] {def, usr}, DEF, USR),
    APP_DEF((usr, app, def) -> new Object[] {app, def}, APP, DEF),
    DEF_APP((usr, app, def) -> new Object[] {def, app}, DEF, APP),
    USR_APP((usr, app, def) -> new Object[] {usr, app}, USR, APP),

    USR_APP_DEF((usr, app, def) -> new Object[] {usr, app, def}, USR, APP, DEF),
    USR_DEF_APP((usr, app, def) -> new Object[] {usr, def, app}, USR, DEF, APP),
    DEF_USR_APP((usr, app, def) -> new Object[] {def, usr, app}, DEF, USR, APP),
    ;
    private final Reorderer reorderer;
    private final GalaxiOptionMode[] order;
    private GalaxiOptionMode(Reorderer reorderer, GalaxiOptionMode... order) {
        this.reorderer = reorderer;
        this.order = order;
    }

    private interface Reorderer {
        Object[] reorder(Object usr, Object app, Object def);
    }

    @SuppressWarnings("unchecked")
    public <T> T select(T usr, T app, T def) {
        if (reorderer == null) {
            switch (this) {
                case USR:
                    return usr;
                case APP:
                    return app;
                case DEF:
                    return def;
                default:
                    return null;
            }
        } else {
            Object[] objects = reorderer.reorder(usr, app, def);

            Object l = objects[0];
            GalaxiOptionMode lt = order[0];
            for (int i = 1; i < objects.length && (l == null || lt == DEF); ++i) {
                Object c = objects[i];
                GalaxiOptionMode ct = order[i];

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

            return (T) l;
        }
    }
}

package net.ME1312.Galaxi.Library.Event;

/**
 * Listener Order Defaults Class<br>
 * Listeners will be called from Short.MIN_VALUE to Short.MAX_VALUE unless the event is annotated by @ReverseOrder
 *
 * @see ReverseOrder
 */
public final class ListenerOrder {
    private ListenerOrder() {}
    public static final short FIRST = Short.MIN_VALUE;
    public static final short VERY_EARLY = (Short.MIN_VALUE / 3) * 2;
    public static final short EARLY = Short.MIN_VALUE / 3;
    public static final short NORMAL = 0;
    public static final short LATE = Short.MAX_VALUE / 3;
    public static final short VERY_LATE = (Short.MAX_VALUE / 3) * 2;
    public static final short LAST = Short.MAX_VALUE;
}

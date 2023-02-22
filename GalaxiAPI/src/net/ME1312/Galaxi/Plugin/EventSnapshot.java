package net.ME1312.Galaxi.Plugin;

final class EventSnapshot {
    static final EventSubscription[] EMPTY_ARRAY = new EventSubscription[0];
    static final EventSnapshot EMPTY = new EventSnapshot(EMPTY_ARRAY, EMPTY_ARRAY);

    final EventSubscription[] normal, overrides;
    EventSnapshot(EventSubscription[] normal, EventSubscription[] overrides) {
        this.normal = normal;
        this.overrides = overrides;
    }
}

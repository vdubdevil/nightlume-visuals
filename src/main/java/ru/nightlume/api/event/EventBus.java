package ru.nightlume.api.event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    private static final List<Subscriber> SUBSCRIBERS = new CopyOnWriteArrayList<>();

    private EventBus() {
    }

    public static void register(Object object) {
        if (object == null) {
            return;
        }

        for (Method method : object.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Subscribe.class)) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 1) {
                continue;
            }

            if (!Event.class.isAssignableFrom(parameters[0])) {
                continue;
            }

            method.setAccessible(true);
            SUBSCRIBERS.add(new Subscriber(object, method, parameters[0]));
        }
    }

    public static void unregister(Object object) {
        SUBSCRIBERS.removeIf(subscriber -> subscriber.parent == object);
    }

    public static void post(Event event) {
        if (event == null) {
            return;
        }

        for (Subscriber subscriber : SUBSCRIBERS) {
            if (!subscriber.type.isAssignableFrom(event.getClass())) {
                continue;
            }

            try {
                subscriber.method.invoke(subscriber.parent, event);
            } catch (Throwable ignored) {
            }
        }
    }

    private static final class Subscriber {
        private final Object parent;
        private final Method method;
        private final Class<?> type;

        private Subscriber(Object parent, Method method, Class<?> type) {
            this.parent = parent;
            this.method = method;
            this.type = type;
        }
    }
}
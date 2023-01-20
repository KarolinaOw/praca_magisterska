package pl.karolinamichalska.logo.lifecycle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LifeCycleMethodsMap {
    private final Map<Class<?>, LifeCycleMethods> map = new ConcurrentHashMap<>();

    public LifeCycleMethods get(Class<?> clazz)
    {
        return map.computeIfAbsent(clazz, LifeCycleMethods::new);
    }
}

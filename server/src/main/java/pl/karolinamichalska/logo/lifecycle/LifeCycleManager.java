package pl.karolinamichalska.logo.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

public class LifeCycleManager {

    private final Logger log = LoggerFactory.getLogger(LifeCycleManager.class);
    private final AtomicReference<State> state = new AtomicReference<>(State.LATENT);
    private final Queue<Object> managedInstances = new ConcurrentLinkedQueue<>();
    private final LifeCycleMethodsMap methodsMap;
    private final AtomicReference<Thread> shutdownHook = new AtomicReference<>();

    private interface LifeCycleStopFailureHandler {
        void handlePreDestroyException(Class<?> klass, Method method, Exception exception);
    }

    private enum State {
        LATENT,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED
    }

    public LifeCycleManager(List<Object> managedInstances, LifeCycleMethodsMap methodsMap) {
        this.methodsMap = (methodsMap != null) ? methodsMap : new LifeCycleMethodsMap();
        for (Object instance : managedInstances) {
            addInstance(instance);
        }
    }

    /**
     * Returns the number of managed instances
     *
     * @return qty
     */
    public int size() {
        return managedInstances.size();
    }

    public void start() {
        if (!state.compareAndSet(State.LATENT, State.STARTING)) {
            throw new RuntimeException("System already starting");
        }
        log.info("Life cycle starting...");

        for (Object obj : managedInstances) {
            LifeCycleMethods methods = methodsMap.get(obj.getClass());
            if (!methods.hasFor(PreDestroy.class)) {
                managedInstances.remove(obj);   // remove reference to instances that aren't needed anymore
            }
        }

        Thread thread = new Thread(() -> {
            try {
                log.info("JVM is shutting down, cleaning up");
                stop();
            } catch (Exception e) {
                log.error("Trying to shut down", e);
            }
        });
        shutdownHook.set(thread);
        Runtime.getRuntime().addShutdownHook(thread);

        state.set(State.STARTED);
        log.info("Life cycle started");
    }

    public void stopWithoutFailureLogging() {
        List<Exception> failures = new ArrayList<>();
        stop((klass, method, exception) -> failures.add(exception));
        if (!failures.isEmpty()) {
            RuntimeException stopException = new RuntimeException();
            for (Exception e : failures) {
                stopException.addSuppressed(e);
            }
            throw stopException;
        }
    }

    public void stop() {
        AtomicBoolean failure = new AtomicBoolean(false);
        stop((klass, method, exception) -> {
            failure.set(true);
            log.error(format("Exception in PreDestroy method %s::%s()", klass.getName(), method.getName()), exception);
        });

        if (failure.get()) {
            throw new RuntimeException();
        }
    }

    /**
     * Stop the life cycle - all instances will have their {@link PreDestroy} method(s) called and any
     * exceptions raised will be passed to the provided {@link LifeCycleStopFailureHandler} to collect.
     */
    private void stop(LifeCycleStopFailureHandler handler) {
        if (!state.compareAndSet(State.STARTED, State.STOPPING)) {
            return;
        }

        Thread thread = shutdownHook.getAndSet(null);
        if (thread != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(thread);
            } catch (IllegalStateException ignored) {
            }
        }

        log.info("Life cycle stopping...");

        List<Object> reversedInstances = new ArrayList<>(managedInstances);
        Collections.reverse(reversedInstances);

        for (Object obj : reversedInstances) {
            stopInstance(obj, handler);
        }

        state.set(State.STOPPED);
        log.info("Life cycle stopped");
    }

    public void addInstance(Object instance) {
        State currentState = state.get();
        checkState((currentState != State.STOPPING) && (currentState != State.STOPPED), "life cycle is stopped");
        startInstance(instance);
        if (methodsMap.get(instance.getClass()).hasFor(PreDestroy.class)) {
            managedInstances.add(instance);
        }
    }

    private void stopInstance(Object obj, LifeCycleStopFailureHandler handler) {
        log.debug("Stopping {}", obj.getClass().getName());
        LifeCycleMethods methods = methodsMap.get(obj.getClass());
        for (Method preDestroy : methods.methodsFor(PreDestroy.class)) {
            log.debug("- invoke {}::{}()", preDestroy.getDeclaringClass().getName(), preDestroy.getName());
            try {
                preDestroy.invoke(obj);
            } catch (Exception e) {
                handler.handlePreDestroyException(obj.getClass(), preDestroy, unwrapInvocationTargetException(e));
            }
        }
    }

    private void startInstance(Object obj) {
        log.debug("Starting {}", obj.getClass().getName());
        LifeCycleMethods methods = methodsMap.get(obj.getClass());
        for (Method postConstruct : methods.methodsFor(PostConstruct.class)) {
            log.debug("- invoke {}::{}()", postConstruct.getDeclaringClass().getName(), postConstruct.getName());
            try {
                postConstruct.invoke(obj);
            } catch (Exception e) {
                RuntimeException failure = new RuntimeException(
                        format("Exception in PostConstruct method %s::%s()", obj.getClass().getName(), postConstruct.getName()),
                        unwrapInvocationTargetException(e));
                stopInstance(obj, (Class<?> klass, Method method, Exception exception) -> {
                    String message = format("Exception in PreDestroy method %1$s::%2$s() after PostConstruct failure in %1$s::%3$s()", klass.getName(), method.getName(), postConstruct.getName());
                    failure.addSuppressed(new RuntimeException(message, exception));
                });
                throw failure;
            }
        }
    }

    private static Exception unwrapInvocationTargetException(Exception e) {
        return (e instanceof InvocationTargetException && e.getCause() instanceof Exception) ? (Exception) e.getCause() : e;
    }
}


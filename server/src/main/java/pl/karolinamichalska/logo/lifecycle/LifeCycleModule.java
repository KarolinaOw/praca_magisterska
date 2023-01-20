package pl.karolinamichalska.logo.lifecycle;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.spi.ProvisionListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.inject.matcher.Matchers.any;

public class LifeCycleModule implements Module {
    private final List<Object> injectedInstances = new ArrayList<>();
    private final LifeCycleMethodsMap lifeCycleMethodsMap = new LifeCycleMethodsMap();
    private final AtomicReference<LifeCycleManager> lifeCycleManager = new AtomicReference<>(null);

    @Override
    public void configure(Binder binder) {
        binder.disableCircularProxies();

        binder.bindListener(any(), this::provision);
    }

    private <T> void provision(ProvisionListener.ProvisionInvocation<T> provision) {
        Object obj = provision.provision();
        if ((obj == null) || !isLifeCycleClass(obj.getClass())) {
            return;
        }

        LifeCycleManager manager = lifeCycleManager.get();
        if (manager != null) {
            try {
                manager.addInstance(obj);
            } catch (Exception e) {
                throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        } else {
            injectedInstances.add(obj);
        }
    }

    @Provides
    @Singleton
    public LifeCycleManager getServerManager()
            throws Exception {
        LifeCycleManager lifeCycleManager = new LifeCycleManager(injectedInstances, lifeCycleMethodsMap);
        this.lifeCycleManager.set(lifeCycleManager);
        return lifeCycleManager;
    }

    private boolean isLifeCycleClass(Class<?> clazz) {
        LifeCycleMethods methods = lifeCycleMethodsMap.get(clazz);
        return methods.hasFor(PostConstruct.class) || methods.hasFor(PreDestroy.class);
    }
}

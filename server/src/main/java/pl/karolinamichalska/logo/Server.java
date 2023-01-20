package pl.karolinamichalska.logo;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.karolinamichalska.logo.jackson.JacksonModule;
import pl.karolinamichalska.logo.lifecycle.LifeCycleManager;
import pl.karolinamichalska.logo.lifecycle.LifeCycleModule;
import pl.karolinamichalska.logo.logo.LogoModule;
import pl.karolinamichalska.logo.server.HttpServer;
import pl.karolinamichalska.logo.server.ServerModule;

public class Server {

    private final static Logger log = LoggerFactory.getLogger(Server.class);

    public void start() {
        ImmutableList.Builder<Module> modules = ImmutableList.builder();
        modules.add(new LifeCycleModule());
        modules.add(new ServerModule());
        modules.add(new JacksonModule());
        modules.add(new LogoModule());

        Injector injector = Guice.createInjector(modules.build());

        LifeCycleManager lifeCycleManager = injector.getInstance(LifeCycleManager.class);
        lifeCycleManager.start();

        injector.getAllBindings();

        HttpServer httpServer = injector.getInstance(HttpServer.class);
        log.info("HTTP server listening at {}", httpServer.getHttpAddress());

        log.info("Server Started");
    }
}

package pl.karolinamichalska.logo.server;

import com.google.inject.AbstractModule;

import static com.google.inject.Scopes.SINGLETON;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HttpServer.class).in(SINGLETON);
        bind(HealthResource.class);
    }
}

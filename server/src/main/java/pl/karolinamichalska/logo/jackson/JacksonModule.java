package pl.karolinamichalska.logo.jackson;

import com.google.inject.AbstractModule;

import static com.google.inject.Scopes.SINGLETON;

public class JacksonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ObjectMapperContextResolver.class).in(SINGLETON);
    }
}

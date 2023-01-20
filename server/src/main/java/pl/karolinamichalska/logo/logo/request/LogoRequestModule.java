package pl.karolinamichalska.logo.logo.request;

import com.google.inject.AbstractModule;
import pl.karolinamichalska.logo.spark.SparkModule;

import static com.google.inject.Scopes.SINGLETON;

public class LogoRequestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LogoRequestStorage.class).to(InMemoryLogoRequestStorage.class).in(SINGLETON);
        bind(LogoRequestService.class).in(SINGLETON);
        bind(LogoRequestResource.class);
    }
}

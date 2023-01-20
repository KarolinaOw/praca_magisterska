package pl.karolinamichalska.logo.logo.submission;

import com.google.inject.AbstractModule;
import pl.karolinamichalska.logo.spark.SparkModule;

import static com.google.inject.Scopes.SINGLETON;

public class LogoSubmissionModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new SparkModule());

        bind(LogoSubmissionStorage.class).to(InMemoryLogoSubmissionStorage.class).in(SINGLETON);
        bind(LogoSubmissionService.class).in(SINGLETON);
    }
}

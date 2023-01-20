package pl.karolinamichalska.logo.spark;

import com.google.inject.AbstractModule;

import static com.google.inject.Scopes.SINGLETON;

public class SparkModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SparkJobService.class).to(DataprocSparkJobService.class).in(SINGLETON);
    }
}

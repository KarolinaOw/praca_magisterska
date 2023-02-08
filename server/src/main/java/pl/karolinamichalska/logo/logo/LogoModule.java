package pl.karolinamichalska.logo.logo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import pl.karolinamichalska.logo.logo.data.LogoDataModule;
import pl.karolinamichalska.logo.logo.request.LogoRequestModule;
import pl.karolinamichalska.logo.logo.submission.LogoSubmissionModule;

import javax.sql.DataSource;

public class LogoModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new LogoDataModule());
        install(new LogoRequestModule());
        install(new LogoSubmissionModule());
    }

    @Provides
    @Singleton
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}

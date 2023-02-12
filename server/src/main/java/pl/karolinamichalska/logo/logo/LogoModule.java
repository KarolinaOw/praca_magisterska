package pl.karolinamichalska.logo.logo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import pl.karolinamichalska.logo.lock.GcsLockManager;
import pl.karolinamichalska.logo.lock.LockManager;
import pl.karolinamichalska.logo.logo.data.LogoDataModule;
import pl.karolinamichalska.logo.logo.request.LogoRequestModule;
import pl.karolinamichalska.logo.logo.submission.LogoSubmissionModule;

import javax.sql.DataSource;

import static com.google.inject.Scopes.SINGLETON;

public class LogoModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new LogoDataModule());
        install(new LogoRequestModule());
        install(new LogoSubmissionModule());
        bind(LockManager.class).to(GcsLockManager.class).in(SINGLETON);
    }

    @Provides
    @Singleton
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}

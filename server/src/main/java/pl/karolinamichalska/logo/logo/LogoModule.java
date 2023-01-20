package pl.karolinamichalska.logo.logo;

import com.google.inject.AbstractModule;
import pl.karolinamichalska.logo.logo.data.LogoDataModule;
import pl.karolinamichalska.logo.logo.request.LogoRequestModule;
import pl.karolinamichalska.logo.logo.submission.LogoSubmissionModule;

public class LogoModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new LogoDataModule());
        install(new LogoRequestModule());
        install(new LogoSubmissionModule());
    }
}

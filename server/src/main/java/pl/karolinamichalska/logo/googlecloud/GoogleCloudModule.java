package pl.karolinamichalska.logo.googlecloud;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class GoogleCloudModule extends AbstractModule {

    @Provides
    @Singleton
    public Storage googleStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}

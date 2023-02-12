package pl.karolinamichalska.logo.lock;

import java.util.function.Supplier;

public interface LockManager {

    <T> T performLockedOrIgnore(String key, Supplier<T> action);
}

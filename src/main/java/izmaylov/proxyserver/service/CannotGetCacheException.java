package izmaylov.proxyserver.service;

@SuppressWarnings("WeakerAccess")
public class CannotGetCacheException extends RuntimeException {
    CannotGetCacheException(String message) {
        super(message);
    }
}

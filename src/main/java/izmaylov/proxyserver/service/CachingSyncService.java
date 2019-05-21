package izmaylov.proxyserver.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для синхронизации кэширования. Так как он отдельно, внутри него можно менять что захочется
 */
@SuppressWarnings("WeakerAccess")
@Service
@Log4j2
public class CachingSyncService {
    private final Map<String, Object> inCaching = new ConcurrentHashMap<>();

    private final static Object IN_CACHING = new Object();

    public Mono<String> checkIsCachedOrMarkToCache(String filename) {
        log.debug("check if caching " + filename);

        synchronized (inCaching) { // можно ли как-то сделать без лока на весь мэп?
            if (inCaching.containsKey(filename)) {
                log.debug("caching " + filename);
                return Mono.just(filename);
            } else {
                log.debug("needs to be cached " + filename);
                inCaching.put(filename, IN_CACHING);
                return Mono.empty();
            }
        }
    }

    public void cached(String filename) {
        inCaching.remove(filename);
    }

    public void waitUntilCached(String filename) throws InterruptedException {
        while (inCaching.containsKey(filename)) {
            TimeUnit.MILLISECONDS.sleep(10);
        }
    }
}

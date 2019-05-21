package izmaylov.proxyserver.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.*;
import java.util.function.UnaryOperator;

/**
 * Главный сервис для кэширования
 */
@Service
@Log4j2
public class CacheLoaderService {
    private static final UnaryOperator<String> encoder = s -> s.replace('/', '_');

    private final ResourceService resourceService;

    private final DownloadService downloadService;

    private final ThreadingService threadingService;

    private final CachingSyncService cachingSyncService;

    @Autowired
    public CacheLoaderService(DownloadService downloadService, ResourceService resourceService,
                              ThreadingService threadingService, CachingSyncService cachingSyncService) {

        this.resourceService = resourceService;
        this.downloadService = downloadService;
        this.threadingService = threadingService;
        this.cachingSyncService = cachingSyncService;
    }

    public Mono<Resource> getResource(String resourceName) {
        log.debug("request to " + resourceName);

        return Mono.just(resourceName)
                .map(encoder)
                .map(resourceService::getFromCachedResource)
                .filter(Resource::exists)
                .switchIfEmpty(download(resourceName));
    }

    private Mono<Resource> download(String urlSuffix) {
        return Mono.just(urlSuffix)
                .flatMap(cachingSyncService::checkIsCachedOrMarkToCache)
                .flatMap(this::waitUntilCached)
                .switchIfEmpty(Mono.just(urlSuffix)
                        .flatMap(suffix -> downloadService.requestForData(suffix, byte[].class))
                        .map(bytes -> cacheFile(urlSuffix, bytes))
                        .map(ByteArrayResource::new)
                );
    }

    // я не смог сюда прикрепить reactor
    private byte[] cacheFile(String filename, byte[] bytes) {
        threadingService.acceptWriter(() -> {
            log.debug("cache file " + filename);

            resourceService.writeResource(encoder.apply(filename), bytes);

            cachingSyncService.cached(filename);

            return null;
        });

        return bytes;
    }


    private Mono<Resource> waitUntilCached(String resource) {
        log.info("wait " + resource);
        CompletableFuture<Resource> futureResource = threadingService.acceptWaiting(() -> {
            try {
                cachingSyncService.waitUntilCached(resource);
            } catch (InterruptedException e) {
                return null;
            }

            return resourceService.getFromCachedResource(encoder.apply(resource));
        });

        return Mono.fromFuture(futureResource);
    }
}

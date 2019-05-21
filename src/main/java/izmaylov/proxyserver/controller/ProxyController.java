package izmaylov.proxyserver.controller;

import izmaylov.proxyserver.service.CacheLoaderService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
public class ProxyController {
    private final CacheLoaderService cacheLoaderService;

    @Autowired
    public ProxyController(CacheLoaderService cacheLoaderService) {
        this.cacheLoaderService = cacheLoaderService;
    }

    // я не смог сделать так, чтобы путь был общий и его можно было достастать в тестах
    @GetMapping("/{id1}/{id2}/{file:[0-9]+\\.png}")
    public Publisher<ResponseEntity<Resource>> getImage(@PathVariable String id1,
                                                        @PathVariable String id2,
                                                        @PathVariable String file) {
        return cacheLoaderService
                .getResource(String.format("%s/%s/%s", id1, id2, file))
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource)
                ).switchIfEmpty( // пусто => ничего не найдено => 404
                        Mono.just(ResponseEntity
                                .notFound()
                                .build())
                );
    }
}

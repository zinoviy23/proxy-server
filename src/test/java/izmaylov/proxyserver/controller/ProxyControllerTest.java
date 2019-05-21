package izmaylov.proxyserver.controller;

import izmaylov.proxyserver.service.CacheLoaderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebFluxTest
@ComponentScan({"izmaylov.proxyserver"})
public class ProxyControllerTest {
    @Autowired
    private WebTestClient client;

    @MockBean
    private CacheLoaderService cacheLoaderService;

    @Before
    public void setUp() {
        Resource resource = new ByteArrayResource(new byte[] {1, 2, 3});
        when(cacheLoaderService.getResource("4/4/6.png"))
                .thenReturn(Mono.just(resource));

        when(cacheLoaderService.getResource("4/4/1000000.png"))
                .thenReturn(Mono.empty());
    }

    @Test
    public void getOk() {
        client.get()
                .uri("/4/4/6.png")
                .accept(MediaType.IMAGE_PNG)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getNotFound() {
        client.get()
                .uri("/4/4/1000000.png")
                .exchange()
                .expectStatus().isNotFound();
    }
}
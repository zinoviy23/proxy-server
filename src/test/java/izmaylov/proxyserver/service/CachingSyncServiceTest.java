package izmaylov.proxyserver.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CachingSyncServiceTest {
    @Autowired
    private CacheLoaderService cacheLoaderService;

    @MockBean
    private DownloadService downloadService;

    @MockBean
    private ResourceService resourceService;

    private int writingCount = 0;

    private int downloadedCount = 0;

    @Before
    public void setUp() {
        writingCount = 0;
        downloadedCount = 0;

        Set<Object> objects = new HashSet<>();

        Resource notExistingResource = Mockito.mock(Resource.class);
        Resource existingResource = mock(Resource.class);

        when(notExistingResource.exists()).thenReturn(false);
        when(existingResource.exists()).thenReturn(true);

        when(resourceService.getFromCachedResource(any())).then(i -> {
            if (objects.contains(i.getArgument(0)))
                return existingResource;
            return notExistingResource;
        });

        doAnswer(i -> {
            writingCount++;

            objects.add(i.getArgument(0)); // если не получилось отправить параллельно

            return null;
        }).when(resourceService).writeResource(any(), any());

        when(downloadService.requestForData(any(), any())).then(i -> {
            downloadedCount++;

            return Mono.just(new byte[]{1, 2, 3});
        });
    }

    @Repeat(value = 10)
    @Test
    public void synchronization() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> {
            StepVerifier.create(cacheLoaderService.getResource("aa"))
                    .expectNextMatches(i -> true)
                    .verifyComplete();

            return null;
        });

        CompletableFuture.supplyAsync(() -> {
            StepVerifier.create(cacheLoaderService.getResource("aa"))
                    .expectNextMatches(i -> true)
                    .verifyComplete();

            return null;
        });

        TimeUnit.SECONDS.sleep(1);

        assertEquals(1, writingCount);
        assertEquals(1, downloadedCount);
    }
}
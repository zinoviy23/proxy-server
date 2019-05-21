package izmaylov.proxyserver.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheLoaderServiceTest {
    @MockBean
    private DownloadService downloadService;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private CachingSyncService cachingSyncService;

    @Autowired
    private CacheLoaderService cacheLoaderService;

    private boolean isWritten = false;

    private int counter = 0;

    @Before
    public void setUp() throws Exception {
        isWritten = false;
        counter = 0;

        Resource resourceExisting = mock(Resource.class);
        Resource resourceNotExisting = mock(Resource.class);

        byte[] bytes = new byte[] {1};

        when(downloadService.requestForData("b", byte[].class))
                .thenReturn(Mono.just(bytes));

        when(resourceNotExisting.exists()).thenReturn(false);

        when(resourceService.getFromCachedResource("a")).thenReturn(resourceExisting);
        when(resourceExisting.contentLength()).thenReturn(48L);

        when(resourceService.getFromCachedResource("b")).thenReturn(resourceNotExisting);

        when(resourceExisting.exists()).thenReturn(true);

        doAnswer(invocationOnMock -> {
            isWritten = true;
            return null;
        }).when(resourceService).writeResource(any(), any());

        when(cachingSyncService.checkIsCachedOrMarkToCache("b")).thenReturn(Mono.empty());
    }

    @Test
    public void existingResource() {
        StepVerifier
                .create(cacheLoaderService.getResource("a"))
                .expectNextMatches(r -> {
                    counter++;
                    try {
                        return r.contentLength() == 48L;
                    } catch (IOException e) {
                        return false;
                    }
                }).verifyComplete();

        assertEquals(1, counter);
    }

    @Test
    public void cacheResource() throws InterruptedException {
        StepVerifier
                .create(cacheLoaderService.getResource("b"))
                .expectNextMatches(r -> {
                    counter++;
                    return r instanceof ByteArrayResource;
                })
                .verifyComplete();

        assertEquals(1, counter);

        TimeUnit.MILLISECONDS.sleep(100);

        assertTrue(isWritten);
    }
}
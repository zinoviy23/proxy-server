package izmaylov.proxyserver.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DownloadServiceTest {

    @Autowired
    private DownloadService downloadService;

    private boolean hasSomething = false;

    @Before
    public void setUp() {
        hasSomething = true;
    }

    @Test
    public void simpleRequestWithLeadingSlash() {
        StepVerifier
                .create(downloadService.requestForData("/4/4/6.png", byte[].class))
                .expectNextMatches(__ -> hasSomething = true)
                .verifyComplete();

        assertTrue(hasSomething);
    }

    @Test
    public void simpleRequest() {
        StepVerifier
                .create(downloadService.requestForData("4/4/6.png", byte[].class))
                .expectNextMatches(__ -> hasSomething = true)
                .verifyComplete();

        assertTrue(hasSomething);
    }

    @Test
    public void empty() {
        StepVerifier
                .create(downloadService.requestForData("kek", byte[].class))
                .verifyComplete();
    }
}
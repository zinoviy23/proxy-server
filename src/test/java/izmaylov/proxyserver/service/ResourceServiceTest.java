package izmaylov.proxyserver.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResourceServiceTest {
    @Autowired
    private ResourceService service;

    @Test
    public void writing() throws IOException {
        service.writeResource("kek", new byte[] { 1, 2, 3});

        assertTrue(Files.exists(Paths.get("cache/kek")));

        try (FileInputStream reader = new FileInputStream("cache/kek")) {

            byte[] bytes = new byte[3];

            int size = reader.read(bytes);

            assertEquals(3, size);

            assertArrayEquals(new byte[] {1, 2, 3}, bytes);
        } finally {
            Files.deleteIfExists(Paths.get("cache/kek"));
        }
    }
}
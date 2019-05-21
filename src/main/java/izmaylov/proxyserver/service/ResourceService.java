package izmaylov.proxyserver.service;

import izmaylov.proxyserver.config.AppConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Сервис для работы с ресурсами
 */
@SuppressWarnings("WeakerAccess")
@Service
@Log4j2
public class ResourceService {
    private final Path mainDirectory;

    @Autowired
    public ResourceService(AppConfig config) {
        mainDirectory = Paths.get(config.getDirectory()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(mainDirectory);
        } catch (IOException e) {
            log.error("Cannot create directory");
        }

        log.info("resource directory: " + mainDirectory);
    }

    public void writeResource(String filename, byte[] bytes) {
        Path file = mainDirectory.resolve(filename);
        try (WritableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {

            channel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public Resource getFromCachedResource(String filename) {
        try {
            return new UrlResource(mainDirectory.resolve(filename).toUri());
        } catch (MalformedURLException e) {
            log.error(e);
            throw new CannotGetCacheException(filename);
        }
    }
}

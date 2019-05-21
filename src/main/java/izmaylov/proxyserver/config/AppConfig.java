package izmaylov.proxyserver.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {
    @Value("${caching.url}")
    private String cachingUrl;

    @Value("${caching.directory}")
    private String directory;

    @Value("${caching.waitingCount}")
    private int waitingCount;

    @Value("${caching.writersCount}")
    private int writersCount;
}

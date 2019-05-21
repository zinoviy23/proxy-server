package izmaylov.proxyserver.service;

import izmaylov.proxyserver.config.AppConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Сервис для скачивания
 */
@SuppressWarnings("WeakerAccess")
@Service
@Log4j2
public class DownloadService {
    private final WebClient webClient = WebClient.create();

    private final String downloadUrl;

    @Autowired
    public DownloadService(AppConfig config) {
        downloadUrl = config.getCachingUrl();

        log.info("downloading from " + downloadUrl);
    }

    public <T> Mono<T> requestForData(String urlSuffix, Class<T> clazz) {
        log.debug("send request to " + getUrl(urlSuffix));

        return webClient.get()
                .uri(getUrl(urlSuffix))
                .exchange()
                .filter(r -> r.statusCode() == HttpStatus.OK)
                .flatMap(r -> r.bodyToMono(clazz));
    }

    private String getUrl(String urlSuffix) {
        return downloadUrl + "/" + StringUtils.trimLeadingCharacter(urlSuffix, '/');
    }

}

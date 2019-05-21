package izmaylov.proxyserver.service;

import izmaylov.proxyserver.config.AppConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Сервис для потоков
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
@Service
@Log4j2
public class ThreadingService {
    private final Executor waitingExecutor;

    private final Executor writersExecutor;

    @Autowired
    public ThreadingService(AppConfig config) {
        if (config.getWritersCount() <= 0)
            throw new IllegalArgumentException("writers count cannot be less than 1");

        if (config.getWaitingCount() <= 0)
            throw new IllegalArgumentException("waiting count cannot be less than 1");

        waitingExecutor = Executors.newFixedThreadPool(config.getWaitingCount());
        writersExecutor = Executors.newFixedThreadPool(config.getWritersCount());

        log.info("threads for waiting: " + config.getWaitingCount());
        log.info("threads for writers " + config.getWritersCount());
    }

    public <T> CompletableFuture<T> acceptWriter(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, writersExecutor);
    }

    public <T> CompletableFuture<T> acceptWaiting(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, waitingExecutor);
    }
}

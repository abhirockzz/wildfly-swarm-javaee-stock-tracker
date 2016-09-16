package com.oracle.cloud.tickertracker;

import com.oracle.cloud.tickertracker.util.StockDataParser;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;

/**
 * Periodically polls the Google Finance REST endpoint using the JAX-RS client
 * API to pull stock prices and pushes them to connected WebSocket clients using
 * CDI events
 *
 * @author Abhishek
 */
@Singleton
@Startup
public class StockPriceScheduler {

    @Resource
    private TimerService ts;
    private Timer timer;

    /**
     * Sets up the EJB timer (polling job)
     */
    @PostConstruct
    public void init() {
        timer = ts.createTimer(0, 5000, null); //trigger every 5 seconds
        Logger.getLogger(StockPriceScheduler.class.getName()).log(Level.INFO, "Timer initiated");
    }

    @Inject
    @StockDataEventQualifier
    private Event<String> msgEvent;

    /**
     * Implements the logic. Invoked by the container as per scheduled
     *
     * @param timer the EJB Timer object
     */
    @Timeout
    public void timeout(Timer timer) {

        /**
         * Invoked asynchronously
         */
        Future<String> tickFuture = ClientBuilder.newClient().
                target("https://www.google.com/finance/info?q=NASDAQ:ORCL").
                request().buildGet().submit(String.class);

        /**
         * Extracting result immediately with a timeout (3 seconds) limit. This
         * is a workaround since we cannot impose timeouts for synchronous
         * invocations
         */
        String tick = null;
        try {
            tick = tickFuture.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Logger.getLogger(StockPriceScheduler.class.getName()).log(Level.INFO, "GET timed out. Next iteration due on - {0}", timer.getNextTimeout());
            return;
        }

        if (tick != null) {
            /**
             * cleaning the JSON payload
             */
            tick = tick.replace("// [", "");
            tick = tick.replace("]", "");

            msgEvent.fire(StockDataParser.parse(tick));
        }

    }

    /**
     * purges the timer
     */
    @PreDestroy
    public void close() {
        timer.cancel();
        Logger.getLogger(StockPriceScheduler.class.getName()).log(Level.INFO, "Application shutting down. Timer will be purged");
    }
}

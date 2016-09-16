package com.oracle.cloud.tickertracker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * A server side WebSocket endpoint to broadcast stock prices to connected
 * clients/users
 *
 * @author Abhishek
 */
@ServerEndpoint("/rt/stocks")
public class RealTimeStockTicker {

    //stores Session (s) a.k.a connected clients
    private static final List<Session> CLIENTS = new ArrayList<>();

    /**
     * Connection callback method. Stores connected client info
     *
     * @param s WebSocket session
     */
    @OnOpen
    public void open(Session s) {
        CLIENTS.add(s);
        Logger.getLogger(RealTimeStockTicker.class.getName()).log(Level.INFO, "Client connected -- {0}", s.getId());
    }

    /**
     * pushes stock prices asynchronously to ALL connected clients
     *
     * @param tickTock the stock price
     */
    public void broadcast(@Observes @StockDataEventQualifier String tickTock) {

        for (final Session s : CLIENTS) {
            if (s != null && s.isOpen()) {
                /**
                 * Asynchronous push
                 */
                s.getAsyncRemote().sendText(tickTock, new SendHandler() {
                    @Override
                    public void onResult(SendResult result) {
                        if (result.isOK()) {
                            Logger.getLogger(RealTimeStockTicker.class.getName()).log(Level.INFO, "Price sent to client {0}", s.getId());
                        } else {
                            Logger.getLogger(RealTimeStockTicker.class.getName()).log(Level.SEVERE, "Could not send price update to client " + s.getId(),
                                    result.getException());
                        }
                    }
                });
            }

        }

    }

    /**
     * Disconnection callback. Removes client (Session object) from internal
     * data store
     *
     * @param s WebSocket session
     */
    @OnClose
    public void close(Session s) {
        CLIENTS.remove(s);
        Logger.getLogger(RealTimeStockTicker.class.getName()).log(Level.INFO, "Client discconnected -- {0}", s.getId());
    }

}

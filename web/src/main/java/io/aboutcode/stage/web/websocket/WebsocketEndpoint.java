package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.subscription.SubscriptionManager;
import io.aboutcode.stage.util.Action;
import io.aboutcode.stage.web.websocket.io.WebsocketIo;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Each instance will be mounted on the corresponding path and will handle communication between
 * connected clients and all handlers that are registered for that path.</p>
 * <p>Since the Spark annotations are not inherited, one would have to annotate every handler
 * individually. This wrapper delegates calls to non-annotated handlers and can be used
 * instead.</p>
 */
@WebSocket
public final class WebsocketEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketEndpoint.class);
    private final SubscriptionManager<Long, Consumer<Object>, Void> broadcastSubscriptionManager = SubscriptionManager
            .asynchronous();
    private final Map<Session, DefaultWebsocketClientSession> sessions = new HashMap<>();
    private final String path;
    private final WebsocketIo io;
    private final List<AutowiredWebsocketHandler> handlers;

    public WebsocketEndpoint(String path, WebsocketIo io,
                             List<AutowiredWebsocketHandler> handlers) {
        this.path = path;
        this.io = io;
        this.handlers = handlers;
    }

    public void initialize() {
        handlers.forEach(handler -> handler.initialize(new WebsocketContext() {
            @Override
            public void publishToSubscribedClients(String topic, Object message) {
                broadcastSubscriptionManager.forTopic(topic,
                                                      (subscriber, handback, ctx) ->
                                                              subscriber.accept(message));
            }

            @Override
            public void publishToAllClients(Object message) {
                broadcastSubscriptionManager.forAll(
                        (subscriber, handback, ctx) -> subscriber.accept(message));
            }
        }));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String input) {
        try {
            io.deserialize(input)
              .ifPresent(message -> {
                  DefaultWebsocketClientSession clientSession = sessions.get(session);
                  List<Object> responses = handlers.stream()
                                                   .map(handler -> handler
                                                           .invokeDataHandlers(message,
                                                                               clientSession))
                                                   .flatMap(Collection::stream)
                                                   .collect(Collectors.toList());

                  responses.forEach(clientSession::send);
              });
        } catch (IOException e) {
            logger.error("Message '{}' not handled by endpoint: {}", input, e.getMessage(), e);
        }
    }


    @OnWebSocketConnect
    public void onConnect(Session session) {
        DefaultWebsocketClientSession clientSession = new DefaultWebsocketClientSession(
                session, () -> onDisconnect(session, 404,
                                            "Remote session terminated unexpectedly"));
        sessions.put(session, clientSession);
        handlers.forEach(websocketDataHandler -> websocketDataHandler.onConnected(clientSession));
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int status, String reason) {
        logger.debug("Disconnecting with status '{}' because: {}", status, reason);
        DefaultWebsocketClientSession clientSession = sessions.remove(session);
        clientSession.unsubscribeClientFromAll();
        handlers.forEach(handler -> handler.onDisconnected(clientSession));
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        logger.error("Error in websocket connection", error);
    }

    public String getPath() {
        return path;
    }

    private class DefaultWebsocketClientSession implements WebsocketClientSession,
            WriteCallback {
        private final Map<String, Object> state = new HashMap<>();
        private final Map<Object, Long> broadcastTopicToHandle = new HashMap<>();
        private final Session session;
        private Action cleanupAction;

        DefaultWebsocketClientSession(Session session, Action cleanupAction) {
            this.session = session;
            this.cleanupAction = cleanupAction;
        }

        @Override
        public void send(Object message) {
            if (session.isOpen()) {
                sendRequest(session, message);
            } else {
                logger.warn("Unresponsive client at '{}', closing connection",
                            session.getRemoteAddress());
                cleanupAction.accept();
            }
        }

        @Override
        public void close(int statusCode, String reason) {
            session.close(statusCode, reason);
        }

        private void sendRequest(Session session, Object message) {
            try {
                io.serialize(message)
                  .ifPresent(data -> session.getRemote().sendString(data, this));
            } catch (IOException e) {
                logger.error("Error serializing message '{}': {}", message, e.getMessage(), e);
            }
        }

        @Override
        public void subscribeClientToTopic(Object topic) {
            broadcastTopicToHandle.computeIfAbsent(topic, key ->
                    broadcastSubscriptionManager.subscribe(key, null, this::send));
        }

        void unsubscribeClientFromAll() {
            broadcastTopicToHandle.forEach((key, value) ->
                                                   broadcastSubscriptionManager.unsubscribe(value));
        }

        @Override
        public void addState(String identifier, Object value) {
            synchronized (state) {
                this.state.put(identifier, value);
            }
        }

        @Override
        public Optional<Object> removeState(String identifier) {
            synchronized (state) {
                if (state.containsKey(identifier)) {
                    return Optional.ofNullable(this.state.remove(identifier));
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<Object> getState(String identifier) {
            synchronized (state) {
                if (state.containsKey(identifier)) {
                    return Optional.ofNullable(state.get(identifier));
                }
            }
            return Optional.empty();
        }

        @Override
        public void writeFailed(Throwable error) {
            logger.error("Error in sending message to websocket connection: {}", error, error);
        }

        @Override
        public void writeSuccess() {
            // this is expected
        }

        @Override
        public Map<String, List<String>> headers() {
            return session.getUpgradeRequest().getHeaders();
        }

        @Override
        public List<String> headers(String name) {
            return session.getUpgradeRequest().getHeaders(name);
        }

        @Override
        public Optional<String> header(String name) {
            return Optional.ofNullable(session.getUpgradeRequest().getHeader(name));
        }
    }
}

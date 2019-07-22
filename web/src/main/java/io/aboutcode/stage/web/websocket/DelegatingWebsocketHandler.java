package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.subscription.SubscriptionManager;
import io.aboutcode.stage.subscription.SubscriptionManager.SubscriptionAction;
import io.aboutcode.stage.util.Action;
import io.aboutcode.stage.util.Tuple2;
import io.aboutcode.stage.web.websocket.standard.TypedWebsocketMessage;
import io.aboutcode.stage.web.websocket.standard.WebsocketClientSession;
import io.aboutcode.stage.web.websocket.standard.WebsocketContext;
import io.aboutcode.stage.web.websocket.standard.WebsocketDataHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
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
 * Helper class that allows simplified registration of {@link WebsocketDataHandler}. Since the
 * annotations are not inherited, one would have to annotate every handler individually. This
 * wrapper delegates calls to non-annotated handlers and can be used instead.
 */
@WebSocket
public final class DelegatingWebsocketHandler<MessageT extends TypedWebsocketMessage> {
    private static final Logger logger = LoggerFactory.getLogger(DelegatingWebsocketHandler.class);
    private final SubscriptionManager<Long, Consumer<MessageT>, Void> broadcastSubscriptionManager = SubscriptionManager
            .asynchronous();
    private final Map<Session, DefaultWebsocketClientSession> sessions = new HashMap<>();
    private final WebsocketIo<MessageT> io;
    private final Set<WebsocketDataHandler> handlers = new HashSet<>();

    public DelegatingWebsocketHandler(WebsocketIo<MessageT> io) {
        this.io = io;
    }

    private static Tuple2<Class, Session> createTopic(Session session, Class type) {
        return Tuple2.of(type, session);
    }

    public void addandInitialize(WebsocketDataHandler<MessageT> handler) {
        if (handlers.add(handler)) {
            handler.initialize(new WebsocketContext<MessageT>() {
                @Override
                public void publishToSubscribedClients(String topic, MessageT message) {
                    broadcastSubscriptionManager
                            .forTopic(topic,
                                      (subscriber, handback, ctx) -> subscriber.accept(message));
                }

                @Override
                public void publishToAllClients(MessageT message) {
                    broadcastSubscriptionManager
                            .forAll((subscriber, handback, ctx) -> subscriber.accept(message));
                }
            });
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            MessageT request = io.deserialize(message);
            DefaultWebsocketClientSession clientSession = sessions.get(session);
            clientSession.send(createTopic(session, request.getClass()),
                               (subscriber, handback, context) -> subscriber.accept(request));
        } catch (IOException e) {
            logger.error("Message '{}' not handled by endpoint: {}", message, e.getMessage(), e);
        }
    }


    @OnWebSocketConnect
    public void onConnect(Session session) {
        DefaultWebsocketClientSession clientSession = new DefaultWebsocketClientSession(
                session, () -> onDisconnect(session, 404,
                                            "Remote session terminated unexpectedly"));
        sessions.put(session, clientSession);
        //noinspection unchecked
        handlers.forEach(websocketDataHandler -> websocketDataHandler.onConnected(clientSession));
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int status, String reason) {
        logger.debug("Disconnecting with status '{}' because: {}", status, reason);
        DefaultWebsocketClientSession clientSession = sessions.remove(session);
        clientSession.send((subscriber, handback, context) -> context.cancel());
        clientSession.unsubscribeClientFromAll();
        //noinspection unchecked
        handlers.forEach(handler -> handler.onDisconnected(clientSession));
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        logger.error("Error in websocket connection", error);
    }

    private class DefaultWebsocketClientSession implements WebsocketClientSession<MessageT>,
            WriteCallback {
        private final Map<String, Object> state = new HashMap<>();
        private final Map<Object, Long> broadcastTopicToHandle = new HashMap<>();
        private final Session session;
        private final SubscriptionManager<Long, Consumer<MessageT>, Void> requestSubscriptionManager = SubscriptionManager
                .asynchronous();
        private final List<Long> requestSubscriptionHandles = new ArrayList<>();
        private Action cleanupAction;

        DefaultWebsocketClientSession(Session session, Action cleanupAction) {
            this.session = session;
            this.cleanupAction = cleanupAction;
        }

        void send(Object topic, SubscriptionAction<Consumer<MessageT>, Void> action) {
            requestSubscriptionManager.forTopic(topic, action);
        }

        void send(SubscriptionAction<Consumer<MessageT>, Void> action) {
            requestSubscriptionManager.forAll(action);
        }

        @Override
        public void send(MessageT message) {
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

        private void sendRequest(Session session, MessageT message) {
            try {
                session.getRemote().sendString(io.serialize(message), this);
            } catch (IOException e) {
                logger.error("Error serializing message '{}': {}", message, e.getMessage(), e);
            }
        }

        @Override
        public void subscribeClientToTopic(Object topic) {
            broadcastTopicToHandle.computeIfAbsent(topic, key ->
                    broadcastSubscriptionManager.subscribe(key, null, this::send));
        }

        @Override
        public <RequestT extends MessageT, ResponseT extends MessageT> void registerMessageHandler(
                Class<RequestT> requestType, Function<RequestT, ResponseT> messageHandler) {
            //noinspection unchecked
            requestSubscriptionHandles
                    .add(requestSubscriptionManager
                                 .subscribe(createTopic(session, requestType),
                                            null,
                                            message -> processRequest((RequestT) message,
                                                                      messageHandler)));
        }

        @Override
        public <RequestT extends MessageT> void registerMessageConsumer(Class<RequestT> requestType,
                                                                        Consumer<RequestT> messageConsumer) {
            registerMessageHandler(requestType, message -> {
                messageConsumer.accept(message);
                return null;
            });
        }

        private <RequestT extends MessageT, ResponseT extends MessageT> void processRequest(
                RequestT request, Function<RequestT, ResponseT> messageHandler) {
            try {
                ResponseT response = messageHandler.apply(request);
                if (!Objects.isNull(response)) {
                    send(response);
                }
            } catch (Exception e) {
                logger.error("Error processing response for request '{}': {}",
                             request,
                             e.getMessage(),
                             e);
            }
        }

        void unsubscribeClientFromAll() {
            broadcastTopicToHandle.forEach((key, value) ->
                                                   broadcastSubscriptionManager.unsubscribe(value));
            requestSubscriptionHandles.forEach(requestSubscriptionManager::unsubscribe);
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
    }
}

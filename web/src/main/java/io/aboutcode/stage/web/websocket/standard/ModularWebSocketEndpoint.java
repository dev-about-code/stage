package io.aboutcode.stage.web.websocket.standard;

import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import io.aboutcode.stage.subscription.SubscriptionManager;
import io.aboutcode.stage.util.Tuple2;
import io.aboutcode.stage.web.web.WebEndpoint;
import io.aboutcode.stage.web.websocket.DelegatingWebSocketHandler;
import io.aboutcode.stage.web.websocket.WebSocketHandler;
import io.aboutcode.stage.web.websocket.WebSocketIo;
import io.aboutcode.stage.web.websocket.WebSocketRoute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of a {@link WebEndpoint} listens on a single websocket path and forwards any
 * request to any {@link WebSocketDataHandler} in the component container. The handlers in turn
 * process the messages and communicate with the client session provided to them by this endpoint.
 */
public final class ModularWebSocketEndpoint<MessageT extends TypedWebSocketMessage> extends
    BaseComponent implements WebEndpoint {
   private static final Logger logger = LoggerFactory.getLogger(ModularWebSocketEndpoint.class);
   private final SubscriptionManager<Long, Consumer<MessageT>, Void> broadcastSubscriptionManager = SubscriptionManager
       .asynchronous();
   private final String targetPath;
   private final WebSocketIo<MessageT> io;
   private Set<WebSocketDataHandler> handlers;

   /**
    * Constructs a new instance.
    *
    * @param targetPath The path to register all handlers on
    * @param io         The io to use for message serialization/deserialization
    */
   public ModularWebSocketEndpoint(String targetPath, WebSocketIo<MessageT> io) {
      this.targetPath = targetPath;
      this.io = io;
   }

   private static Tuple2<Class, Session> createTopic(Session session, Class type) {
      return Tuple2.of(type, session);
   }

   @Override
   public void resolve(DependencyContext context) throws DependencyException {
      handlers = context.retrieveDependencies(WebSocketDataHandler.class);
   }

   @Override
   public List<WebSocketRoute> getWebSocketRoutes() {
      //noinspection unchecked
      handlers.forEach(handler -> handler.initialize(new WebSocketContext<MessageT>() {

         @Override
         public void publishToAllClients(String topic, MessageT message) {
            broadcastSubscriptionManager
                .forTopic(topic, (subscriber, handback, context) -> subscriber.accept(message));
         }

         @Override
         public <TargetT extends MessageT> void registerMessageType(String messageIdentifier,
                                                                    Class<TargetT> targetClass) {
            io.registerMessageType(messageIdentifier, targetClass);
         }
      }));

      return WebSocketRoute.list(
          WebSocketRoute.at(targetPath,
                            new DelegatingWebSocketHandler(new WebSocketHandler() {
                               private final SubscriptionManager<Long, Consumer<MessageT>, Void> requestSubscriptionManager = SubscriptionManager
                                   .asynchronous();
                               private final Map<Session, DefaultWebSocketClientSession> sessions = new HashMap<>();

                               @Override
                               public void onMessage(Session session, String message) {
                                  try {
                                     MessageT request = io.deserialize(message);
                                     requestSubscriptionManager
                                         .forTopic(createTopic(session, request.getClass()),
                                                   (subscriber, handback, context) -> subscriber
                                                       .accept(request));
                                  } catch (IOException e) {
                                     logger.error(String
                                                      .format("Message not handled by endpoint: %s",
                                                              message));
                                  }
                               }

                               @Override
                               public void onConnect(Session session) {
                                  DefaultWebSocketClientSession clientSession = new DefaultWebSocketClientSession(
                                      session, requestSubscriptionManager,
                                      nada -> onDisconnect(session, 404,
                                                           "Remote session terminated unexpectedly"));
                                  sessions.put(session, clientSession);
                                  //noinspection unchecked
                                  handlers.forEach(webSocketDataHandler -> webSocketDataHandler
                                      .onConnected(clientSession));
                               }

                               @Override
                               public void onDisconnect(Session session, int status,
                                                        String reason) {
                                  logger.debug(String.format(
                                      "Disconnecting with status '%d' because: %s", status,
                                      reason));
                                  requestSubscriptionManager
                                      .forAll((subscriber, handback, context) -> context.cancel());
                                  DefaultWebSocketClientSession clientSession = sessions
                                      .remove(session);
                                  clientSession.unsubscribeClientFromAll();
                                  //noinspection unchecked
                                  handlers
                                      .forEach(handler -> handler.onDisconnected(clientSession));
                               }

                               @Override
                               public void onError(Session session, Throwable error) {
                                  logger.error(
                                      String.format("Error in websocket connection: %s", error),
                                      error);
                               }
                            })
          )
      );
   }

   private class DefaultWebSocketClientSession implements WebSocketClientSession<MessageT>,
       WriteCallback {
      private final Map<String, Object> state = new HashMap<>();
      private final Map<String, Long> broadcastTopicToHandle = new HashMap<>();
      private final Session session;
      private final SubscriptionManager<Long, Consumer<MessageT>, Void> requestSubscriptionManager;
      private final List<Long> requestSubscriptionHandles = new ArrayList<>();
      private Consumer<Void> cleanupFunction;

      DefaultWebSocketClientSession(Session session,
                                    SubscriptionManager<Long, Consumer<MessageT>, Void> requestSubscriptionManager,
                                    Consumer<Void> cleanupFunction) {
         this.session = session;
         this.requestSubscriptionManager = requestSubscriptionManager;
         this.cleanupFunction = cleanupFunction;
      }

      @Override
      public void send(MessageT message) {
         if (session.isOpen()) {
            sendRequest(session, message);
         } else {
            logger.warn(String.format("Unresponsive client at '%s', closing connection",
                                      session.getRemoteAddress()));
            cleanupFunction.accept(null);
         }
      }

      private void sendRequest(Session session, MessageT message) {
         try {
            session.getRemote().sendString(io.serialize(message), this);
         } catch (IOException e) {
            logger
                .error(String.format("Error serializing message '%s': %s", message, e.getMessage()),
                       e);
         }
      }

      @Override
      public void subscribeClientToTopic(String topic) {
         if (broadcastTopicToHandle.containsKey(topic)) {
            throw new IllegalArgumentException("Topic already subscribed: " + topic);
         }
         broadcastTopicToHandle
             .put(topic, broadcastSubscriptionManager.subscribe(topic, null, this::send));
      }

      @Override
      public <TargetT extends MessageT> void registerClientRequestHandler(
          Class<TargetT> targetClass, Consumer<TargetT> requestHandler) {
         //noinspection unchecked
         requestSubscriptionHandles.add(requestSubscriptionManager
                                            .subscribe(createTopic(session, targetClass), null,
                                                       message -> requestHandler
                                                           .accept((TargetT) message)));
      }

      void unsubscribeClientFromAll() {
         broadcastTopicToHandle
             .forEach((key, value) -> broadcastSubscriptionManager.unsubscribe(value));
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
         logger.error(String.format("Error in sending message to websocket connection: %s", error),
                      error);
      }

      @Override
      public void writeSuccess() {
         // this is expected
      }
   }
}

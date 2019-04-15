package io.aboutcode.stage.web.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Helper class that allows simplified registration of {@link WebSocketHandler}. Since the
 * annotations are not inherited, one would have to annotate every handler individually. This
 * wrapper delegates calls to non-annotated handlers and can be used instead.
 */
@WebSocket
public final class DelegatingWebSocketHandler implements WebSocketHandler {
   private WebSocketHandler delegate;

   public DelegatingWebSocketHandler(WebSocketHandler delegate) {
      this.delegate = delegate;
   }

   @OnWebSocketMessage
   @Override
   public void onMessage(Session session, String message) {
      delegate.onMessage(session, message);
   }

   @OnWebSocketConnect
   @Override
   public void onConnect(Session session) {
      delegate.onConnect(session);
   }

   @OnWebSocketClose
   @Override
   public void onDisconnect(Session session, int status, String reason) {
      delegate.onDisconnect(session, status, reason);
   }

   @OnWebSocketError
   @Override
   public void onError(Session session, Throwable error) {
      delegate.onError(session, error);
   }
}

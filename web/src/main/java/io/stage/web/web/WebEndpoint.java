package io.aboutcode.stage.web.web;

import io.aboutcode.stage.web.websocket.WebSocketRoute;
import java.util.Collections;
import java.util.List;

/**
 * Implementors of this interface will be registered by the WebServerComponent and can partake in
 * request-response Http communication.
 */
public interface WebEndpoint {
   /**
    * Returns the routes that this endpoint exposes, defaults to an empty list.
    *
    * @return The routes that this endpoint exposes.
    */
   default List<Route> getRoutes() {
      return Collections.emptyList();
   }

   /**
    * Returns the websocket routes that this endpoint wants to register, defaults to an empty list.
    *
    * @return The websocket routes that this endpoint wants to register.
    */
   default List<WebSocketRoute> getWebSocketRoutes() {
      return Collections.emptyList();
   }
}

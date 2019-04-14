package io.aboutcode.stage.web;

import io.aboutcode.stage.configuration.Parameter;

/**
 * A configuration object for a webserver that provides TSL secured HTTP communication.
 */
final class TslConfiguration {
   @Parameter(name = "web-keystore",
       description = "The full file system path to the keystore for the web server's private and public key")
   private String keyStoreLocation;
   @Parameter(name = "web-keystore-password",
       description = "The password for the keystore; omit if not set",
       mandatory = false)
   private String keyStorePassword;
   @Parameter(name = "web-truststore",
       description = "The full file system path to the trust store for the web server's certificate",
       mandatory = false)
   private String trustStoreLocation;
   @Parameter(name = "web-truststore-password",
       description = "The password for the trust store; omit if not set",
       mandatory = false)
   private String trustStorePassword;
   @Parameter(name = "web-client-certificate-required",
       description = "If set, the client is required to present a certificate",
       mandatory = false)
   private boolean clientCertificateRequired;

   TslConfiguration() {
   }

   String getKeyStoreLocation() {
      return keyStoreLocation;
   }

   String getKeyStorePassword() {
      return keyStorePassword;
   }

   String getTrustStoreLocation() {
      return trustStoreLocation;
   }

   String getTrustStorePassword() {
      return trustStorePassword;
   }

   boolean isClientCertificateRequired() {
      return clientCertificateRequired;
   }
}

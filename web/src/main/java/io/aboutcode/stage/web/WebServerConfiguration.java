package io.aboutcode.stage.web;

import io.aboutcode.stage.configuration.Parameter;

/**
 * A configuration object for a webserver that provides unsecured HTTP communication.
 */
final class WebServerConfiguration {
    @Parameter(name = "web-port",
            description = "The port the application should be accessible through")
    private int port;
    @Parameter(name = "static-folder",
            description = "The full and absolute folder name of the directory that contains external (i.e. on the file system) static resources",
            mandatory = false)
    private String externalStaticFolder;

    WebServerConfiguration(int defaultPort) {
        this.port = defaultPort;
    }

    int getPort() {
        return port;
    }

    String getExternalStaticFolder() {
        return externalStaticFolder;
    }
}

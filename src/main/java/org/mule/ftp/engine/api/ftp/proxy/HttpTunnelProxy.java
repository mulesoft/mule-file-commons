/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.proxy;

import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.sdk.api.annotation.semantics.connectivity.ConfiguresProxy;
import org.mule.sdk.api.annotation.semantics.connectivity.Host;
import org.mule.sdk.api.annotation.semantics.connectivity.Port;
import org.mule.sdk.api.annotation.semantics.security.Username;

import java.util.Objects;

import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;

/**
 * Groups FTP Proxy connection parameters
 *
 * @since 1.6.0
 */

@Summary("Enables you to set HTTP tunnel proxy.")
@ConfiguresProxy
public class HttpTunnelProxy implements ProxySettings {

  /**
   * The FTP Proxy server host, such as www.mulesoft.com, localhost, or 192.168.0.1, etc
   */
  @Parameter
  @DisplayName("Proxy host")
  @Expression(SUPPORTED)
  @Optional(defaultValue = "")
  @Host
  protected String host;

  /**
   * The port number of the FTP Proxy server to connect
   */
  @Parameter
  @DisplayName("Proxy port")
  @Expression(SUPPORTED)
  @Optional(defaultValue = "")
  @Port
  protected int port = 3128;

  /**
   * Username for the FTP Proxy Server. Required if the Proxy server is authenticated.
   */
  @Parameter
  @DisplayName("Proxy username")
  @Expression(SUPPORTED)
  @Optional(defaultValue = "")
  @Username
  protected String username;

  /**
   * Password for the FTP Proxy Server. Required if the Proxy server is authenticated.
   */
  @Parameter
  @DisplayName("Proxy password")
  @Expression(SUPPORTED)
  @Optional(defaultValue = "")
  @Password
  protected String password;

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    HttpTunnelProxy that = (HttpTunnelProxy) o;
    return port == that.port && Objects.equals(host, that.host) && Objects.equals(username, that.username)
        && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, username, password);
  }

}

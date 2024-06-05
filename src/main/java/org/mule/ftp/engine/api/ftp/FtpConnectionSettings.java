/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.sdk.api.annotation.semantics.connectivity.Host;
import org.mule.sdk.api.annotation.semantics.connectivity.Port;
import org.mule.sdk.api.annotation.semantics.security.Username;

/**
 * Groups FTP connection parameters
 *
 * @since 1.0
 */
public final class FtpConnectionSettings {

  /**
   * The FTP server host, such as www.mulesoft.com, localhost, or 192.168.0.1, etc
   */
  @Parameter
  @Placement(order = 1)
  @Host
  private String host;

  /**
   * The port number of the FTP server to connect
   */
  @Parameter
  @Optional(defaultValue = "21")
  @Placement(order = 2)
  @Port
  private int port = 21;

  /**
   * Username for the FTP Server. Required if the server is authenticated.
   */
  @Parameter
  @Optional
  @Placement(order = 3)
  @Username
  private String username;

  /**
   * Password for the FTP Server. Required if the server is authenticated.
   */
  @Parameter
  @Password
  @Optional
  @Placement(order = 4)
  private String password;

  public int getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }
}

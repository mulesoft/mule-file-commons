/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.mule.ftp.engine.api.FileSystemProvider;
import org.mule.ftp.engine.api.exceptions.FileError;
import org.mule.ftp.engine.api.ftp.logging.LoggingOutputStream;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;
import org.mule.sdk.api.annotation.semantics.connectivity.ExcludeFromConnectivitySchema;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.mule.ftp.engine.api.exceptions.FileError.CANNOT_REACH;
import static org.mule.ftp.engine.api.exceptions.FileError.CONNECTION_TIMEOUT;
import static org.mule.ftp.engine.api.exceptions.FileError.CONNECTIVITY;
import static org.mule.ftp.engine.api.exceptions.FileError.INVALID_CREDENTIALS;
import static org.mule.ftp.engine.api.exceptions.FileError.SERVICE_NOT_AVAILABLE;
import static org.mule.ftp.engine.api.exceptions.FileError.UNKNOWN_HOST;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Connects to an FTP server
 *
 * @since 1.6.0
 */
public abstract class FtpAbstractConnectionProvider extends FileSystemProvider<FtpFileSystem> implements
    PoolingConnectionProvider<FtpFileSystem>, Initialisable {

  private static final Logger LOGGER = getLogger(FtpAbstractConnectionProvider.class);
  private static final String FTP_ERROR_MESSAGE_MASK =
      "Could not establish FTP connection with host: '%s' at port: '%d' - %s";
  public static final String ERROR_CODE_MASK = "Error code: %d - %s";

  @Inject
  private LockFactory lockFactory;

  private static final String TIMEOUT_CONFIGURATION = "Timeout Configuration";
  private static final String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

  private static AtomicBoolean alreadyLoggedConnectionTimeoutWarning = new AtomicBoolean(false);
  private static AtomicBoolean alreadyLoggedResponseTimeoutWarning = new AtomicBoolean(false);

  /**
   * The directory to be considered as the root of every relative path used with this connector. If not provided, it will default
   * to the remote server default.
   */
  @Parameter
  @Optional
  @Summary("The directory to be considered as the root of every relative path used with this connector")
  @DisplayName("Working Directory")
  private String workingDir = null;

  @ParameterGroup(name = TIMEOUT_CONFIGURATION)
  private TimeoutSettings timeoutSettings = new TimeoutSettings();

  /**
   * Invokes the {@link FtpFileSystem#disconnect()} method on the given {@code ftpFileSystem}
   *
   * @param ftpFileSystem a {@link FtpFileSystem} instance
   */
  @Override
  public void disconnect(FtpFileSystem ftpFileSystem) {
    ftpFileSystem.disconnect();
  }

  /**
   * Validates the connection by delegating into {@link FtpFileSystem#validateConnection()}
   *
   * @param ftpFileSystem the connection to validate
   * @return a {@link ConnectionValidationResult}
   */
  @Override
  public ConnectionValidationResult validate(FtpFileSystem ftpFileSystem) {
    return ftpFileSystem.validateConnection();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getWorkingDir() {
    return workingDir;
  }

  @ParameterGroup(name = CONNECTION)
  private FtpConnectionSettings connectionSettings;

  /**
   * The transfer mode to be used. Currently {@code BINARY} and {@code ASCII} are supported.
   * <p>
   * Defaults to {@code BINARY}
   */
  @Parameter
  @Optional(defaultValue = "BINARY")
  @Summary("Transfer mode to be used")
  private FtpTransferMode transferMode;

  /**
   * Whether to use passive mode. Set to {@code false} to switch to active mode.
   * <p>
   * Defaults to {@code true}.
   */
  @Parameter
  @Optional(defaultValue = "true")
  @Summary("Whether to use passive mode. Set to \"false\" to switch to active mode")
  private boolean passive = true;

  /**
   *  Enable/disable verification that the remote host taking part of a data connection is the same as the host to which the control connection is attached.
   */
  @Parameter
  @Optional(defaultValue = "true")
  @Summary("Whether to verify if the remote host taking part of a data connection is the same as the host to which the control connection is attached or not")
  @DisplayName("Enable Remote Verification")
  @ExcludeFromConnectivitySchema
  private boolean remoteVerificationEnabled = true;

  /**
   * Set the control encoding to use in the control channel with the remote server. This does NOT set the encoding for the content of the files to be transferred.
   * <p>
   * Known control encodings:
   * <ul>
   *     <li>ISO-8859-1</li>
   *     <li>UTF-8</li>
   * </ul>
   */
  @Parameter
  @Optional(defaultValue = DEFAULT_CONTROL_ENCODING)
  @Placement(tab = ADVANCED_TAB)
  @OfValues(ControlEncodingValueProvider.class)
  @Summary("Set the control encoding (for example UTF-8) to use in the control channel with the remote server. This does NOT set the encoding for the content of the files to be transferred.")
  @DisplayName("Control Encoding")
  @ExcludeFromConnectivitySchema
  private String controlEncoding;

  private SingleFileListingMode singleFileListingMode = SingleFileListingMode.UNSET;

  /**
   * Creates and returns a new instance of {@link FtpFileSystem}
   *
   * @return a {@link FtpFileSystem}
   */
  @Override
  public FtpFileSystem connect() throws ConnectionException {
    return new FtpFileSystem(setupClient(), getWorkingDir(), lockFactory, singleFileListingMode);
  }

  private FTPClient setupClient() throws ConnectionException {
    checkConnectionTimeoutPrecision();
    checkResponseTimeoutPrecision();

    FTPClient client = createClient();
    client.setControlEncoding(controlEncoding);
    if (getConnectionTimeout() != null && getConnectionTimeoutUnit() != null) {
      client.setConnectTimeout(new Long(getConnectionTimeoutUnit().toMillis(getConnectionTimeout())).intValue());
    }
    if (getResponseTimeout() != null && getResponseTimeoutUnit() != null) {
      client.setDefaultTimeout((int) (getResponseTimeoutUnit().toMillis(getResponseTimeout())));
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER
          .debug(format("Connecting to host: '%s' at port: '%d'", connectionSettings.getHost(), connectionSettings.getPort()));
    }

    try {
      client.setRemoteVerificationEnabled(remoteVerificationEnabled);
      client.connect(connectionSettings.getHost(), connectionSettings.getPort());
      if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
        throw handleClientReplyCode(client.getReplyCode());
      }
      if (!client.login(connectionSettings.getUsername(), connectionSettings.getPassword())) {
        throw handleClientReplyCode(client.getReplyCode());
      }
    } catch (SocketTimeoutException e) {
      throw new FTPConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e, CONNECTION_TIMEOUT);
    } catch (ConnectException e) {
      throw new FTPConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e, CANNOT_REACH);
    } catch (UnknownHostException e) {
      throw new FTPConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e, UNKNOWN_HOST);
    } catch (Exception e) {
      ConnectionException connectionException;
      if (client.getReplyCode() != 0) {
        connectionException = handleClientReplyCode(client.getReplyCode(), e);
        LOGGER.error(connectionException.getMessage(), connectionException);
        throw connectionException;
      }
      connectionException = new ConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e);
      LOGGER.error(connectionException.getMessage(), connectionException);
      throw connectionException;
    }

    return client;
  }

  protected abstract FTPClient createClient();

  @Override
  public void onBorrow(FtpFileSystem connection) {
    connection.setTransferMode(transferMode);
    connection.setResponseTimeout(getResponseTimeout(), getResponseTimeoutUnit());
    connection.setPassiveMode(passive);
  }

  /**
   * Handles a {@link FTPClient} reply code and returns a {@link FTPConnectionException} specifying the correspondent
   * {@link FileError} indicating the cause of the failure.
   *
   * @param replyCode FTP Client reply code
   * @return A {@link FTPConnectionException} specifying the error cause with a {@link FileError}
   */
  private ConnectionException handleClientReplyCode(int replyCode) {
    return handleClientReplyCode(replyCode, null);
  }

  /**
   * Handles a {@link FTPClient} reply code and returns a {@link FTPConnectionException} specifying the correspondent
   * {@link FileError} indicating the cause of the failure.
   *
   * @param replyCode FTP Client reply code
   * @param cause the cause exception
   * @return A {@link FTPConnectionException} specifying the error cause with a {@link FileError}
   */
  protected ConnectionException handleClientReplyCode(int replyCode, Throwable cause) {
    switch (replyCode) {
      case 501:
      case 530:
        return new FTPConnectionException(getErrorMessage(replyCode, "User cannot log in"),
                                          INVALID_CREDENTIALS);
      case 421:
        return new FTPConnectionException(getErrorMessage(replyCode, "Service is unavailable"),
                                          SERVICE_NOT_AVAILABLE);
    }
    if (cause != null) {
      return new FTPConnectionException(getErrorMessage(connectionSettings, format("Error code: '%d'", replyCode)), cause,
                                        CONNECTIVITY);
    }

    return new FTPConnectionException(getErrorMessage(connectionSettings, format("Error code: '%d'", replyCode)));
  }

  private String getErrorMessage(FtpConnectionSettings connectionSettings, String message) {
    return format(FTP_ERROR_MESSAGE_MASK, connectionSettings.getHost(), connectionSettings.getPort(), message);
  }

  protected String getErrorMessage(int replyCode, String message) {
    return format(FTP_ERROR_MESSAGE_MASK, connectionSettings.getHost(), connectionSettings.getPort(),
                  format(ERROR_CODE_MASK, replyCode, message));
  }

  protected Integer getConnectionTimeout() {
    return timeoutSettings.getConnectionTimeout();
  }

  protected TimeUnit getConnectionTimeoutUnit() {
    return timeoutSettings.getConnectionTimeoutUnit();
  }

  protected Integer getResponseTimeout() {
    return timeoutSettings.getResponseTimeout();
  }

  protected TimeUnit getResponseTimeoutUnit() {
    return timeoutSettings.getResponseTimeoutUnit();
  }

  public void setConnectionTimeout(Integer connectionTimeout) {
    timeoutSettings.setConnectionTimeout(connectionTimeout);
  }

  public void setConnectionTimeoutUnit(TimeUnit connectionTimeoutUnit) {
    timeoutSettings.setConnectionTimeoutUnit(connectionTimeoutUnit);
  }

  public void setResponseTimeout(Integer responseTimeout) {
    timeoutSettings.setResponseTimeout(responseTimeout);
  }

  public void setResponseTimeoutUnit(TimeUnit responseTimeoutUnit) {
    timeoutSettings.setResponseTimeoutUnit(responseTimeoutUnit);
  }

  private void checkConnectionTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getConnectionTimeoutUnit(), getConnectionTimeout())
        && alreadyLoggedConnectionTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Connection timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }

  private void checkResponseTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getResponseTimeoutUnit(), getResponseTimeout())
        && alreadyLoggedResponseTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Response timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }

  private boolean supportedTimeoutPrecision(TimeUnit timeUnit, Integer timeout) {
    return timeUnit != null && timeout != null && (timeUnit.toMillis(timeout) >= 1 || timeout == 0);
  }

  protected void setupWireLogging(FTPClient client, Consumer<String> operation) {
    try {
      client
          .addProtocolCommandListener(new PrintCommandListener(new PrintWriter(new OutputStreamWriter(new LoggingOutputStream(operation),
                                                                                                      "UTF-8")),
                                                               true));
    } catch (UnsupportedEncodingException e) {
      LOGGER.error(e.getMessage(), e);
    }

  }
}

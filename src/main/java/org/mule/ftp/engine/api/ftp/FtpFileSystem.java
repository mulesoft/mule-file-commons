/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.mule.ftp.engine.api.AbstractExternalFileSystem;
import org.mule.ftp.engine.api.AbstractFileSystem;
import org.mule.ftp.engine.api.FileAttributes;
import org.mule.ftp.engine.api.command.CopyCommand;
import org.mule.ftp.engine.api.command.CreateDirectoryCommand;
import org.mule.ftp.engine.api.command.DeleteCommand;
import org.mule.ftp.engine.api.command.ListCommand;
import org.mule.ftp.engine.api.command.MoveCommand;
import org.mule.ftp.engine.api.command.ReadCommand;
import org.mule.ftp.engine.api.command.RenameCommand;
import org.mule.ftp.engine.api.command.WriteCommand;
import org.mule.ftp.engine.api.lock.URLPathLock;
import org.mule.ftp.engine.api.lock.UriLock;
import org.mule.ftp.engine.api.util.UriUtils;
import org.mule.ftp.engine.api.ftp.command.FtpCopyCommand;
import org.mule.ftp.engine.api.ftp.command.FtpCreateDirectoryCommand;
import org.mule.ftp.engine.api.ftp.command.FtpDeleteCommand;
import org.mule.ftp.engine.api.ftp.command.FtpListCommand;
import org.mule.ftp.engine.api.ftp.command.FtpMoveCommand;
import org.mule.ftp.engine.api.ftp.command.FtpReadCommand;
import org.mule.ftp.engine.api.ftp.command.FtpRenameCommand;
import org.mule.ftp.engine.api.ftp.command.FtpWriteCommand;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lock.LockFactory;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mule.ftp.engine.api.exceptions.FileError.DISCONNECTED;
import static org.mule.ftp.engine.api.util.UriUtils.createUri;
import static org.mule.ftp.engine.api.ftp.FtpUtils.createUrl;
import static org.mule.ftp.engine.api.ftp.FtpUtils.getReplyCodeErrorMessage;
import static org.mule.ftp.engine.api.ftp.FtpUtils.normalizePath;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of {@link AbstractFileSystem} for files residing on a FTP server
 *
 * @since 1.0
 */
public class FtpFileSystem extends AbstractExternalFileSystem {

  private static final Logger LOGGER = getLogger(FtpFileSystem.class);
  private SingleFileListingMode singleFileListingMode = SingleFileListingMode.UNSET;

  private static String resolveBasePath(String basePath, FTPClient client) {
    if (isBlank(basePath)) {
      try {
        return client.printWorkingDirectory();
      } catch (Exception e) {
        LOGGER.error("FTP working dir was not specified and failed to resolve a default one", e);
        throw new MuleRuntimeException(createStaticMessage("FTP working dir was not specified and failed to resolve a default one"),
                                       e);
      }
    }
    return UriUtils.createUri("", basePath).getPath();
  }

  private final FTPClient client;
  private final CopyCommand copyCommand;
  private final CreateDirectoryCommand createDirectoryCommand;
  private final DeleteCommand deleteCommand;
  private final ListCommand listCommand;
  private final MoveCommand moveCommand;
  private final ReadCommand readCommand;
  private final RenameCommand renameCommand;
  private final WriteCommand writeCommand;
  private final LockFactory lockFactory;

  /**
   * Creates a new instance
   *
   * @param client a ready to use {@link FTPClient}
   */
  public FtpFileSystem(FTPClient client, String basePath, LockFactory lockFactory, SingleFileListingMode singleFileListingMode) {
    super(resolveBasePath(basePath, client));
    this.client = client;
    this.lockFactory = lockFactory;
    this.singleFileListingMode = singleFileListingMode;

    copyCommand = new FtpCopyCommand(this, client);
    createDirectoryCommand = new FtpCreateDirectoryCommand(this, client);
    deleteCommand = new FtpDeleteCommand(this, client);
    moveCommand = new FtpMoveCommand(this, client);
    readCommand = new FtpReadCommand(this, client);
    listCommand = new FtpListCommand(this, client, (FtpReadCommand) readCommand);
    renameCommand = new FtpRenameCommand(this, client);
    writeCommand = new FtpWriteCommand(this, client);
  }

  /**
   * Severs the connection by invoking {@link FTPClient#logout()} and {@link FTPClient#disconnect()} on the provided
   * {@link #client}.
   * <p>
   * Notice that {@link FTPClient#disconnect()} will be invoked even if {@link FTPClient#logout()} fails. This method will never
   * throw exception. Any errors will be logged.
   */
  public void disconnect() {
    try {
      client.logout();
    } catch (FTPConnectionClosedException e) {
      // this is valid and expected if the server closes the connection prematurely as a result of the logout... ignore
    } catch (Exception e) {
      LOGGER.error("Exception found trying to logout from ftp at {} ", toURL(createUri("")), e);
      LOGGER.debug(e.getMessage(), e);
    } finally {
      try {
        client.disconnect();
      } catch (Exception e) {
        LOGGER.error("Exception found trying to disconnect from ftp at {} ", toURL(createUri("")), e);
        LOGGER.debug(e.getMessage(), e);
      }
    }
  }

  public boolean isFeatureSupported(String command) {
    try {
      return client.hasFeature(command);
    } catch (IOException exception) {
      LOGGER.error(format("Unable to resolve if feature {} is supported.", command), exception);
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isConnected() {
    return client.isConnected();
  }

  /**
   * Validates the underlying connection to the remote server
   *
   * @return a {@link ConnectionValidationResult}
   */
  public ConnectionValidationResult validateConnection() {
    if (!isConnected()) {
      LOGGER.trace("Connection validation failed.");

      return failure("Connection is stale", new FTPConnectionException("Connection is stale", DISCONNECTED));
    }

    try {
      changeToBaseDir();
    } catch (Exception e) {
      LOGGER.error("Error occurred while changing to base directory {}", getBasePath(), e);
      return failure("Configured workingDir is unavailable", e);
    }
    return success();
  }

  /**
   * Sets the transfer mode on the {@link #client}
   *
   * @param mode a {@link FtpTransferMode}
   */
  public void setTransferMode(FtpTransferMode mode) {
    try {
      if (!client.setFileType(mode.getCode())) {
        throw new IOException(format("Failed to set %s transfer type. %s", mode.getDescription(),
                                     getReplyCodeErrorMessage(client.getReplyCode())));
      }
    } catch (Exception e) {
      LOGGER.error(format("Found exception trying to change transfer mode to %s. %s",
                          mode.getClass(),
                          getReplyCodeErrorMessage(client.getReplyCode())),
                   e);
      throw new MuleRuntimeException(createStaticMessage(format("Found exception trying to change transfer mode to %s. %s",
                                                                mode.getClass(),
                                                                getReplyCodeErrorMessage(client.getReplyCode()))),
                                     e);
    }
  }

  /**
   * Sets the data timeout property on the underlying {@link #client}
   *
   * @param timeout a timeout scalar
   * @param timeUnit a {@link TimeUnit} which qualifies the {@code timeout}
   */
  public void setResponseTimeout(Integer timeout, TimeUnit timeUnit) {
    client.setDataTimeout(new Long(timeUnit.toMillis(timeout)).intValue());
  }

  /**
   * If {@code passive} is {@code true} then the {@link #client} is set on passive mode. Otherwise is set on active mode.
   *
   * @param passive whether to go passive mode or not
   */
  public void setPassiveMode(boolean passive) {
    if (passive) {
      LOGGER.debug("Entering FTP passive mode");
      client.enterLocalPassiveMode();
    } else {
      LOGGER.debug("Entering FTP active mode");
      client.enterLocalActiveMode();
    }
  }

  /**
   * Returns an InputStream which obtains the content for the file of the given {@code filePayload}.
   * <p>
   * The invoked <b>MUST</b> make sure that the returned stream is closed in order for the underlying connection to be closed.
   *
   * @param filePayload a {@link FileAttributes} referencing to a FTP file
   * @return an {@link InputStream}
   */
  public InputStream retrieveFileContent(FileAttributes filePayload) {
    try {
      InputStream inputStream = client.retrieveFileStream(normalizePath(filePayload.getPath()));
      if (inputStream == null) {
        throw new FileNotFoundException(format("Could not retrieve content of file '%s' because it doesn't exist",
                                               filePayload.getPath()));
      }

      return inputStream;
    } catch (Exception e) {
      LOGGER.error(format("Exception was found trying to retrieve the contents of file '%s'. %s",
                          filePayload.getPath(),
                          getReplyCodeErrorMessage(client.getReplyCode())),
                   e);
      throw new MuleRuntimeException(createStaticMessage(format("Exception was found trying to retrieve the contents of file '%s'. %s",
                                                                filePayload.getPath(),
                                                                getReplyCodeErrorMessage(client.getReplyCode()))),
                                     e);
    }
  }

  /**
   * Awaits for the underlying {@link #client} to complete any pending commands. This is necessary for certain operations such as
   * write. Using the {@link #client} before that can result in unexpected behavior
   */
  public void awaitCommandCompletion() {
    try {
      if (!client.completePendingCommand()) {
        throw new IllegalStateException("Pending command did not complete");
      }
    } catch (IllegalStateException | IOException e) {
      LOGGER.error(format("Failed to complete pending command. %s",
                          getReplyCodeErrorMessage(client.getReplyCode())),
                   e);
      throw new MuleRuntimeException(createStaticMessage(format("Failed to complete pending command. %s",
                                                                getReplyCodeErrorMessage(client.getReplyCode()))),
                                     e);
    }
  }

  protected UriLock createLock(URI uri) {
    return new URLPathLock(toURL(uri), lockFactory);
  }

  private URL toURL(URI uri) {
    try {
      return createUrl(client, uri);
    } catch (MalformedURLException e) {
      LOGGER.error(format("Could not get URL for FTP server %s", uri.getHost()), e);
      throw new MuleRuntimeException(createStaticMessage("Could not get URL for FTP server"), e);
    }
  }

  /**
   * Changes the {@link #client}'s current working directory to the base path
   */
  @Override
  public void changeToBaseDir() {
    String basePath = getBasePath();
    if (basePath != null) {
      try {
        client.changeWorkingDirectory(normalizePath(createUri("/", getBasePath()).getPath()));
      } catch (IOException e) {
        LOGGER.error(format("Failed to perform CWD to the base directory '%s'", basePath), e);
        ConnectionException ce = new ConnectionException(e, client);
        throw new MuleRuntimeException(createStaticMessage(format("Failed to perform CWD to the base directory '%s'",
                                                                  basePath)),
                                       ce);
      }
    }
  }

  public FTPClient getClient() {
    return client;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ReadCommand getReadCommand() {
    return readCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ListCommand getListCommand() {
    return listCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WriteCommand getWriteCommand() {
    return writeCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CopyCommand getCopyCommand() {
    return copyCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected MoveCommand getMoveCommand() {
    return moveCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DeleteCommand getDeleteCommand() {
    return deleteCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected RenameCommand getRenameCommand() {
    return renameCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CreateDirectoryCommand getCreateDirectoryCommand() {
    return createDirectoryCommand;
  }

  /**
   * Obtains a {@link FtpFileAttributes} for the given {@code filePath}
   *
   * @param filePath the path to the file you want
   * @return a {@link FtpFileAttributes} or {@code null} if it doesn't exist
   */
  public FtpFileAttributes getFileAttributes(String filePath) {
    return ((FtpReadCommand) readCommand).getFile(filePath);
  }

  public void setSingleFileListingMode(SingleFileListingMode singleFileListingMode) {
    LOGGER.debug("Setting singleFileListingMode = {}", singleFileListingMode);
    this.singleFileListingMode = singleFileListingMode;
  }

  public SingleFileListingMode getSingleFileListingMode() {
    LOGGER.debug("Current singleFileListingMode = {}", singleFileListingMode);
    return this.singleFileListingMode;
  }
}

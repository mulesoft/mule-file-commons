/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.ftp.engine.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * {@link ConnectionException} implementation to declare connectivity errors in the connectors extending {@link FtpConnectorConfig}
 *
 * @since 1.0
 */
public class FTPConnectionException extends ConnectionException {

  public FTPConnectionException(String s) {
    super(s);
  }

  public FTPConnectionException(String message, FileError errors) {
    super(message, new ModuleException(message, errors));
  }

  public FTPConnectionException(String message, Throwable throwable, FileError fileError) {
    super(message, new ModuleException(fileError, throwable));
  }
}

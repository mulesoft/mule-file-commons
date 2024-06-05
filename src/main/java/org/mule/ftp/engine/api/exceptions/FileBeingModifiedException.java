/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.exceptions;

import org.mule.runtime.api.i18n.I18nMessage;

/**
 * This exception is thrown when at the moment of getting the actual content of a file, its size is still changing.
 *
 * @since 1.2.0
 */
public class FileBeingModifiedException extends FileReadException {

  /**
   * Creates a new instance with the specified detail {@code message}
   *
   * @param message the detail message
   */
  public FileBeingModifiedException(I18nMessage message) {
    super(message);
  }

  /**
   * Creates a new instance with the specified detail {@code message}
   *
   * @param message the detail message
   * @param cause
   */
  public FileBeingModifiedException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new instance
   *
   * @param cause
   */
  public FileBeingModifiedException(Throwable cause) {
    super(cause);
  }
}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.exceptions;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessage;

/**
 * {@link MuleRuntimeException} to be thrown when there is a problem while trying to read the content of a file lazily.
 * Notice that this are not {@link org.mule.runtime.extension.api.exception.ModuleException}s so they do not have a Mule Error
 * associated, as these exceptions are thrown outside of the Connector Operation/Source execution.
 *
 * @since 1.2.0
 */
public abstract class FileReadException extends MuleRuntimeException {

  /**
   * Creates a new instance with the specified detail {@code message}
   *
   * @param message the detail message
   */
  public FileReadException(I18nMessage message) {
    super(message);
  }

  /**
   * Creates a new instance with the specified detail {@code message}
   *
   * @param message the detail message
   * @param cause
   */
  public FileReadException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new instance
   *
   * @param cause
   */
  public FileReadException(Throwable cause) {
    super(cause);
  }
}

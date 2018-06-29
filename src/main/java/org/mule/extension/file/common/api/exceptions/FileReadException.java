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
 * @since 1.2.0
 */
public class FileReadException extends MuleRuntimeException {

  public FileReadException(I18nMessage message) {
    super(message);
  }

  public FileReadException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public FileReadException(Throwable cause) {
    super(cause);
  }
}

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.stream;

import java.io.IOException;
import java.io.InputStream;

public class ExceptionInputStream extends InputStream {

  RuntimeException exception;

  public ExceptionInputStream(RuntimeException exception) {
    this.exception = exception;
  }

  @Override
  public int read() throws IOException {
    throw exception;
  }
}

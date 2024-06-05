/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class LoggingOutputStream extends OutputStream {

  Consumer<String> operation;
  String buffer = "";

  public LoggingOutputStream(Consumer<String> operation) {
    this.operation = operation;
  }

  @Override
  public void write(int b) throws IOException {
    byte[] bytes = new byte[1];
    bytes[0] = (byte) (b & 0xff);
    buffer += new String(bytes);
    if (buffer.endsWith("\n")) {
      operation.accept(buffer.substring(0, buffer.length() - 1));
      buffer = "";
    }
  }
}

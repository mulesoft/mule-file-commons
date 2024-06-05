/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueProvider;

import java.util.Set;

import static org.mule.runtime.extension.api.values.ValueBuilder.getValuesFor;

/**
 * A static {@link ValueProvider} implementation for FTP Connection provider which hints the available control encodings.
 *
 * @since 1.5.0
 */
public class ControlEncodingValueProvider implements ValueProvider {

  private static final Set<Value> controlEncodings = getValuesFor("ISO-8859-1", "UTF-8");

  @Override
  public Set<Value> resolve() {
    return controlEncodings;
  }
}

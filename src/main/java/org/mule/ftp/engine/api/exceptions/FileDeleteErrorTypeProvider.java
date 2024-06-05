/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.exceptions;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import org.mule.ftp.engine.api.BaseFileSystemOperations;
import org.mule.ftp.engine.api.FileSystem;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * Errors that can be thrown in the {@link BaseFileSystemOperations#delete(FileSystem, String, CoreEvent)} operation.
 * 
 * @since 1.0
 */
public class FileDeleteErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return unmodifiableSet(new HashSet<>(asList(FileError.ILLEGAL_PATH, FileError.ACCESS_DENIED)));
  }
}


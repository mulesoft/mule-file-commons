/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.exceptions;

import static org.mule.extension.file.common.api.exceptions.FileError.FILE_ALREADY_EXISTS;
import static org.mule.extension.file.common.api.exceptions.FileError.ILLEGAL_PATH;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import org.mule.extension.file.common.api.BaseFileSystemOperations;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * Errors that can be thrown both in
 * {@link BaseFileSystemOperations#copy(FileConnectorConfig, FileSystem, String, String, boolean, boolean, CoreEvent)} or
 * {@link BaseFileSystemOperations#move(FileConnectorConfig, FileSystem, String, String, boolean, boolean, CoreEvent)} operation.
 *
 * @since 1.0
 */
public class FileCopyErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return unmodifiableSet(new HashSet<>(asList(ILLEGAL_PATH, FILE_ALREADY_EXISTS)));
  }
}


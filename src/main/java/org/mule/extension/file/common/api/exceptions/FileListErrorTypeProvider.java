/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.exceptions;

import static org.mule.extension.file.common.api.exceptions.FileError.ACCESS_DENIED;
import static org.mule.extension.file.common.api.exceptions.FileError.ILLEGAL_PATH;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.matcher.FileMatcher;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.BaseFileSystemOperations;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Errors that can be thrown in the
 * {@link BaseFileSystemOperations#list(FileConnectorConfig, FileSystem, String, boolean, Message, FileMatcher)}
 * operation.
 *
 * @since 1.0
 */
public class FileListErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ILLEGAL_PATH, ACCESS_DENIED)));
  }
}


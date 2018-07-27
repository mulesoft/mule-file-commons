/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api.source;

import org.mule.extension.file.common.api.AbstractFileSystem;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static java.lang.String.format;
import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;

/**
 * Groups post processing action parameters
 *
 * @since 1.1.2, 1.2.0
 */
public abstract class AbstractPostActionGroup {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostActionGroup.class);

  public abstract boolean isAutoDelete();

  public abstract String getMoveToDirectory();

  public abstract String getRenameTo();

  public abstract boolean isApplyPostActionWhenFailed();

  public void validateSelf() throws IllegalArgumentException {
    if (isAutoDelete()) {
      if (getMoveToDirectory() != null) {
        throw new IllegalArgumentException(format("The autoDelete parameter was set to true, but the value '%s' was given to the "
            + "moveToDirectory parameter. These two are contradictory.", getMoveToDirectory()));
      } else if (getRenameTo() != null)
        throw new IllegalArgumentException(format("The autoDelete parameter was set to true, but the value '%s' was given to the "
            + "renameTo parameter. These two are contradictory.", getRenameTo()));
    }
    if (getMoveToDirectory() == null && getRenameTo() != null) {
      throw new IllegalArgumentException(format("The value '%s' was given to the renameTo parameter, but the moveToDirectory parameter"
          + " was not set. renameTo is only used to change the name to the file when it is moved to " +
          "the moveToDirectory.", getRenameTo()));
    }
  }

  public void apply(AbstractFileSystem fileSystem, FileAttributes fileAttributes, FileConnectorConfig config) {
    if (LOGGER.isTraceEnabled()) {
      try {
        validateSelf();
      } catch (IllegalArgumentException e) {
        LOGGER.trace(e.getMessage());
      }
    }

    boolean moved = false;
    try {
      if (getMoveToDirectory() != null) {
        fileSystem.move(config, fileAttributes.getPath(), getMoveToDirectory(), false, true,
                        getRenameTo());
        moved = true;
      }
    } catch (FileAlreadyExistsException e) {
      if (!isAutoDelete()) {
        String moveToFileName = getRenameTo() == null ? fileAttributes.getName() : getRenameTo();
        String moveToPath = Paths.get(getMoveToDirectory()).resolve(moveToFileName).toString();
        LOGGER.warn(String.format("A file with the same name was found when trying to move '%s' to '%s'" +
            ". The file '%s' was not sent to the moveTo directory and it remains on the poll directory.",
                                  fileAttributes.getPath(), moveToPath, fileAttributes.getPath()));
        throw e;
      }
    } finally {
      if (isAutoDelete() && !moved) {
        fileSystem.delete(fileAttributes.getPath());
      }
    }
  }
}

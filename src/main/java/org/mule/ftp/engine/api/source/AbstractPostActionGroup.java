/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.source;

import org.mule.ftp.engine.api.AbstractFileSystem;
import org.mule.ftp.engine.api.FileAttributes;
import org.mule.ftp.engine.api.FileConnectorConfig;
import org.mule.ftp.engine.api.exceptions.FileAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static java.lang.String.format;

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

  public abstract boolean getOverwrite();

  public abstract boolean isApplyPostActionWhenFailed();

  public void validateSelf() throws IllegalArgumentException {
    if (isAutoDelete()) {
      if (getMoveToDirectory() != null) {
        throw new IllegalArgumentException(format("The autoDelete parameter was set to true, but the value '%s' was given to the "
            + "moveToDirectory parameter. These two are contradictory.", getMoveToDirectory()));
      } else if (getRenameTo() != null) {
        throw new IllegalArgumentException(format("The autoDelete parameter was set to true, but the value '%s' was given to the "
            + "renameTo parameter. These two are contradictory.", getRenameTo()));
      }
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

    boolean movedOrRenamed = false;
    try {
      if (getMoveToDirectory() != null) {
        fileSystem.move(config, fileAttributes.getPath(), getMoveToDirectory(), getOverwrite(), true,
                        getRenameTo());
        movedOrRenamed = true;
      } else if (getRenameTo() != null) {
        fileSystem.rename(fileAttributes.getPath(), getRenameTo(), getOverwrite());
        movedOrRenamed = true;
      }
    } catch (FileAlreadyExistsException e) {
      if (!isAutoDelete()) {
        if (getMoveToDirectory() == null) {
          LOGGER.warn(format("A file with the same name was found when trying to rename '%s' to '%s'" +
              ". The file '%s' was not renamed and it remains on the poll directory.",
                             fileAttributes.getName(), getRenameTo(), fileAttributes.getPath()));
        } else {
          String moveToFileName = getRenameTo() == null ? fileAttributes.getName() : getRenameTo();
          String moveToPath = Paths.get(getMoveToDirectory()).resolve(moveToFileName).toString();
          LOGGER.warn(format("A file with the same name was found when trying to move '%s' to '%s'" +
              ". The file '%s' was not sent to the moveTo directory and it remains on the poll directory.",
                             fileAttributes.getPath(), moveToPath, fileAttributes.getPath()));
        }
        throw e;
      }
    } finally {
      if (isAutoDelete() && !movedOrRenamed) {
        fileSystem.delete(fileAttributes.getPath());
      }
    }
  }
}

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.ftp.engine.api.source.AbstractPostActionGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Path;

import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;

/**
 * Groups post processing action parameters
 *
 * @since 1.1
 */
public class PostActionGroup extends AbstractPostActionGroup {

  /**
   * Whether each file should be deleted after processing or not
   */
  @Parameter
  @Optional(defaultValue = "false")
  private boolean autoDelete = false;

  /**
   * If provided, each processed file will be moved to a directory pointed by this path.
   */
  @Parameter
  @Optional
  @Path(type = DIRECTORY, location = EXTERNAL)
  private String moveToDirectory;

  /**
   * This parameter works in tandem with {@code moveToDirectory}. Use this parameter to enter the name under which the file should
   * be moved. Do not set this parameter if {@code moveToDirectory} hasn't been set as well.
   */
  @Parameter
  @Optional
  private String renameTo;

  /**
   * Whether any of the post actions ({@code autoDelete} and {@code moveToDirectory}) should also be applied in case the
   * file failed to be processed. If set to {@code false}, no failed files will be moved nor deleted.
   */
  @Parameter
  @Optional(defaultValue = "true")
  private boolean applyPostActionWhenFailed = true;

  /**
   * Enables you to overwrite the target file when the destination file has the same name
   */
  @Parameter
  @Optional(defaultValue = "false")
  private boolean overwrite = false;


  public PostActionGroup() {}

  public PostActionGroup(boolean autoDelete, String moveToDirectory, String renameTo, boolean applyPostActionWhenFailed) {
    this(autoDelete, moveToDirectory, renameTo, applyPostActionWhenFailed, false);
  }

  public PostActionGroup(boolean autoDelete, String moveToDirectory, String renameTo, boolean applyPostActionWhenFailed,
                         boolean overwrite) {
    this.autoDelete = autoDelete;
    this.moveToDirectory = moveToDirectory;
    this.renameTo = renameTo;
    this.applyPostActionWhenFailed = applyPostActionWhenFailed;
    this.overwrite = overwrite;
  }

  public boolean isAutoDelete() {
    return autoDelete;
  }

  public String getMoveToDirectory() {
    return moveToDirectory;
  }

  public String getRenameTo() {
    return renameTo;
  }

  public boolean isApplyPostActionWhenFailed() {
    return applyPostActionWhenFailed;
  }

  public boolean getOverwrite() {
    return overwrite;
  }
}

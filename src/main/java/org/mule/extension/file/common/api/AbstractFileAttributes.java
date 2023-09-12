/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file.common.api;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Base class for implementations of {@link FileAttributes}
 *
 * @since 1.0
 */
public abstract class AbstractFileAttributes implements FileAttributes, Serializable {

  private static final long serialVersionUID = 3249780732227598L;

  @Parameter
  protected final String path;

  @Parameter
  private String fileName;

  /**
   * Creates a new instance
   *
   * @param path a {@link Path} pointing to the represented file
   */
  protected AbstractFileAttributes(Path path) {
    this.path = path.toString();
    this.fileName = path.getFileName() != null ? path.getFileName().toString() : "";
  }

  /**
   * Creates a new instance
   *
   * @param uri a {@link URI} pointing to the represented file
   */
  protected AbstractFileAttributes(URI uri) {
    this.path = uri.getPath();
    String name = FilenameUtils.getName(uri.getPath());
    this.fileName = name != null ? name : "";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPath() {
    return path;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return fileName;
  }

  /**
   * @return The file's name
   */
  public String getFileName() {
    return fileName;
  }

  protected LocalDateTime asDateTime(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, SHORT_PREFIX_STYLE);
  }
}

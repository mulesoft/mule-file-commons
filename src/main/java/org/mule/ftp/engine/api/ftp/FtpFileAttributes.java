/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.mule.ftp.engine.api.AbstractFileAttributes;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.net.URI;
import java.time.LocalDateTime;

import static org.mule.ftp.engine.api.ftp.FtpUtils.normalizePath;

/**
 * Metadata about a file in a FTP server
 *
 * @since 1.0
 */
public class FtpFileAttributes extends AbstractFileAttributes {

  @Parameter
  @Optional
  private LocalDateTime timestamp;

  @Parameter
  // TODO MULE-15337: Remove redundant 'name' attribute in next major version,
  // since it represents the same that 'fileName' from AbstractFileAttributes.
  private String name;

  @Parameter
  private long size;

  @Parameter
  private boolean regularFile;

  @Parameter
  private boolean directory;

  @Parameter
  private boolean symbolicLink;

  /**
   * Creates a new instance
   *
   * @param uri the file's {@link URI}
   * @param ftpFile the {@link FTPFile} which represents the file on the FTP server
   */



  public FtpFileAttributes(URI uri, FTPFile ftpFile) {
    super(uri);
    timestamp = ftpFile.getTimestamp() != null ? asDateTime(ftpFile.getTimestamp().toInstant()) : null;
    // TODO MULE-15337: Remove redundant 'name' attribute in next major version
    name = ftpFile.getName() != null ? ftpFile.getName() : "";
    size = ftpFile.getSize();
    regularFile = ftpFile.isFile();
    directory = ftpFile.isDirectory();
    symbolicLink = ftpFile.isSymbolicLink();
  }


  public FtpFileAttributes() {
    super(createDefaultUri());
    timestamp = null;
    name = "";
    size = 0;
    regularFile = false;
    directory = false;
    symbolicLink = false;
  }

  private static URI createDefaultUri() {
    return URI.create("file:///defaultPath");
  }

  /**
   * @return The last time the file was modified, or {@code null} if such information is not available.
   */
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRegularFile() {
    return regularFile;
  }

  public boolean getRegularFile() {
    return regularFile;
  }

  public void setRegularFile(boolean regularFile) {
    this.regularFile = regularFile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDirectory() {
    return directory;
  }

  public boolean getDirectory() {
    return directory;
  }

  public void setDirectory(boolean directory) {
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSymbolicLink() {
    return symbolicLink;
  }

  public boolean getSymbolicLink() {
    return symbolicLink;
  }

  public void setSymbolicLink(boolean symbolicLink) {
    this.symbolicLink = symbolicLink;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPath() {
    return normalizePath(super.getPath());
  }

}

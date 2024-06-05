/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

/**
 * Flag to indicate if the FTP server supports the using initiateListParsing to list a single file.
 */
public enum SingleFileListingMode {
  UNSET, SUPPORTED, UNSUPPORTED
}

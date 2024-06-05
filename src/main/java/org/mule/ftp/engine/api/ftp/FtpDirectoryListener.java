/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.ftp.engine.api.ftp;

import org.mule.ftp.engine.api.matcher.NullFilePayloadPredicate;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.execution.OnTerminate;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollContext.PollItemStatus;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.InputStream;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.mule.ftp.engine.api.FileDisplayConstants.MATCHER;
import static org.mule.runtime.core.api.util.ExceptionUtils.extractConnectionException;
import static org.mule.runtime.core.api.util.IOUtils.closeQuietly;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.runtime.source.PollContext.PollItemStatus.SOURCE_STOPPING;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls a directory looking for files that have been created on it. One message will be generated for each file that is found.
 * <p>
 * The key part of this functionality is how to determine that a file is actually new. There're three strategies for that:
 * <ul>
 * <li>Set the <i>autoDelete</i> parameter to <i>true</i>: This will delete each processed file after it has been processed,
 * causing all files obtained in the next poll to be necessarily new</li>
 * <li>Set <i>moveToDirectory</i> parameter: This will move each processed file to a different directory after it has been
 * processed, achieving the same effect as <i>autoDelete</i> but without loosing the file</li>
 * <li></li>
 * <li>Use the <i>watermarkEnabled</i> parameter to only pick files that have been created or updated after the last poll was
 * executed.</li>
 * </ul>
 * <p>
 * A matcher can also be used for additional filtering of files.
 *
 * @since 1.1
 */
@MediaType(value = ANY, strict = false)
@DisplayName("On New or Updated File")
@Summary("Triggers when a new file is created in a directory")
@Alias("listener")
// TODO: MULE-13940 - add mimeType here too
public class FtpDirectoryListener extends PollingSource<InputStream, FtpFileAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FtpDirectoryListener.class);
  private static final String ATTRIBUTES_CONTEXT_VAR = "attributes";
  private static final String POST_PROCESSING_GROUP_NAME = "Post processing action";

  @Config
  private FtpConnectorConfig config;

  @Connection
  private ConnectionProvider<FtpFileSystem> fileSystemProvider;

  /**
   * The directory on which polled files are contained
   */
  @Parameter
  @Optional
  private String directory;

  /**
   * Whether or not to also files contained in sub directories.
   */
  @Parameter
  @Optional(defaultValue = "true")
  @Summary("Whether or not to also catch files created on sub directories")
  private boolean recursive = true;

  /**
   * A matcher used to filter events on files which do not meet the matcher's criteria
   */
  @Parameter
  @Optional
  @Alias("matcher")
  @DisplayName(MATCHER)
  private FtpFileMatcher predicateBuilder;

  /**
   * Controls whether or not to do watermarking, and if so, if the watermark should consider the file's modification or creation
   * timestamps
   */
  @Parameter
  @Optional(defaultValue = "false")
  private boolean watermarkEnabled = false;

  /**
   * Wait time in milliseconds between size checks to determine if a file is ready to be read. This allows a file write to
   * complete before processing. You can disable this feature by omitting a value. When enabled, Mule performs two size checks
   * waiting the specified time between calls. If both checks return the same value, the file is ready to be read.
   */
  @Parameter
  @ConfigOverride
  @Summary("Wait time in milliseconds between size checks to determine if a file is ready to be read.")
  private Long timeBetweenSizeCheck;

  /**
   * A {@link TimeUnit} which qualifies the {@link #timeBetweenSizeCheck} attribute.
   */
  @Parameter
  @ConfigOverride
  @Summary("Time unit to be used in the wait time between size checks")
  private TimeUnit timeBetweenSizeCheckUnit;

  private URI directoryUri;
  private Predicate<FtpFileAttributes> matcher;
  private FtpFileSystem ftpFileSystemConnection;

  @Override
  protected void doStart() {
    refreshMatcher();
    directoryUri = resolveRootUri();
  }

  @Override
  protected void doStop() {

  }

  @OnSuccess
  public void onSuccess(@ParameterGroup(name = POST_PROCESSING_GROUP_NAME) PostActionGroup postAction,
                        SourceCallbackContext ctx) {
    postAction(postAction, ctx);
    returnConnection();
  }

  @OnError
  public void onError(@ParameterGroup(name = POST_PROCESSING_GROUP_NAME) PostActionGroup postAction,
                      SourceCallbackContext ctx) {
    if (postAction.isApplyPostActionWhenFailed()) {
      postAction(postAction, ctx);
      returnConnection();
    }
  }

  @OnTerminate
  public void onTerminate(SourceCallbackContext ctx) {
    returnConnection();
  }

  @Override
  public void poll(PollContext<InputStream, FtpFileAttributes> pollContext) {
    refreshMatcher();
    if (pollContext.isSourceStopping()) {
      return;
    }

    try {
      openConnection();
    } catch (Exception e) {
      extractConnectionException(e).ifPresent((connectionException) -> pollContext.onConnectionException(connectionException));
      LOGGER.error(format("Could not obtain connection while trying to poll directory '%s'. %s", directoryUri.getPath(),
                          e.getMessage()),
                   e);

      return;
    }

    try {
      List<Result<InputStream, FtpFileAttributes>> files =
          ftpFileSystemConnection
              .list(config, directoryUri.getPath(), recursive, matcher,
                    config.getTimeBetweenSizeCheckInMillis(timeBetweenSizeCheck, timeBetweenSizeCheckUnit).orElse(null));

      if (files.isEmpty()) {
        return;
      }
      for (Result<InputStream, FtpFileAttributes> file : files) {

        FtpFileAttributes attributes = file.getAttributes().orElse(null);

        if (attributes == null || attributes.isDirectory()) {
          continue;
        }

        if (!matcher.test(attributes)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Skipping file '{}' because the matcher rejected it", attributes.getPath());
          }
          return;
        }

        if (!processFile(file, pollContext)) {
          break;
        }
      }
    } catch (Exception e) {
      LOGGER.error(format("Found exception trying to poll directory '%s'. Will try again on the next poll. ",
                          directoryUri.getPath(), e.getMessage()),
                   e);
    } finally {
      returnConnection();
    }
  }

  private void refreshMatcher() {
    matcher = predicateBuilder != null ? predicateBuilder.build() : new NullFilePayloadPredicate<>();
  }

  @Override
  public void onRejectedItem(Result<InputStream, FtpFileAttributes> result, SourceCallbackContext callbackContext) {
    closeQuietly(result.getOutput());
  }

  private FtpFileSystem openConnection() throws Exception {
    if (ftpFileSystemConnection == null)
      ftpFileSystemConnection = fileSystemProvider.connect();
    try {
      ftpFileSystemConnection.changeToBaseDir();
    } catch (Exception e) {
      returnConnection();
      throw e;
    }
    return ftpFileSystemConnection;
  }

  private boolean processFile(Result<InputStream, FtpFileAttributes> file,
                              PollContext<InputStream, FtpFileAttributes> pollContext) {
    FtpFileAttributes attributes = file.getAttributes().get();
    String fullPath = attributes.getPath();

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Processing file {}", attributes);
    }

    PollItemStatus status = pollContext.accept(item -> {
      SourceCallbackContext ctx = item.getSourceCallbackContext();
      Result<InputStream, FtpFileAttributes> result = null;
      try {
        ctx.addVariable(ATTRIBUTES_CONTEXT_VAR, attributes);
        item.setResult(file);
        item.setId(attributes.getPath());
        if (watermarkEnabled) {
          if (attributes.getTimestamp() != null) {
            // We are truncating this value to maintain watermark behaviour from previous versions.
            item.setWatermark(attributes.getTimestamp().truncatedTo(ChronoUnit.MINUTES));
          } else {
            LOGGER.warn(format("Use of watermark for files processing is enabled, but file [%s] does not have the"
                + " corresponding modification timestamp. Watermark ignored for this file.",
                               fullPath));
          }
        }
      } catch (Throwable t) {
        LOGGER.error(format("Found file '%s' but found exception trying to dispatch it for processing. %s",
                            fullPath, t.getMessage()),
                     t);
        if (result != null) {
          onRejectedItem(result, ctx);
        }
      }
    });

    return status != SOURCE_STOPPING;
  }

  synchronized private void postAction(PostActionGroup postAction, SourceCallbackContext ctx) {
    ctx.<FtpFileAttributes>getVariable(ATTRIBUTES_CONTEXT_VAR).ifPresent(attrs -> {
      try {
        postAction.apply(openConnection(), attrs, config);
      } catch (Exception e) {
        LOGGER
            .error("An error occurred while retrieving a connection to apply the post processing action to the file {} , it was neither moved nor deleted.",
                   attrs.getPath());
      }
    });
  }

  private URI resolveRootUri() {
    try {
      return new OnNewFileCommand(openConnection()).resolveRootUri(directory);
    } catch (Exception e) {
      throw new MuleRuntimeException(I18nMessageFactory.createStaticMessage(
                                                                            format("Could not resolve path to directory '%s'. %s",
                                                                                   directory, e.getMessage())),
                                     e);
    }
  }

  private synchronized void returnConnection() {
    if (ftpFileSystemConnection != null) {
      fileSystemProvider.disconnect(ftpFileSystemConnection);
      ftpFileSystemConnection = null;
    }
  }
}

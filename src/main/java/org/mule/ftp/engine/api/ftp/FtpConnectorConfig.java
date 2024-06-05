package org.mule.ftp.engine.api.ftp;

import org.mule.ftp.engine.api.FileConnectorConfig;
import org.mule.runtime.core.api.connector.ConnectionManager;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

public abstract class FtpConnectorConfig extends FileConnectorConfig {

  /**
   * Wait time between size checks to determine if a file is ready to be read. This allows a file write to complete before
   * processing. If no value is provided, the check will not be performed. When enabled, Mule performs two size checks waiting the
   * specified time between calls. If both checks return the same value, the file is ready to be read.This attribute works in
   * tandem with {@link #timeBetweenSizeCheckUnit}.
   */
  @Parameter
  @Placement(tab = ADVANCED_TAB)
  @Summary("Wait time between size checks to determine if a file is ready to be read.")
  @Optional
  private Long timeBetweenSizeCheck;

  /**
   * A {@link TimeUnit} which qualifies the {@link #timeBetweenSizeCheck} attribute.
   * <p>
   * Defaults to {@code MILLISECONDS}
   */
  @Parameter
  @Placement(tab = ADVANCED_TAB)
  @Optional(defaultValue = "MILLISECONDS")
  @Summary("Time unit to be used in the wait time between size checks")
  private TimeUnit timeBetweenSizeCheckUnit;

  @Inject
  private ConnectionManager connectionManager;


  public ConnectionManager getConnectionManager() {
    return connectionManager;
  }
}

/*
Copyright 2011-2012 Selenium committers
Copyright 2011-2012 Software Freedom Conservancy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.openqa.selenium.opera;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.Beta;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Manages the life and death of a operadriver server.
 */
public class OperaDriverService extends DriverService {

  /**
   * System property that defines the location of the operadriver executable that will be used by
   * the {@link #createDefaultService() default service}.
   */
  public static final String OPERA_DRIVER_EXE_PROPERTY = "webdriver.opera.driver";

  /**
   * System property that defines the location of the log that will be written by
   * the {@link #createDefaultService() default service}.
   */
  public final static String OPERA_DRIVER_LOG_PROPERTY = "webdriver.opera.logfile";

  /**
   * Boolean system property that defines whether the OperaDriver executable should be started
   * with verbose logging.
   */
  public static final String OPERA_DRIVER_VERBOSE_LOG_PROPERTY =
      "webdriver.opera.verboseLogging";

  /**
   * Boolean system property that defines whether the OperaDriver executable should be started
   * in silent mode.
   */
  public static final String OPERA_DRIVER_SILENT_OUTPUT_PROPERTY =
      "webdriver.opera.silentOutput";

  /**
   *
   * @param executable The operadriver executable.
   * @param port Which port to start the operadriver on.
   * @param args The arguments to the launched server.
   * @param environment The environment for the launched server.
   * @throws IOException If an I/O error occurs.
   */
  public OperaDriverService(File executable, int port, ImmutableList<String> args,
                            ImmutableMap<String, String> environment) throws IOException {
    super(executable, port, args, environment);
  }

  /**
   * Configures and returns a new {@link OperaDriverService} using the default configuration. In
   * this configuration, the service will use the operadriver executable identified by the
   * {@link #OPERA_DRIVER_EXE_PROPERTY} system property. Each service created by this method will
   * be configured to use a free port on the current system.
   *
   * @return A new OperaDriverService using the default configuration.
   */
  public static OperaDriverService createDefaultService() {
    return new Builder().usingAnyFreePort().build();
  }

  /**
   * Builder used to configure new {@link OperaDriverService} instances.
   */
  public static class Builder extends DriverService.Builder<OperaDriverService> {

    private boolean verbose = Boolean.getBoolean(OPERA_DRIVER_VERBOSE_LOG_PROPERTY);
    private boolean silent = Boolean.getBoolean(OPERA_DRIVER_SILENT_OUTPUT_PROPERTY);

    /**
     * Sets which driver executable the builder will use.
     *
     * @param file The executable to use.
     * @return A self reference.
     */
    public Builder usingDriverExecutable(File file) {
      super.usingDriverExecutable(file);
      return this;
    }

    /**
     * Sets which port the driver server should be started on. A value of 0 indicates that any
     * free port may be used.
     *
     * @param port The port to use; must be non-negative.
     * @return A self reference.
     */
    public Builder usingPort(int port) {
      super.usingPort(port);
      return this;
    }

    /**
     * Configures the driver server to start on any available port.
     *
     * @return A self reference.
     */
    public Builder usingAnyFreePort() {
      super.usingAnyFreePort();
      return this;
    }

    /**
     * Defines the environment for the launched driver server. These
     * settings will be inherited by every browser session launched by the
     * server.
     *
     * @param environment A map of the environment variables to launch the
     *     server with.
     * @return A self reference.
     */
    @Beta
    public Builder withEnvironment(Map<String, String> environment) {
      super.withEnvironment(environment);
      return this;
    }

    /**
     * Configures the driver server to write log to the given file.
     *
     * @param logFile A file to write log to.
     * @return A self reference.
     */
    public Builder withLogFile(File logFile) {
      super.withLogFile(logFile);
      return this;
    }

    /**
     * Configures the driver server verbosity.
     *
     * @param verbose true for verbose output, false otherwise.
     * @return A self reference.
    */
    public Builder withVerbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    /**
     * Configures the driver server for silent output.
     *
     * @param silent true for silent output, false otherwise.
     * @return A self reference.
    */
    public Builder withSilent(boolean silent) {
      this.silent = silent;
      return this;
    }

    @Override
    protected File findDefaultExecutable() {
      return findExecutable("operadriver", OPERA_DRIVER_EXE_PROPERTY,
                            "https://github.com/operasoftware/operachromiumdriver",
                            "https://github.com/operasoftware/operachromiumdriver/releases");
    }

    @Override
    protected ImmutableList<String> createArgs() {
      if (getLogFile() == null) {
        String logFilePath = System.getProperty(OPERA_DRIVER_LOG_PROPERTY);
        if (logFilePath != null) {
          withLogFile(new File(logFilePath));
        }
      }

      ImmutableList.Builder<String> argsBuilder = ImmutableList.builder();
      argsBuilder.add(String.format("--port=%d", getPort()));
      if (getLogFile() != null) {
        argsBuilder.add(String.format("--log-path=%s", getLogFile().getAbsolutePath()));
      }
      if (verbose) {
        argsBuilder.add("--verbose");
      }
      if (silent) {
        argsBuilder.add("--silent");
      }

      return argsBuilder.build();
    }

    @Override
    protected OperaDriverService createDriverService(File exe, int port,
                                                      ImmutableList<String> args,
                                                      ImmutableMap<String, String> environment) {
      try {
        return new OperaDriverService(exe, port, args, environment);
      } catch (IOException e) {
        throw new WebDriverException(e);
      }
    }
  }
}

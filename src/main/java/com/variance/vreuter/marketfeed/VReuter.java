/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed;

import com.anosym.utilities.FormattedCalendar;
import com.variance.vreuter.marketfeed.property.VReuterProperty;
import java.io.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author Administrator
 */
@Singleton
@Startup
public class VReuter implements VReuterRemote {

  private class VPrintStream extends OutputStream {

    private File file;
    private FileOutputStream fileOutputStream;
    private int index = 0;
    private String tmpName;
    private String tmpExt;

    public VPrintStream(File file) {
      this.file = file;
      String tmp = file.getAbsolutePath();
      tmpName = tmp.substring(0, tmp.lastIndexOf('.'));
      tmpExt = tmp.substring(tmp.lastIndexOf('.'));
      open();
    }

    private void open() {
      if (fileOutputStream != null) {
        try {
          fileOutputStream.close();
        } catch (Exception ex) {
        }
      }
      try {
        fileOutputStream = new FileOutputStream(file, true);
      } catch (FileNotFoundException ex) {
      }
    }

    @Override
    public void write(int b) throws IOException {
      if (fileOutputStream != null) {
        boolean doOpen = false;
        while (file.length() > (1024 * 1024)) {
          String name = tmpName + (index++) + tmpExt;
          file = new File(name);
          doOpen = true;
        }
        if (doOpen) {
          open();
        }
        fileOutputStream.write(b);
      }
    }
  }
  private static final String PROPERTY_FILE_PATH = File.separator + "reuters" + File.separator + "vreuter.xml";
  private VReuterProperty reuterProperties;
  private OutputStream errorLogging;
  private OutputStream reportLogging;
  private ResourceManager<VReuterProperty> resourceManager;
  private volatile boolean updateProperties = false;
  private volatile boolean stopped = false;

  @PostConstruct
  @Override
  public void onStart() {
    errorLogging = System.err;
    reportLogging = System.out;
    resourceManager = new XmlFileResourceManager<VReuterProperty>();
    reuterProperties = resourceManager.load(PROPERTY_FILE_PATH, new VReuterProperty());
    checkLogging();
  }

  @Asynchronous
  @Schedule(hour = "*", minute = "*/1", persistent = false)
  private synchronized void updateProperties() {
    try {
      System.out.println("Checking asynchrnous update of reuter properties.............");
      //if not called to update properties,
      //wait until properties change
      if (updateProperties && !stopped && resourceManager != null) {
        resourceManager.persist(reuterProperties, PROPERTY_FILE_PATH);
      }
    } finally {
      updateProperties = false;
    }
  }

  @Override
  public synchronized void updateSynchronously() {
    updateProperties = true;
    updateProperties();
  }

  private void checkLogging() {
    if (reuterProperties != null && reuterProperties.getLoggingProperty().isEnableErrorLogging()) {
      try {
        File errorFile = new File(reuterProperties.getFileDirectory(), reuterProperties.getLoggingProperty().getErrorLog());
        if (!errorFile.getParentFile().exists()) {
          try {
            errorFile.getParentFile().mkdirs();
          } catch (Exception e) {
          }
        }
        System.setErr(new PrintStream(new VPrintStream(errorFile)) {
          @Override
          public void println(String x) {
            super.println(FormattedCalendar.toISOString(Calendar.getInstance()) + ": " + x);
          }

          @Override
          public void println(Object x) {
            super.println(FormattedCalendar.toISOString(Calendar.getInstance()) + ": " + x);
          }
        });
      } catch (Exception ex) {
        Logger.getLogger(VReuter.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else if (errorLogging != null) {
      System.setErr((PrintStream) errorLogging);
    }
    if (reuterProperties != null && reuterProperties.getLoggingProperty().isEnableReportLogging()) {
      try {
        File reportFile = new File(reuterProperties.getFileDirectory(), reuterProperties.getLoggingProperty().getReportLog());
        try {
          if (!reportFile.getParentFile().exists()) {
            reportFile.getParentFile().mkdirs();
          }
        } catch (Exception e) {
        }
        System.setOut(new PrintStream(new VPrintStream(reportFile)) {
          @Override
          public void println(String x) {
            super.println(FormattedCalendar.toISOString(Calendar.getInstance()) + ": " + x);
          }

          @Override
          public void println(Object x) {
            super.println(FormattedCalendar.toISOString(Calendar.getInstance()) + ": " + x);
          }
        });
      } catch (Exception ex) {
        Logger.getLogger(VReuter.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else if (reportLogging != null) {
      System.setOut((PrintStream) reportLogging);
    }
  }

  @PreDestroy
  @Override
  public void onDestroy() {
    synchronized (VReuter.this) {
      //it is important we do this, otherwise the last updates may not take effect.
      stopped = true;
      VReuter.this.notifyAll();
    }
  }

  /**
   * Called by external entities to notify the Reuter Manager that the properties have changed and
   * needs to be updated There is no guarantee how this update is done, and it may not occur
   * immediately. The returning of this method does not indicate a success.
   */
  @Override
  public void doUpdate() {
    this.updateProperties = true;
  }

  @Override
  public void setErrorLogging(OutputStream errorLogging) {
    if (this.errorLogging != errorLogging) {
      this.errorLogging = errorLogging;
    }
  }

  @Override
  public void setReportLogging(OutputStream reportLogging) {
    if (this.reportLogging != reportLogging) {
      this.reportLogging = reportLogging;
    }
  }

  @Override
  public synchronized VReuterProperty getReuterProperties() {
    if (reuterProperties == null) {
      reuterProperties = resourceManager.load(PROPERTY_FILE_PATH, new VReuterProperty());
    }
    return reuterProperties;
  }

  @Override
  public synchronized void setReuterProperties(VReuterProperty reuterProperties) {
    this.reuterProperties = reuterProperties;
    if (resourceManager != null) {
      resourceManager.persist(reuterProperties, PROPERTY_FILE_PATH);
    }
    try {
      checkLogging();
    } catch (Exception e) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, PROPERTY_FILE_PATH, e);
    }
  }
}

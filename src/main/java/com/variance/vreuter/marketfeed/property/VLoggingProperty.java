/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.vjax.annotations.Comment;

/**
 * Logging properties of the application.
 * @author Administrator
 */
public class VLoggingProperty {

    private boolean enableErrorLogging;
    private boolean enableReportLogging;
    private String errorLog;
    private String reportLog;

    public VLoggingProperty() {
        errorLog = "reuters_tof_error.log";
        reportLog = "reuters_tof_report.log";
        this.enableErrorLogging = true;
        this.enableReportLogging = false;
    }

    public boolean isEnableErrorLogging() {
        return enableErrorLogging;
    }

    public void setEnableErrorLogging(boolean enableErrorLogging) {
        this.enableErrorLogging = enableErrorLogging;
    }

    public boolean isEnableReportLogging() {
        return enableReportLogging;
    }

    public void setEnableReportLogging(boolean enableReportLogging) {
        this.enableReportLogging = enableReportLogging;
    }

    @Comment("Name of the Error Log file.\nThis file is relative to the specified File Directory")
    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    @Comment("Name of the Report Log file.\nThis file is relative to the specified File Directory")
    public String getReportLog() {
        return reportLog;
    }

    public void setReportLog(String reportLog) {
        this.reportLog = reportLog;
    }
}

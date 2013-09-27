/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.service;

/**
 *
 * @author Administrator
 */
public enum VStatusResponse {

    TECHNICAL_ERROR(1, "Negative for Technical Reasons", ""),
    NO_SUCH_NAME(2, "Negative No such Name", ""),
    TEMPLATE_ERROR(4, "Template Error", ""),
    INVALID_MESSAGE(6, "Invalid Message", ""),
    MESSAGE_TYPE_ERROR(7, "Message Type Error", ""),
    TAG_ERROR(8, "Tag Error", ""),
    MISSING_RECORD_NAME(9, "Missing Record Name", ""),
    REQUEST_REJECTED(26, "Request Rejected Try Again", ""),
    HARD_RESET_COMPLETE(96, "Hard Reset Complete", "");

    private VStatusResponse(int statusIndex, String statusDescription, String statusMessage) {
        this.statusIndex = statusIndex;
        this.statusDescription = statusDescription;
        this.statusMessage = statusMessage;
    }
    private int statusIndex;
    private String statusDescription;
    private String statusMessage;

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return "VStatusResponse{" + "Status Index=" + statusIndex + "\tStatus Description=" + statusDescription + "\tStatus Message=" + statusMessage + '}';
    }

    public static VStatusResponse findInstance(int statusIndex) {
        for (VStatusResponse vsr : values()) {
            if (vsr.statusIndex == statusIndex) {
                return vsr;
            }
        }
        return null;
    }
}

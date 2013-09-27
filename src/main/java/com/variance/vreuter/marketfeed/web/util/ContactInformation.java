/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web.util;

/**
 *
 * @author Administrator
 */
public class ContactInformation {

    private String name;
    private String phoneNumber;
    private String emailAddress;
    private String requestInformation;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRequestInformation() {
        return requestInformation;
    }

    public void setRequestInformation(String requestInformation) {
        this.requestInformation = requestInformation;
    }
}

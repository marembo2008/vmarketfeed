/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.deal.managers;

import com.anosym.utilities.FormattedCalendar;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marembo
 */
public class VCalendarConverter {

  private String pattern;

  public VCalendarConverter() {
  }

  public VCalendarConverter(String pattern) {
    this.pattern = pattern;
  }

  public String convert(Calendar dateTime) {
    try {
      String year = dateTime.get(Calendar.YEAR) + "";
      String month = (dateTime.get(Calendar.MONTH) + 1) + "";
      String date = dateTime.get(Calendar.DATE) + "";
      if (year.length() != 4) {
        year = "20" + year;
      }
      if (month.length() != 2) {
        month = "0" + month;
      }
      if (date.length() != 2) {
        date = "0" + date;
      }
      return date + "/" + month + "/" + year;

    } catch (Exception ex) {
      Logger.getLogger(VCalendarConverter.class.getName()).log(Level.SEVERE, null, ex);
    }
    return FormattedCalendar.getISODateString(dateTime);
  }
}

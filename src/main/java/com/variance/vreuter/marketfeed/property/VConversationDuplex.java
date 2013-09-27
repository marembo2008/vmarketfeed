/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.property;

import com.anosym.utilities.Duplex;
import com.anosym.vjax.annotations.Comment;

/**
 *
 * @author Administrator
 */
public class VConversationDuplex extends Duplex<String, String> {

  @Override
  @Comment("The Conversation ")
  public String getFirstValue() {
    return super.getFirstValue();
  }

  @Override
  public String getSecondValue() {
    return super.getSecondValue();
  }

  @Override
  public void setFirstValue(String firstValue) {
    super.setFirstValue(firstValue);
  }

  @Override
  public void setSecondValue(String secondValue) {
    super.setSecondValue(secondValue);
  }
}

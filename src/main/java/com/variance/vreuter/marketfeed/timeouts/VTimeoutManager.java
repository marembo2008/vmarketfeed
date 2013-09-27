/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.timeouts;

import com.anosym.utilities.Duplex;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * implements the logic of timeouts in the system. A timeout request is started by the manager and
 * when it expires, the relevant TimeoutListeners are notified as appropriate.
 *
 * @author Administrator
 */
public class VTimeoutManager {

  private final Map<VTimeoutListener, List<Duplex<VTimer, Object>>> timerData;

  public VTimeoutManager() {
    timerData = new HashMap<VTimeoutListener, List<Duplex<VTimer, Object>>>();
  }

  private class MarketfeedTimer implements VTimer {

    public void start() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stop() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void pause() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resume() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  /**
   * General for starting any timer
   *
   * @param listener
   * @param millisecondDuration
   * @return
   */
  public VTimer startTimer(VTimeoutListener listener, long millisecondDuration) {
    return new MarketfeedTimer();
  }

  /**
   * This provides protection against an invalid response or no acknowledgment after a transmitted
   * message. The no-response timer is started after transmitting each block check character. It is
   * stopped on receipt of an
   * <ACK> or <NAK>. The default no-response timeout is five seconds. A different value can be set
   * by Reuters installation staff to meet users’ requirements. However, in practice five seconds
   * has proved to be adequate and has not been changed
   *
   * @param listener
   * @return
   */
  public VTimer startNoResponseTimeout(VTimeoutListener listener) {
    return null;
  }

  /**
   * This provides protection against a line break in midmessage, and non-recognition of the
   * terminating characters of an information message (i.e. <ETX> and the block check character).
   * The inter-character timer is active while an information message is being received. It is
   * started on receipt of <STX>
   * and restarted after each further character. It is stopped on receipt of <ETX> and the block
   * check character. The default inter-character timeout is one second.
   *
   * @param listener
   * @return
   */
  public VTimer startIntercharacterTimeout(VTimeoutListener listener) {
    return null;
  }

  /**
   * When a request for data is sent by the user computer, the Dealing Server transmits an <ACK> and
   * then an information message containing formatted data. The no-message timer should be started
   * on receipt of the <ACK> and stopped on receiving the <STX> at the start of the data. The
   * timeout should be set at between 15 and 30 seconds. Normal response time will be around one to
   * two seconds.
   *
   * @param listener
   * @return
   */
  public VTimer startNoMessageTimeout(VTimeoutListener listener) {
    return null;
  }
}

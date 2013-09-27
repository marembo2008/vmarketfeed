/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.service;

import com.anosym.utilities.Duplex;
import com.anosym.utilities.MapQueue;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public interface VMarketfeedListener {

    /**
     * Called when a deal has been forwarded to the bank application
     * When conversation text is not separated from the deal information
     * the following could include both the conversation text and the actual deal details
     * @param forwardedDeal the forwarded deal
     * @param remainingDeals the deals remaining in the queue, awaiting to be forwarded
     */
    public void dealForwarded(Duplex<Integer, Duplex<Integer, Map<Integer, String>>> forwardedDeal, MapQueue<Integer, Duplex<Integer, Map<Integer, String>>> remainingDeals);

    /**
     * Called when a conversation has been forwarded to the bank application database
     * The listener will only be notified of conversation forwarding only if:
     * <ol>
     * <li><b>Conversation request has been enabled on the server.</b></li>
     * <li><b>Conversation text has been specified to use a different table.</b></li>
     * </ol>
     * @param forwardedConversation the conversation that has just been forwarded
     * @param remainingConversations the remaining conversations awaiting to be forwarded
     */
    public void conversationForwarded(Duplex<Integer, Map<Integer, String>> forwardedConversation, MapQueue<Integer, Map<Integer, String>> remainingConversations);
}

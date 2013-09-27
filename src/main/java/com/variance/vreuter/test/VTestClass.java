/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.test;

import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.VXMLBindingException;
import com.anosym.vjax.xml.VDocument;
import com.anosym.vjax.xml.VNamespace;
import com.variance.vreuter.marketfeed.deal.VDealColumn;
import com.variance.vreuter.marketfeed.deal.VRecordResponseData;
import com.variance.vreuter.marketfeed.util.VColumnRecord;

/**
 *
 * @author Administrator
 */
public class VTestClass {

    private static int index0 = 0;
    private static int index1 = 1;

    public static String generate() {
        if (index1 == 26) {
            index1 = 0;
            index0++;
        }
        if (index0 == 26) {
            index0 = 0;
        }
        if (index0 == 0 && index1 == 0) {
            index1++;
        }
        byte[] bbs = {(byte) (65 + (index0 % 26)), (byte) (65 + (index1 % 26))};
        index1++;
        String s = new String(bbs);
        return s;
    }

    public static void main(String[] args) throws VXMLBindingException {
        VDealColumn col = new VDealColumn(new VColumnRecord("CONTREFNNO", VRecordResponseData.TICKET_ID), "324343");
        System.setProperty(VNamespace.DEFUALT_NAMESPACE_PROPERTY_BINDING, "false");
        VMarshaller<VDealColumn> m = new VMarshaller<VDealColumn>();
        VDocument doc = m.marshallDocument(col);
        System.out.println(doc.toXmlString());
    }
}

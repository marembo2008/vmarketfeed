/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed;

import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.VXMLBindingException;
import com.anosym.vjax.xml.VDocument;
import com.anosym.vjax.xml.VElement;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The resources here are persistent only for the duration for the system running
 *
 * @author Administrator
 */
public class XmlEjbResourceManager<T> implements ResourceManager<T> {

  @Override
  public T load(String path, T defaultProp) {
    T prop = defaultProp;
    try {
      byte[] bs = (byte[]) new InitialContext().lookup(path);
      if (bs == null) {
        System.err.println("Context path......................" + path);
        VMarshaller<T> vm = new VMarshaller<T>();
        VDocument doc = new VDocument();
        VElement e = vm.marshall(defaultProp);
        doc.setRootElement(e);
        new InitialContext().bind(path, doc.toXmlString().getBytes());
        return defaultProp;
      } else {
        ByteArrayInputStream inn = new ByteArrayInputStream(bs);
        VMarshaller<T> vm = new VMarshaller<T>();
        VDocument doc = VDocument.parseDocument(inn);
        prop = vm.unmarshall(doc);
      }
    } catch (VXMLBindingException ex) {
      Logger.getLogger(VReuter.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NamingException ex) {
      try {
        VMarshaller<T> vm = new VMarshaller<T>();
        VDocument doc = new VDocument();
        VElement e = vm.marshall(defaultProp);
        doc.setRootElement(e);
        new InitialContext().bind(path, doc.toXmlString().getBytes());
        return defaultProp;
      } catch (NamingException ex1) {
        Logger.getLogger(XmlEjbResourceManager.class.getName()).log(Level.SEVERE, null, ex1);
      } catch (VXMLBindingException ex1) {
        Logger.getLogger(XmlEjbResourceManager.class.getName()).log(Level.SEVERE, null, ex1);
      }
    }
    return prop;
  }

  @Override
  public void persist(T resource, String path) {
    try {
      VMarshaller<T> vm = new VMarshaller<T>();
      VDocument doc = new VDocument();
      VElement e = vm.marshall(resource);
      doc.setRootElement(e);
      new InitialContext().rebind(path, doc.toXmlString().getBytes());
    } catch (VXMLBindingException ex) {
      Logger.getLogger(XmlEjbResourceManager.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NamingException ex) {
      Logger.getLogger(XmlEjbResourceManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}

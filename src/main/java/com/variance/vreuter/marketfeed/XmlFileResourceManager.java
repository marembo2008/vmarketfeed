/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed;

import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.VXMLBindingException;
import com.anosym.vjax.xml.VDocument;
import com.anosym.vjax.xml.VElement;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class XmlFileResourceManager<T> implements ResourceManager<T> {

    private static final String INSTALL_DIR = System.getProperty("user.home");

    @Override
    public T load(String path, T defaultProp) {
        T prop = defaultProp;
        try {
            File file = new File(INSTALL_DIR, path);
            System.out.println("INSTALL_DIR: FOR VREUTER PROPERTY:  " + INSTALL_DIR);
            if (!file.exists()) {
                VMarshaller<T> vm = new VMarshaller<T>();
                VDocument doc = new VDocument(file.getAbsolutePath());
                VElement e = vm.marshall(defaultProp);
                doc.setRootElement(e);
                doc.writeDocument();
                return defaultProp;
            } else {
                FileInputStream inn = new FileInputStream(file);
                VMarshaller<T> vm = new VMarshaller<T>();
                VDocument doc = VDocument.parseDocument(inn);
                prop = vm.unmarshall(doc);
            }
        } catch (VXMLBindingException ex) {
            Logger.getLogger(XmlFileResourceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(XmlFileResourceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prop;
    }

    @Override
    public void persist(T resource, String path) {
        try {
            File file = new File(INSTALL_DIR, path);
            VMarshaller<T> vm = new VMarshaller<T>();
            VElement e = vm.marshall(resource);
            VDocument doc = new VDocument(file.getAbsolutePath());
            doc.setRootElement(e);
            doc.writeDocument();
        } catch (VXMLBindingException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, path, ex);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, path, ex);
        }
    }
}

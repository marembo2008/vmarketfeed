/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.web;

import com.anosym.jflemax.validation.annotation.LoginStatus;
import com.anosym.jflemax.validation.annotation.OnRequest;
import com.anosym.jflemax.validation.annotation.OnRequests;
import com.anosym.jflemax.validation.annotation.Principal;
import com.anosym.jflemax.validation.annotation.ViewExpiredPages;
import com.anosym.utilities.Utility;
import com.anosym.vjax.VMarshaller;
import com.anosym.vjax.xml.VDocument;
import com.anosym.vjax.xml.VElement;
import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.web.util.ContactInformation;
import com.variance.vreuter.marketfeed.web.util.Credential;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Administrator
 */
@Named("loginManager")
@ApplicationScoped
@OnRequests(onRequests = {
  @OnRequest(toPages = "*",
          excludedPages = {"/loginpage.xhtml"},
          controller = "loginManager",
          logInStatus = LoginStatus.WHEN_LOGGED_OUT,
          priority = 200,
          redirect = true,
          redirectPage = "/loginpage.xhtml",
          onRequestMethod = "checkLoggedIn")
})
@ViewExpiredPages(pages = {"/loginpage.xhtml"})
public class VLoginManager implements VLoginManagerRemote {

  private static final String SUCCESS_LOGIN = "TicketDetails?faces-redirect=true";
  private static final File CREDENTIAL_FILE = new File(System.getProperty("user.home", "/home"), "user_credentials.cred");
  private static final File CREDENTIAL_BACKUP_FILE = new File(Utility.getCurrentWorkingDirectory(VLoginManager.class), "user_credentials.cred");
  private Credential credential;
  private Credential newUser;
  private ContactInformation contactInformation;
  private Map<String, Credential> loggedOnUsers = new HashMap<String, Credential>();
  @EJB
  private VMarketfeedManagerRemote marketfeedManager;
  @EJB
  private VReuterRemote vreuter;

  public boolean checkLoggedIn() {
    return loggedOnUsers.containsKey(currentSession());
  }

  @Override
  public String toLogin() {
    try {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
      loggedOnUsers.remove(httpSession.getId());
      marketfeedManager.endSession();
      httpSession.invalidate();
    } catch (Exception ee) {
    }
    return "/loginpage.xhtml?faces-redirect=true";
  }

  @Override
  public String sendQuery() {
    if (contactInformation.getName() == null || contactInformation.getName().isEmpty()
            || contactInformation.getEmailAddress() == null || contactInformation.getEmailAddress().isEmpty()
            || contactInformation.getPhoneNumber() == null || contactInformation.getPhoneNumber().isEmpty()
            || contactInformation.getRequestInformation() == null || contactInformation.getRequestInformation().isEmpty()) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("All Fields must be specified"));
    }
    return null;
  }

  @Override
  public ContactInformation getContactInformation() {
    if (contactInformation == null) {
      contactInformation = new ContactInformation();
    }
    return contactInformation;
  }

  @Override
  public void setContactInformation(ContactInformation contactInformation) {
    this.contactInformation = contactInformation;
  }

  @Override
  public Credential getNewUser() {
    if (newUser == null) {
      newUser = new Credential();
    }
    return newUser;
  }

  @Override
  public void setNewUser(Credential newUser) {
    this.newUser = newUser;
  }

  @Override
  public boolean isAdministrator() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
    if (httpSession != null) {
      Credential c = loggedOnUsers.get(httpSession.getId());
      if (c != null) {
        return c.isAdministrator();
      }
    }
    return false;
  }

  @Principal
  public Credential getCurrentCredential() {
    return loggedOnUsers.get(currentSession());
  }

  @Override
  public Credential getCredential() {
    return credential;
  }

  @Override
  public void setCredential(Credential credential) {
    this.credential = credential;
  }

  @Override
  public String login() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
    if (httpSession == null) {
      return "";
    }
    try {
      login(credential);
      loggedOnUsers.put(httpSession.getId(), credential);
      credential = new Credential();
      marketfeedManager.startSession();
      return SUCCESS_LOGIN;
    } catch (Exception e) {
      Logger.getLogger(VLoginManager.class.getName()).log(Level.SEVERE, null, e);
      String message = e.getMessage();
      if (message == null || message.isEmpty()) {
        message = "Invalid User Name or Password";
      }
      FacesMessage msg = new FacesMessage(message, e.getLocalizedMessage());
      FacesContext.getCurrentInstance().addMessage(null, msg);
      toLogin();
    }
    return null;
  }

  public String currentSession() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(true);
    return httpSession.getId();
  }

  @Override
  public String getCurrentSessionUser() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
    if (httpSession != null) {
      Credential c = loggedOnUsers.get(httpSession.getId());
      if (c != null) {
        return c.getUserName();
      }
    }
    return null;
  }

  @Override
  public String doCreateUser() {
    newUser = new Credential();
    return "NewUserDetail?faces-redirect=true";
  }

  @Override
  public String getCurrentYear() {
    Calendar c = Calendar.getInstance();
    String year = c.get(Calendar.YEAR) + "";
    return year;
  }

  private boolean accept(Credential cred) {
    try {
      if (loggedOnUsers.containsValue(cred)) {
        //get the session, if it is this session, simply return true
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        Credential _c = loggedOnUsers.get(httpSession.getId());
        if (cred.equals(_c)) {
          return true;
        }
      }
      VDocument doc = VDocument.parseDocument(new FileInputStream(CREDENTIAL_FILE));
      VMarshaller<List<Credential>> vm = new VMarshaller<List<Credential>>();
      List<Credential> creds = vm.unmarshall(doc);
      int index = creds.indexOf(cred);
      if (index > -1) {
        credential = creds.get(index);
      }
      return index > -1;
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  @Override
  public void login(Credential credentials) {
    if (!accept(credentials)) {
      throw new EJBException("Wrong user name or password");
    }
  }

  @Override
  public boolean isSessionActive() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
    System.out.println("Checking Session: " + ((httpSession != null) ? httpSession.getId() : null));
    return httpSession != null && this.loggedOnUsers.get(httpSession.getId()) != null;
  }

  @PostConstruct
  @Override
  public void onStart() {
    if (!CREDENTIAL_FILE.exists()) {
      try {
        List<Credential> creds = new ArrayList<Credential>();
        creds.add(new Credential("admin", "adminadmin", true));
        creds.add(new Credential("variance", "gwara_thula_marembo", true));
        VDocument doc = new VDocument(CREDENTIAL_FILE);
        VMarshaller<List<Credential>> vm = new VMarshaller<List<Credential>>();
        VElement el = vm.marshall(creds);
        doc.setRootElement(el);
        doc.writeDocument();
      } catch (Exception ex) {
        Logger.getLogger(VLoginManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    credential = new Credential();
  }

  @Override
  public String createUser() {
    List<Credential> creds = null;
    try {
      VDocument doc = VDocument.parseDocument(new FileInputStream(CREDENTIAL_FILE));
      VMarshaller<List<Credential>> vm = new VMarshaller<List<Credential>>();
      creds = vm.unmarshall(doc);
      if (creds.contains(newUser)) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("A user with the specified name already exists"));
        return null;
      }
      creds.add(newUser);
      VElement el = vm.marshall(creds);
      doc.setRootElement(el);
      doc = new VDocument(CREDENTIAL_FILE);
      doc.setRootElement(el);
      doc.writeDocument();
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Successfully Created User"));
    } catch (Exception e) {
      Logger.getLogger(VLoginManager.class.getName()).log(Level.SEVERE, null, e);
      FacesContext ctx = FacesContext.getCurrentInstance();
      FacesMessage msg = new FacesMessage("Failed to Create User: " + e.getClass().getName(), e.getClass().getName());
      ctx.addMessage(null, msg);
    }
    return null;
  }

  @Override
  public void removeUser() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
    if (httpSession != null) {
      loggedOnUsers.remove(httpSession.getId());
    }
  }

  @Override
  public void handleSessionDestroyed(String sessionId) {
    Credential cred = loggedOnUsers.remove(sessionId);
    System.out.print("Closing Session: " + sessionId + " for UserId:" + cred);
  }
}

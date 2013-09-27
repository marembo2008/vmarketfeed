/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed.connection;

import com.variance.vreuter.marketfeed.VReuterRemote;
import com.variance.vreuter.marketfeed.property.VSocketConnectionProperty;
import com.variance.vreuter.marketfeed.service.VDataManager;
import com.variance.vreuter.marketfeed.service.VSeparatorCharacterConstants;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author Administrator
 */
@Singleton
@Startup
@DependsOn("VReuter")
public class VDealingServerSocketConnection implements VDealingServerConnection {

  @EJB
  private VReuterRemote reuter;
  private Socket connectionSocket;
  @EJB
  private VDataManager dataManager;
  private final Queue<byte[]> dataBuffer = new ConcurrentLinkedQueue<byte[]>();
  private volatile boolean active;
  private volatile boolean destroyed;

  @PostConstruct
  public void onStart() {
    try {
      destroyed = false;
      doConnect();
    } catch (Exception e) {
      Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  @PreDestroy
  public void onDestroy() {
    System.err.println("Stopping marketfeed Service.................");
    try {
      destroyed = true;
      synchronized (this) {
        this.notifyAll();
      }
      Thread.currentThread().join(1000);
      if (connectionSocket != null) {
        connectionSocket.close();
      }
    } catch (Exception ex) {
      Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  void doConnect() {
    connect();
    monitorConnection();
  }

  private void initialize() throws IOException {
    byte[] initData = dataManager.getInitializationData();
    OutputStream out = connectionSocket.getOutputStream();
    out.write(initData);
    out.flush();
  }

  private void connect() {

    if (reuter.getReuterProperties().isReuterPropertySet()) {
      System.err.println("Starting Marketfeed Service Connection......");
      try {
        if (dataManager != null) {
          connectionSocket = new Socket();
          VSocketConnectionProperty prop = (VSocketConnectionProperty) reuter.getReuterProperties().getConnectionProperty(); //we shoud be guaranteed of this
          SocketAddress address = new InetSocketAddress(prop.getServerAddress(), prop.getServerPort());
//                    SocketAddress address = new InetSocketAddress("192.168.0.25", prop.getServerPort());
//                    InetAddress iaddress = InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, (byte)0, (byte)25});
//                    address = new InetSocketAddress(iaddress, 5003);
//                    String s = address.toString();
          connectionSocket.connect(address);
          if (connectionSocket.isConnected()) {
            //initialize the stream
            byte[] initData = dataManager.getInitializationData();
            if (initData != null) {
              active = true;
              OutputStream out = connectionSocket.getOutputStream();
              out.write(initData);
              out.flush();
              handleDataInput();
              manageData();
              doRequest();
              synchronized (this) {
                this.notify();
              }
              return;
            } else {
              throw new RuntimeException("Initialization Parameters Required");
            }
          } else {
            throw new RuntimeException("Could not establish socket connection");
          }
        }
        throw new RuntimeException("Data Manager null");
      } catch (Exception ee) {
        Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ee);
        active = false;
        try {
          Thread.currentThread().join(10000);
        } catch (InterruptedException ex) {
        }
        if (connectionSocket != null) {
          try {
            connectionSocket.close();
          } catch (Exception ex) {
          }
        }
        System.err.println(ee.getLocalizedMessage());
      }
    } else {
      System.err.println("Marketfeed Service Connection Could not be established because the Rueters Properties is not yet set.");
    }
  }

  private void handleDataInput() {
    new Thread(new Runnable() {
      public void run() {
        byte[] bb = new byte[0];
        try {
          while (isActive() && !destroyed) {
            InputStream inn = connectionSocket.getInputStream();
            BufferedInputStream bInn = new BufferedInputStream(inn);
            int d = 0;
            while ((d = bInn.read()) != -1) {
              bb = Arrays.copyOf(bb, bb.length + 1);
              bb[bb.length - 1] = (byte) d;
              if (bb.length == 2 && (bb[0] == bb[1] && bb[1] == VSeparatorCharacterConstants.FS)) {
                bb = new byte[]{bb[0]};
              }
              if (d == VSeparatorCharacterConstants.FS && bb.length > 2) { //we know the data has larger lengths
                dataBuffer.offer(bb);
                synchronized (VDealingServerSocketConnection.this) {
                  VDealingServerSocketConnection.this.notifyAll();
                }
                //if the data is not demarcated by two FS characters
                bb = Arrays.copyOf(bb, 1);
                bb[0] = (byte) d;
              }
            }
          }
        } catch (IOException ex) {
          Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
          active = false;
          synchronized (VDealingServerSocketConnection.this) {
            VDealingServerSocketConnection.this.notifyAll();
          }
        }
      }
    }).start();
  }

  private void manageData() {
    new Thread(new Runnable() {
      public void run() {
        while (isActive() && !destroyed) {
          try {
            while ((dataBuffer.isEmpty() || dataManager == null) && active) {
              synchronized (VDealingServerSocketConnection.this) {
                try {
                  VDealingServerSocketConnection.this.wait();
                } catch (InterruptedException ex) {
                  Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
              if (destroyed) {
                return;
              }
            }
            while (!dataBuffer.isEmpty()) {
              synchronized (dataBuffer) {
                byte[] data = dataBuffer.poll();
                if (data != null) {
                  dataManager.handleData(data);
                }
              }
            }
          } catch (Exception ex) {
            Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    }).start();
  }

  private void monitorConnection() {
    new Thread(new Runnable() {
      public void run() {
        System.err.println("Starting Connection Monitoring..............." + destroyed);
        while (!destroyed) {
          try {
            while (isActive()) {
              synchronized (VDealingServerSocketConnection.this) {
                try {
                  VDealingServerSocketConnection.this.wait();
                } catch (InterruptedException ex) {
                  Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
              if (destroyed) {
                return;
              }
            }
            System.err.println("Trying dealing server reconnect");
            connect();
          } catch (Exception ex) {
            Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getLocalizedMessage());
          } finally {
            //sleep for 1 minute before attempting reconnection
            final Object o = new Object();
            synchronized (o) {
              try {
                o.wait(10000);
              } catch (InterruptedException ex) {
                Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
              }
            }
            if (destroyed) {
              return;
            }
          }
        }
      }
    }).start();
  }

  private void doRequest() {
    new Thread(new Runnable() {
      public void run() {
        final Object sync = new Object();
        try {
          while (active && !destroyed) {
            if (dataManager.isErrorInInitialization()) {
              initialize();
              synchronized (sync) {
                try {
                  sync.wait(1000);
                } catch (InterruptedException ex) {
                  Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
            }
            byte[] data = dataManager.getData();
            if (data == null) {
              while (!dataBuffer.isEmpty()) {
                synchronized (sync) {
                  try {
                    //wait for at least 10 minute before next request..
                    sync.wait(10000);
                  } catch (InterruptedException ex) {
                    Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
                  }
                }
              }
              data = dataManager.getInitializationData();
            }
            {
              OutputStream out = connectionSocket.getOutputStream();
              out.write(data);
              //wait for at least 4 seconds
              synchronized (sync) {
                try {
                  sync.wait(500);
                } catch (InterruptedException ex) {
                  Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
              }
            }
          }
        } catch (IOException ex) {
          Logger.getLogger(VDealingServerSocketConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
          active = false;
        }
      }
    }).start();
  }

  public void setDataManager(VDataManager dataManager) {
    this.dataManager = dataManager;
  }

  public void setReuterManager(VReuterRemote reuterManager) {
    this.reuter = reuterManager;
  }

  private boolean isActive() {
    return active && reuter.getReuterProperties().isReuterPropertySet();
  }
}

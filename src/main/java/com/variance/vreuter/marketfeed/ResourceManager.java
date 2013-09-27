/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.variance.vreuter.marketfeed;

/**
 *
 * @author Administrator
 */
public interface ResourceManager<T> {

  /**
   * Loads the recourse from the specified path if applicable
   *
   * @param path
   * @return
   */
  public T load(String path, T defaultProp);

  /**
   * Persists the path to the specified path if applicable
   *
   * @param resource
   * @param path
   */
  public void persist(T resource, String path);
}

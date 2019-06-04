/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.doubango.tinyWRAP;

public class T140Callback {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected T140Callback(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(T140Callback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tinyWRAPJNI.delete_T140Callback(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    tinyWRAPJNI.T140Callback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    tinyWRAPJNI.T140Callback_change_ownership(this, swigCPtr, true);
  }

  public T140Callback() {
    this(tinyWRAPJNI.new_T140Callback(), true);
    tinyWRAPJNI.T140Callback_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public int ondata(T140CallbackData pData) {
    return (getClass() == T140Callback.class) ? tinyWRAPJNI.T140Callback_ondata(swigCPtr, this, T140CallbackData.getCPtr(pData), pData) : tinyWRAPJNI.T140Callback_ondataSwigExplicitT140Callback(swigCPtr, this, T140CallbackData.getCPtr(pData), pData);
  }

}

/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.doubango.tinyWRAP;

public class XcapMessage {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected XcapMessage(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(XcapMessage obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tinyWRAPJNI.delete_XcapMessage(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public byte[] getXcapContent() {
    final int clen = (int)this.getXcapContentLength();
    if(clen>0){
		final java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(clen);
        final int read = (int)this.getXcapContent(buffer, clen);
        final byte[] bytes = new byte[read];
        buffer.get(bytes, 0, read);
        return bytes;
    }
    return null;
  }

  public XcapMessage() {
    this(tinyWRAPJNI.new_XcapMessage(), true);
  }

  public short getCode() {
    return tinyWRAPJNI.XcapMessage_getCode(swigCPtr, this);
  }

  public String getPhrase() {
    return tinyWRAPJNI.XcapMessage_getPhrase(swigCPtr, this);
  }

  public String getXcapHeaderValue(String name, long index) {
    return tinyWRAPJNI.XcapMessage_getXcapHeaderValue__SWIG_0(swigCPtr, this, name, index);
  }

  public String getXcapHeaderValue(String name) {
    return tinyWRAPJNI.XcapMessage_getXcapHeaderValue__SWIG_1(swigCPtr, this, name);
  }

  public String getXcapHeaderParamValue(String name, String param, long index) {
    return tinyWRAPJNI.XcapMessage_getXcapHeaderParamValue__SWIG_0(swigCPtr, this, name, param, index);
  }

  public String getXcapHeaderParamValue(String name, String param) {
    return tinyWRAPJNI.XcapMessage_getXcapHeaderParamValue__SWIG_1(swigCPtr, this, name, param);
  }

  public long getXcapContentLength() {
    return tinyWRAPJNI.XcapMessage_getXcapContentLength(swigCPtr, this);
  }

  public long getXcapContent(java.nio.ByteBuffer output, long maxsize) {
    return tinyWRAPJNI.XcapMessage_getXcapContent(swigCPtr, this, output, maxsize);
  }

}

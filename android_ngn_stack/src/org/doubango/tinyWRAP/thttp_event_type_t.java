/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.doubango.tinyWRAP;

public enum thttp_event_type_t {
  thttp_event_dialog_started,
  thttp_event_message,
  thttp_event_auth_failed,
  thttp_event_closed,
  thttp_event_transport_error,
  thttp_event_dialog_terminated;

  public final int swigValue() {
    return swigValue;
  }

  public static thttp_event_type_t swigToEnum(int swigValue) {
    thttp_event_type_t[] swigValues = thttp_event_type_t.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (thttp_event_type_t swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + thttp_event_type_t.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private thttp_event_type_t() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private thttp_event_type_t(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private thttp_event_type_t(thttp_event_type_t swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}


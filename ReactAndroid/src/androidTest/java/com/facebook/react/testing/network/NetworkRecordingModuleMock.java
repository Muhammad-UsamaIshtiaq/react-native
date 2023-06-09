/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.testing.network;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

/**
 * Mock Networking module that records last request received by {@link #sendRequest} method and
 * returns response code and body that should be set with {@link #setResponse}
 */
@ReactModule(name = "Networking", canOverrideExistingModule = true)
public class NetworkRecordingModuleMock extends ReactContextBaseJavaModule {

  public int mRequestCount = 0;
  public @Nullable String mRequestMethod;
  public @Nullable String mRequestURL;
  public @Nullable ReadableArray mRequestHeaders;
  public @Nullable ReadableMap mRequestData;
  public int mLastRequestId;
  public boolean mAbortedRequest;

  private boolean mCompleteRequest;
  private ReactApplicationContext mContext;

  public NetworkRecordingModuleMock(ReactApplicationContext reactContext) {
    this(reactContext, true);
  }

  public NetworkRecordingModuleMock(ReactApplicationContext reactContext, boolean completeRequest) {
    super(reactContext);
    mCompleteRequest = completeRequest;
    mContext = reactContext;
  }

  public interface RequestListener {
    public void onRequest(String method, String url, ReadableArray header, ReadableMap data);
  }

  private int mResponseCode;
  private @Nullable String mResponseBody;
  private @Nullable RequestListener mRequestListener;

  public void setResponse(int code, String body) {
    mResponseCode = code;
    mResponseBody = body;
  }

  public void setRequestListener(RequestListener requestListener) {
    mRequestListener = requestListener;
  }

  @Override
  public final String getName() {
    return "Networking";
  }

  private void fireReactCallback(
      Callback callback, int status, @Nullable String headers, @Nullable String body) {
    callback.invoke(status, headers, body);
  }

  @ReactMethod
  public final void sendRequest(
      String method,
      String url,
      int requestId,
      ReadableArray headers,
      ReadableMap data,
      final String responseType,
      boolean incrementalUpdates,
      int timeout,
      boolean withCredentials) {
    mLastRequestId = requestId;
    mRequestCount++;
    mRequestMethod = method;
    mRequestURL = url;
    mRequestHeaders = headers;
    mRequestData = data;
    if (mRequestListener != null) {
      mRequestListener.onRequest(method, url, headers, data);
    }
    if (mCompleteRequest) {
      onResponseReceived(requestId, mResponseCode, null);
      onDataReceived(requestId, mResponseBody);
      onRequestComplete(requestId, null);
    }
  }

  @ReactMethod
  public void abortRequest(int requestId) {
    mLastRequestId = requestId;
    mAbortedRequest = true;
  }

  @Override
  public boolean canOverrideExistingModule() {
    return true;
  }

  public void setCompleteRequest(boolean completeRequest) {
    mCompleteRequest = completeRequest;
  }

  private void onDataReceived(int requestId, String data) {
    WritableArray args = Arguments.createArray();
    args.pushInt(requestId);
    args.pushString(data);

    mContext.emitDeviceEvent("didReceiveNetworkData", args);
  }

  private void onRequestComplete(int requestId, @Nullable String error) {
    WritableArray args = Arguments.createArray();
    args.pushInt(requestId);
    args.pushString(error);

    mContext.emitDeviceEvent("didCompleteNetworkResponse", args);
  }

  private void onResponseReceived(int requestId, int code, WritableMap headers) {
    WritableArray args = Arguments.createArray();
    args.pushInt(requestId);
    args.pushInt(code);
    args.pushMap(headers);

    mContext.emitDeviceEvent("didReceiveNetworkResponse", args);
  }
}

package com.dergoogler.mmrl.platform.stub;

interface IModuleOpsCallback {
    void onSuccess(String id);
    void onFailure(String id, String msg);
}
package com.dergoogler.mmrl.platform.stub;

import com.dergoogler.mmrl.platform.content.LocalModule;

interface IShellCallback {
    void onStdout(String msg);
    void onStderr(String msg);
    void onSuccess(in @nullable LocalModule module);
    void onFailure(in @nullable LocalModule module);
}
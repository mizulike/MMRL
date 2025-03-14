package dev.dergoogler.mmrl.compat.stub;

import dev.dergoogler.mmrl.compat.content.LocalModule;

interface IShellCallback {
    void onStdout(String msg);
    void onStderr(String msg);
    void onSuccess(in @nullable LocalModule module);
    void onFailure(in @nullable LocalModule module);
}
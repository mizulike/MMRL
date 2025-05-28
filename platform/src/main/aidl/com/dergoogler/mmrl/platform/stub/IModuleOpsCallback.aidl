package com.dergoogler.mmrl.platform.stub;

import com.dergoogler.mmrl.platform.model.ModId;

interface IModuleOpsCallback {
    void onSuccess(in ModId id);
    void onFailure(in ModId id, String msg);
}
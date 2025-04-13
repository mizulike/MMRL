package com.dergoogler.mmrl.platform.stub;

import com.dergoogler.mmrl.platform.stub.IFileManager;
import com.dergoogler.mmrl.platform.stub.IModuleManager;

interface IServiceManager {
    int getUid() = 0;
    int getPid() = 1;
    String getSELinuxContext() = 2;
    String currentPlatform() = 3;
    IModuleManager getModuleManager() = 4;
    IFileManager getFileManager() = 5;

    void destroy() = 16777114; // Only for Shizuku
}
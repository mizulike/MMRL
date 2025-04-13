package com.dergoogler.mmrl.platform.stub;

import com.dergoogler.mmrl.platform.content.LocalModule;
import com.dergoogler.mmrl.platform.content.ModuleCompatibility;
import com.dergoogler.mmrl.platform.content.BulkModule;
import com.dergoogler.mmrl.platform.content.NullableBoolean;
import com.dergoogler.mmrl.platform.stub.IShellCallback;
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback;
import com.dergoogler.mmrl.platform.stub.IShell;

interface IModuleManager {
    String getManagerName();
    String getVersion();
    int getVersionCode();
    List<LocalModule> getModules();
    ModuleCompatibility getModuleCompatibility();
    String getSeLinuxContext();
    LocalModule getModuleById(String id);
    LocalModule getModuleInfo(String zipPath);
    IShell getShell(in List<String> command, in LocalModule module, IShellCallback callback);
    oneway void reboot(String reason);
    oneway void enable(String id, boolean useShell, IModuleOpsCallback callback);
    oneway void disable(String id, boolean useShell, IModuleOpsCallback callback);
    oneway void remove(String id, boolean useShell, IModuleOpsCallback callback);
    IShell install(String path, in List<BulkModule> bulkModule, IShellCallback callback);
    IShell action(String modId, boolean legacy, IShellCallback callback);

    // General
    int getSuperUserCount();

    // KernelSU (Next) related
    NullableBoolean isLkmMode();
    boolean isSafeMode();
    boolean setSuEnabled(boolean enabled);
    boolean isSuEnabled();
    boolean uidShouldUmount(int uid);
}
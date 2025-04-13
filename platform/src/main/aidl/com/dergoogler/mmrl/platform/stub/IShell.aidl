package com.dergoogler.mmrl.platform.stub;

interface IShell {
    boolean isAlive();
    void exec();
    void close();
}
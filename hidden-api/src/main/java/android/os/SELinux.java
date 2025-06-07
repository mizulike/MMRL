package android.os;

public class SELinux {
    public static native boolean isSELinuxEnabled();
    public static native String getContext();
    public static native boolean isSELinuxEnforced();
}
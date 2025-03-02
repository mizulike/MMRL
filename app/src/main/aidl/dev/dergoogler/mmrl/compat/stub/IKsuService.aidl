package dev.dergoogler.mmrl.compat.stub;

import android.content.pm.PackageInfo;
import rikka.parcelablelist.ParcelableListSlice;

interface IKsuService {
    ParcelableListSlice<PackageInfo> getPackages(int flags);
}
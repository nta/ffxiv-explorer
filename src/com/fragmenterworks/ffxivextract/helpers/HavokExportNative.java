package com.fragmenterworks.ffxivextract.helpers;

import java.nio.ByteBuffer;

public class HavokExportNative {
    static public native String returnExportString(ByteBuffer data);

    public static void initExportNative()
    {
        try
        {
            System.loadLibrary("havokExport");
        }
        catch (UnsatisfiedLinkError e)
        {
            e.printStackTrace();
        }
    }
}

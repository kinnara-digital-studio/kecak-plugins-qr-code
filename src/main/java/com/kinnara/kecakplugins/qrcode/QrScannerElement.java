package com.kinnara.kecakplugins.qrcode;

import org.joget.apps.form.lib.TextField;

public class QrScannerElement extends TextField {
    @Override
    public String getFormBuilderCategory() {
        return "Kecak";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "QR Scanner";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}

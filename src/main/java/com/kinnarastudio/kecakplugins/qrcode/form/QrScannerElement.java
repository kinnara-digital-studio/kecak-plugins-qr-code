package com.kinnarastudio.kecakplugins.qrcode.form;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.TextField;
import org.joget.plugin.base.PluginManager;

import java.util.ResourceBundle;

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
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
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

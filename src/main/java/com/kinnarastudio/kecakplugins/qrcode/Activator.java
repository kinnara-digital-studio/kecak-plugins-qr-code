package com.kinnarastudio.kecakplugins.qrcode;

import java.util.ArrayList;
import java.util.Collection;

import com.kinnarastudio.kecakplugins.qrcode.form.JwtQrCodeElement;
import com.kinnarastudio.kecakplugins.qrcode.form.QrCodeElement;
import com.kinnarastudio.kecakplugins.qrcode.form.QrScannerElement;
import com.kinnarastudio.kecakplugins.qrcode.userview.AuthTokenQrCodeMenu;
import com.kinnarastudio.kecakplugins.qrcode.userview.QrCodeMenu;
import com.kinnarastudio.kecakplugins.qrcode.webservice.QrCodeSignature;
import com.kinnarastudio.kecakplugins.qrcode.webservice.QrCodeWebService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(AuthTokenQrCodeMenu.class.getName(), new AuthTokenQrCodeMenu(), null));
        registrationList.add(context.registerService(QrCodeMenu.class.getName(), new QrCodeMenu(), null));
        registrationList.add(context.registerService(QrCodeSignature.class.getName(), new QrCodeSignature(), null));
        registrationList.add(context.registerService(QrCodeWebService.class.getName(), new QrCodeWebService(), null));
        registrationList.add(context.registerService(QrCodeElement.class.getName(), new QrCodeElement(), null));
        registrationList.add(context.registerService(JwtQrCodeElement.class.getName(), new JwtQrCodeElement(), null));
        registrationList.add(context.registerService(QrScannerElement.class.getName(), new QrScannerElement(), null));
//        registrationList.add(context.registerService(QrCodeSignatureVerificationHashVariable.class.getName(), new QrCodeSignatureVerificationHashVariable(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}
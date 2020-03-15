package com.kinnara.kecakplugins.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class QrCodeMenu extends UserviewMenu {
    @Override
    public String getCategory() {
        return "Kecak";
    }

    @Override
    public String getIcon() {
        return "/plugin/org.joget.apps.userview.lib.HtmlPage/images/grid_icon.gif";
    }

    @Override
    public String getRenderPage() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        String content = AppUtil.processHashVariable(getPropertyString("content"), null, null, null);

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, Integer.parseInt(getPropertyString("width")), Integer.parseInt(getPropertyString("height")));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] encoded = Base64.getEncoder().encode(outputStream.toByteArray());
            String html = "<img src='data:image/png;base64," + new String(encoded) + "'>";
            return html;
        } catch (WriterException | IOException | NumberFormatException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        }

        return null;
    }

    @Override
    public boolean isHomePageSupported() {
        return false;
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public String getName() {
        return "QR Code";
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
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/QrCodeMenu.json", null, false, "/messages/QrCode");
    }
}

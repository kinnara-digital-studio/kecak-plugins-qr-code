package com.kinnara.kecakplugins.qrcode;

import com.google.zxing.WriterException;
import com.kinnara.kecakplugins.qrcode.util.QrGenerator;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.AuthTokenService;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * @author aristo
 *
 * @see <a href="https://gitlab.com/kinnarastudio/kecak-plugins-qr-code/-/wikis/Auth-Token-QR">Auth Token QR</a>
 */
public class AuthTokenQrCodeMenu extends UserviewMenu implements QrGenerator {
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
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        AuthTokenService authTokenService = (AuthTokenService) applicationContext.getBean("authTokenService");

        String hostUrl = AppUtil.processHashVariable(getPropertyString("hostUrl"), null, null, null);
        String username = getPropertyCurrentUser() ? WorkflowUtil.getCurrentUsername() : AppUtil.processHashVariable(getPropertyString("username"), null, null, null);

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JSONObject jsonContent = new JSONObject();
            jsonContent.put("host", hostUrl);
            jsonContent.put("token", authTokenService.generateToken(username));

            int width = Integer.parseInt(getPropertyString("width"));
            int height = Integer.parseInt(getPropertyString("height"));
            writeQrCodeToStream(jsonContent.toString(), width, height, outputStream);

            byte[] encoded = Base64.getEncoder().encode(outputStream.toByteArray());

            LogUtil.info(getClassName(), "Generating authentication QR for [" + username + "]");

            String html = "<img src='data:image/png;base64," + new String(encoded) + "'>";
            return html;
        } catch (WriterException | IOException | NumberFormatException | JSONException e) {
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
        return "Auth Token QR";
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
        return AppUtil.readPluginResource(getClassName(), "/properties/AuthTokenQrCodeMenu.json", null, false, "/messages/QrCode");
    }


    /**
     * Get property "currentUser"
     *
     * @return
     */
    private boolean getPropertyCurrentUser() {
        return "true".equalsIgnoreCase(getPropertyString("currentUser"));
    }
}

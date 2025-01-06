package com.kinnarastudio.kecakplugins.qrcode.form;

import com.google.zxing.WriterException;
import com.kinnarastudio.kecakplugins.qrcode.exception.RestApiException;
import com.kinnarastudio.kecakplugins.qrcode.util.QrGenerator;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;
import org.kecak.apps.app.service.AuthTokenService;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author aristo
 * <p>
 * Not yet implemented
 */
public class JwtQrCodeElement extends Element implements FormBuilderPaletteElement, PluginWebSupport, QrGenerator {
    public final static String LABEL = "JWT QR Code";

    public final static String JWR_PROP_CONTENT_TYPE = "contentType";
    public final static String JWR_PROP_CONTENT = "content";

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final AuthTokenService authTokenService = (AuthTokenService) applicationContext.getBean("authTokenService");

        String template = "JwtQrCodeElement.ftl";

        dataModel.put("className", getClassName());

        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        dataModel.put("appId", appDefinition.getAppId());
        dataModel.put("appVersion", appDefinition.getVersion());

        dataModel.put("primaryKey", formData.getPrimaryKeyValue());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final int width = getWidth();
            final int height = getHeight();
            final String appId = appDefinition.getAppId();
            final long version = appDefinition.getVersion();

            final String username = WorkflowUtil.getCurrentUsername();
            final int timeOutInMinutes = getTimeout();
            final String token = authTokenService.generateToken(username, new HashMap<>() {{
                final String contentType = getContentType();
                put(JWR_PROP_CONTENT_TYPE, contentType);

                final String content = getContent();
                put(JWR_PROP_CONTENT, content);

            }}, timeOutInMinutes);

            final String content = String.format("%s/web/json/app/%s/%d/plugin/%s/service?token=%s", getWebContext(), appId, version, getClassName(), token);
            writeQrCodeToStream(content, width, height, outputStream);

            dataModel.put("link", content);

            byte[] encoded = Base64.getEncoder().encode(outputStream.toByteArray());
            dataModel.put("base64EncodedImage", new String(encoded));

        } catch (IOException | WriterException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        }

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getName() {
        return LABEL;
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
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/JwtQrCodeElement.json", null, true, "/messages/QrCode");
    }

    @Override
    public String getFormBuilderCategory() {
        return "Kecak";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getFormBuilderIcon() {
        return null;
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<img src='${request.contextPath}/plugin/${className}/images/qr-logo.png' width='320' height='320/>";
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            final String method = request.getMethod();
            if (!"GET".equalsIgnoreCase(method)) {
                throw new RestApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method [" + method + "] is not supported");
            }

            final ApplicationContext applicationContext = AppUtil.getApplicationContext();
            final AuthTokenService authTokenService = (AuthTokenService) applicationContext.getBean("authTokenService");

            try {
                final String token = getParameter(request, "token");
                final String contentType = String.valueOf(authTokenService.getClaimDataFromToken(token, "contentType"));
                final String content = String.valueOf(authTokenService.getClaimDataFromToken(token, "content"));

                LogUtil.info(getClassName(), "contentType [" + contentType + "] content [" + content + "]");

                if (Pattern.matches("https?://.+", content)) {
                    LogUtil.info(getClassName(), "Redirect to [" + content + "]");
                    response.sendRedirect(content);
                } else {
                    response.setContentType(contentType);
                    response.getWriter().write(content);
                }
            } catch (RuntimeException e) {
                throw new RestApiException(e);
            }
        } catch (RestApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    protected Optional<String> optParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return Optional.of(parameterName)
                .filter(s -> !s.isEmpty())
                .map(request::getParameter);
    }

    protected String getParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return optParameter(request, parameterName)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + parameterName + " is not supplied]"));
    }

    protected String getWebContext() {
        return getPropertyString("webContext");
    }

    protected String getContent() {
        return getPropertyString("content");
    }

    protected String getContentType() {
        return getPropertyString("contentType");
    }

    protected int getWidth() {
        return Integer.parseInt(getPropertyString("width"));
    }

    protected int getHeight() {
        return Integer.parseInt(getPropertyString("height"));
    }

    protected int getTimeout() {
        try {
            return Integer.parseInt(getPropertyString("timeout"));
        }catch (NumberFormatException e) {
            return 5;
        }
    }
}

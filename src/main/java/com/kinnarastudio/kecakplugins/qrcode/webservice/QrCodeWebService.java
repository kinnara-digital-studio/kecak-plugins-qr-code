package com.kinnarastudio.kecakplugins.qrcode.webservice;

import com.google.zxing.WriterException;
import com.kinnarastudio.kecakplugins.qrcode.exception.RestApiException;
import com.kinnarastudio.kecakplugins.qrcode.util.QrGenerator;
import com.kinnarastudio.commons.Try;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.property.service.PropertyUtil;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

/**
 * @author aristo
 *
 * QR Code Webservice
 *
 * @see <a href="https://gitlab.com/kinnarastudio/kecak-plugins-qr-code/-/wikis/QR-Code-Webservice">QR Code Webservice</>
 */
public class QrCodeWebService extends DefaultApplicationPlugin implements PluginWebSupport, QrGenerator {
    @Override
    public String getName() {
        return getLabel() + getVersion();
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
    public Object execute(Map map) {
        return null;
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
            if(appDefinition == null) {
                throw new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Application definition is not defined");
            }

            try {
                httpServletResponse.setContentType("image/png");
                OutputStream outputStream = httpServletResponse.getOutputStream();

                String content = AppUtil.processHashVariable(getContent(), null, null, null);
                int width = getOptionalParameter(httpServletRequest, "width").map(Integer::parseInt).orElse(getWidth());
                int height = getOptionalParameter(httpServletRequest, "height").map(Integer::parseInt).orElse(getHeight());;
                writeQrCodeToStream(content, width, height, outputStream);
            } catch (WriterException e) {
                throw new RestApiException(e);
            }
        } catch (RestApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            httpServletResponse.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public String getLabel() {
        return "QR Code Webservice";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/QrCodeWebService.json", null, false, "/messages/QrCode");
    }


    protected String getRequiredParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter ["+parameterName+" is not supplied]"));
    }

    private String getOptionalParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        return getOptionalParameter(request, parameterName)
                .orElse(defaultValue);
    }

    private Optional<String> getOptionalParameter(HttpServletRequest request, String parameterName) {
        return Optional.of(parameterName)
                .map(request::getParameter);
    }

    @Nonnull
    protected Map<String, Object> getConfiguration() throws RestApiException {
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        return Optional.ofNullable(appDefinition)
                .map(AppDefinition::getPluginDefaultPropertiesList)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(p -> p.getId().equals(getClassName()))
                .findFirst()
                .map(PluginDefaultProperties::getPluginProperties)
                .map(PropertyUtil::getPropertiesValueFromJson)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_FORBIDDEN, "Missing application configuration"));
    }

    protected String getContent() throws RestApiException {
        Map<String, Object> configuration = getConfiguration();
        return Optional.of("content")
                .map(configuration::get)
                .map(String::valueOf)
                .orElseThrow(() -> new RestApiException("Configuration [content] not found"));
    }

    protected int getWidth() throws RestApiException {
        Map<String, Object> configuration = getConfiguration();
        return Optional.of("width")
                .map(configuration::get)
                .map(String::valueOf)
                .map(Try.onFunction(Integer::parseInt))
                .orElseThrow(() -> new RestApiException("Configuration [width] not found"));
    }

    protected int getHeight() throws RestApiException {
        Map<String, Object> configuration = getConfiguration();
        return Optional.of("height")
                .map(configuration::get)
                .map(String::valueOf)
                .map(Try.onFunction(Integer::parseInt))
                .orElseThrow(() -> new RestApiException("Configuration [height] not found"));
    }
}

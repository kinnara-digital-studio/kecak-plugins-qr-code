package com.kinnara.kecakplugins.qrcode;

import com.google.zxing.WriterException;
import com.kinnara.kecakplugins.qrcode.exception.RestApiException;
import com.kinnara.kecakplugins.qrcode.util.QrGenerator;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class QrCodeMenu extends UserviewMenu implements PluginWebSupport, QrGenerator {
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
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        Userview userview = getUserview();
        String html = "<img src='/web/json/app/" + appDefinition.getAppId() + "/" + appDefinition.getVersion() + "/plugin/" + getClassName() + "/service?userview=" + userview.getPropertyString("id") + "&menu=" + getPropertyString("customId") + "'";
        return html;
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
        return getLabel() + getVersion();
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
        return "QR Code";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/QrCodeMenu.json", null, false, "/messages/QrCode");
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
            String userviewId = getRequiredParameter(httpServletRequest, "userview");
            String menuId = getRequiredParameter(httpServletRequest, "menu");
            Userview userview = generateUserview(httpServletRequest, appDefinition, userviewId, menuId);
            UserviewMenu menu = getUserviewMenu(userview, menuId);
            try {
                httpServletResponse.setContentType("image/png");
                OutputStream outputStream = httpServletResponse.getOutputStream();
                writeQrCodeMenuToUserviewMenu(menu, outputStream);
            } catch (WriterException e) {
                throw new RestApiException(e);
            }
        } catch (RestApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            httpServletResponse.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    private String getRequiredParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter ["+parameterName+" is not supplied]"));
    }

    private String getOptionalParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .orElse(defaultValue);
    }

    private Userview generateUserview(HttpServletRequest request, AppDefinition appDefinition, String userviewId, String menuId) throws RestApiException {
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) applicationContext.getBean("userviewDefinitionDao");
        UserviewService userviewService = (UserviewService) applicationContext.getBean("userviewService");

        return Optional.ofNullable(userviewDefinitionDao.loadById(userviewId, appDefinition))
                .map(UserviewDefinition::getJson)
                .map(s -> userviewService.createUserview(s, menuId, false, AppUtil.getRequestContextPath(), request.getParameterMap(), null, false))
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Error generating userview [" + userviewId + "] menu ["+ menuId + "]"));
    }

    private UserviewMenu getUserviewMenu(@Nonnull  Userview userview, @Nonnull String menuId) throws RestApiException {
        return Optional.of(userview)
                .map(Userview::getCategories)
                .map(Collection::stream)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Userview category not found"))
                .map(UserviewCategory::getMenus)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(menu -> Optional.of("customId")
                        .map(menu::getProperty)
                        .map(String::valueOf)
                        .map(menuId::equals)
                        .orElse(false))
                .findFirst()
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Userview menu not found"));
    }
}

package com.kinnara.kecakplugins.qrcode;

import com.google.zxing.WriterException;
import com.kinnara.kecakplugins.qrcode.exception.RestApiException;
import com.kinnara.kecakplugins.qrcode.util.QrGenerator;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * @author aristo
 *
 * Not yet implemented
 */
public class QrCodeElement extends Element implements FormBuilderPaletteElement, PluginWebSupport, QrGenerator {
    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "QrCodeElement.ftl";

        dataModel.put("className", getClassName());

        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        dataModel.put("appId",appDefinition.getAppId());
        dataModel.put("appVersion",appDefinition.getVersion());

        Form form = FormUtil.findRootForm(this);
        if(form != null) {
            dataModel.put("formDefId", form.getPropertyString("id"));
        }

        dataModel.put("primaryKey", formData.getPrimaryKeyValue());

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int height = Integer.parseInt(getPropertyString("height"));
            int width = Integer.parseInt(getPropertyString("width"));
            writeQrCodeToStream(AppUtil.processHashVariable(getPropertyString("content"), null, null, null), width, height, outputStream);
            byte[] encoded = Base64.getEncoder().encode(outputStream.toByteArray());
            String src = "'data:image/png;base64," + new String(encoded);
            dataModel.put("src", src);
        } catch (WriterException | IOException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        }



        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getName() {
        return "QR Code Element";
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
        return AppUtil.readPluginResource(getClassName(), "/properties/QrCodeElement.json", null, true, "/messages/QrCode").replaceAll("\"", "'");
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
        return "<img src='${request.contextPath}/plugin/${className}/images/pdf-logo.png' width='320' height='320/>";
    }

    @Override
    public void webService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
            String formDefId = getRequiredParameter(httpServletRequest, "form");
            String field = getRequiredParameter(httpServletRequest, "field");
            Form form = generateForm(appDefinition, formDefId);
            Element element = FormUtil.findElement(field, form, new FormData());
            try {
                httpServletResponse.setContentType("image/png");
                OutputStream outputStream = httpServletResponse.getOutputStream();
                writeQrCodeToFormElement(element, outputStream);
            } catch (WriterException e) {
                throw new RestApiException(e);
            }
        } catch (RestApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            httpServletResponse.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    protected String getRequiredParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter ["+parameterName+" is not supplied]"));
    }

    @Nullable
    protected Form generateForm(AppDefinition appDef, String formDefId) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        FormService formService = (FormService) appContext.getBean("formService");
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao)appContext.getBean("formDefinitionDao");

        // proceed without cache
        if (appDef != null && formDefId != null && !formDefId.isEmpty()) {
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef != null) {
                String json = formDef.getJson();
                Form form = (Form)formService.createElementFromJson(json);

                return form;
            }

        }
        return null;
    }
}

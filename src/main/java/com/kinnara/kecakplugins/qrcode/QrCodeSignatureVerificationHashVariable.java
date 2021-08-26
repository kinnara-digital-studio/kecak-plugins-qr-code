package com.kinnara.kecakplugins.qrcode;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author aristo
 *
 * Not yet implemented
 */
public class QrCodeSignatureVerificationHashVariable extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "qrSignatureVerification";
    }

    @Override
    public String processHashVariable(String key) {
        String[] split = key.split("\\|", 2);
        if(split.length < 2) {
            return "";
        }

        LogUtil.info(getClassName(), "split[0] : " + split[0]);
        LogUtil.info(getClassName(), "split[1] : " + split[1]);
        Pattern p = Pattern.compile("\\{[^}]+\\}");
        Matcher m = p.matcher(key);
        if(m.find()) {
            LogUtil.info(getClassName(), "m.group : " + m.group(split[0]));;
        }

        String data = SecurityUtil.decrypt(split[0]);
        LogUtil.info(getClassName(), "data : " + data);;
        JSONObject jsonContent = Optional.of(data)
                .map(Try.onFunction(JSONObject::new))
                .orElseGet(JSONObject::new);
        LogUtil.info(getClassName(), "jsonContent : " + jsonContent);
        String result = jsonContent.optString(split[1]);
        LogUtil.info(getClassName(), "result : " + result);
        return result;
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
        return "QR Code Signature Verification";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }

    @Override
    public Collection<String> availableSyntax() {
        Collection<String> list = new ArrayList();
        list.add(this.getPrefix() + ".DATA|FIELD");
        return list;
    }
}

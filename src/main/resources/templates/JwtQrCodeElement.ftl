<style type="text/css">
.pnx-icon-qr-code {
    width: 24px !important;
    height: 24px !important;
}
</style>

<div class="form-cell" ${elementMetaData!}>
    <#if includeMetaData!>
        <label class="label">${element.properties.label}</label>
        <img class="pnx-icon-qr-code" src="${request.contextPath}/plugin/${className}/images/qr-logo.png" />
        <span class="form-floating-label">QR Code</span>
    <#else>
        <#--<label class="label">${element.properties.label}</label>-->
        <#--<br>-->
        <div class="row">

            <div class="${element.properties.position}">
                <#if (element.properties.clickableQrCode!'') == 'true' >
                    <a href="${link}"><img class="img-responsive center-block" src="data:image/png;base64,${base64EncodedImage!''}" width="${element.properties.width!320}" height="${element.properties.height!320}" /> </a>
                <#else>
                    <img class="img-responsive center-block" src="data:image/png;base64,${base64EncodedImage!''}" width="${element.properties.width!320}" height="${element.properties.height!320}" />
                </#if>
                <#if element.properties.showQrContent! == 'true'>
                    <b>${element.properties.content!}</b>
                </#if>
            </div>

        </div>
    </#if>
</div>
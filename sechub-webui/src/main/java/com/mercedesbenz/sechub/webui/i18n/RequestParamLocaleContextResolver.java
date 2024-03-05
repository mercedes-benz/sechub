// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.i18n;

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.i18n.LocaleContextResolver;

@Component(WebHttpHandlerBuilder.LOCALE_CONTEXT_RESOLVER_BEAN_NAME)
public class RequestParamLocaleContextResolver implements LocaleContextResolver {
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        List<String> lang = exchange.getRequest().getQueryParams().get("lang");
        Locale targetLocale = null;

        if (lang != null && !lang.isEmpty()) {
            targetLocale = Locale.forLanguageTag(lang.get(0));
        }
        if (targetLocale == null) {
            targetLocale = Locale.US;
        }
        return new SimpleLocaleContext(targetLocale);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
        throw new UnsupportedOperationException("Cannot change lang query parameter - use a different locale context resolution strategy");
    }
}

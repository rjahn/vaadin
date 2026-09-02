/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.server;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.vaadin.util.InternalUrlUtil;

/**
 * An abstract base class for DeploymentConfiguration implementations. This
 * class provides default implementation for common config properties.
 *
 * @since 7.4
 *
 * @author Vaadin Ltd
 */
public abstract class AbstractDeploymentConfiguration
        implements DeploymentConfiguration {

    @Override
    public String getUIClassName() {
        return getApplicationOrSystemProperty(VaadinSession.UI_PARAMETER, null);
    }

    @Override
    public String getUIProviderClassName() {
        return getApplicationOrSystemProperty(
                Constants.SERVLET_PARAMETER_UI_PROVIDER, null);
    }

    @Override
    public String getWidgetset(String defaultValue) {
        return getApplicationOrSystemProperty(Constants.PARAMETER_WIDGETSET,
                defaultValue);
    }

    @Override
    public String getResourcesPath() {
        return getApplicationOrSystemProperty(
                Constants.PARAMETER_VAADIN_RESOURCES, null);
    }

    @Override
    public String getClassLoaderName() {
        return getApplicationOrSystemProperty("ClassLoader", null);
    }
    
    @Override
    public Set<String> getUrlSafeSchemes() {
        Set<String> schemes = InternalUrlUtil.getUrlSafeSchemes();
        
        if (!schemes.isEmpty()) {
            return schemes;
        }
        
        String config = getApplicationOrSystemProperty(Constants.URL_SAFE_SCHEMES, null);
        
        final Set<String> safeSchemes = parseSafeSchemes(config);
        
        InternalUrlUtil.setUrlSafeSchemes(safeSchemes != null ? safeSchemes : DeploymentConfiguration.super.getUrlSafeSchemes());
        
        // read to support changes in set method
        return InternalUrlUtil.getUrlSafeSchemes();
    }

    private static Set<String> parseSafeSchemes(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        Set<String> schemes = new HashSet<>();
        
        for (String scheme : value.split(",")) {
            String trimmed = scheme.trim();
            
            if (!trimmed.isEmpty()) {
                schemes.add(trimmed.toLowerCase(Locale.ROOT));
            }
        }
        
        return schemes.isEmpty() ? null : schemes;
    }    
}

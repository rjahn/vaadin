/*
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
package com.vaadin.util;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.Constants;
import com.vaadin.server.VaadinService;

/**
 * Internal utility class for URL handling.
 *
 * @see <a href="https://github.com/vaadin/flow/blob/main/flow-server/src/main/java/com/vaadin/flow/internal/UrlUtil.java">UrlUtil</a> 
 */
public class InternalUrlUtil {
    static final Set<String> ALLOW_ALL_URL_SAFE_SCHEMES = Collections.singleton(Constants.URL_SAFE_SCHEMES_ALL);

    private static Set<String> urlSafeSchemes = Collections.unmodifiableSet(Collections.emptySet());

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(InternalUrlUtil.class.getName());
    }
    
    
    /**
     * Gets currently configured safe schemes.
     * 
     * @return the schemes set
     */
    public static synchronized Set<String> getUrlSafeSchemes() {
        return urlSafeSchemes;
    }

    /**
     * Sets safe schemes.
     * 
     * @param urlSafeSchemes the schemes
     */
    public static synchronized void setUrlSafeSchemes(Set<String> urlSafeSchemes) {
        if (urlSafeSchemes != null) {
            urlSafeSchemes = Collections.unmodifiableSet(urlSafeSchemes);
        } else {
            urlSafeSchemes = Collections.unmodifiableSet(Collections.emptySet());
        }
        
        getLogger().fine("Set safe URL schemes to: " + urlSafeSchemes);
    }

    /**
     * Gets whether given URL is a safe URL.
     * 
     * @param url the URL
     * @return <code>true</code> if safe URL, <code>false</code> otherwise
     */
    public static boolean isSafeUrl(String url) {
        Set<String> safeSchemes = getUrlSafeSchemes();
        
        //No safe schemes available -> try to configure
        if (safeSchemes.isEmpty()) {
            VaadinService service = VaadinService.getCurrent();
            
            if (service == null) {
                getLogger().log(Level.WARNING, "Use default safe scheme configuration because no configuration found in: {}", Constants.URL_SAFE_SCHEMES);
                
                safeSchemes = ALLOW_ALL_URL_SAFE_SCHEMES;
                
                // only internal
                //setUrlSafeSchemes(safeSchemes);
            } else {
                safeSchemes = service.getDeploymentConfiguration().getUrlSafeSchemes();
                
                // save set schemes
                setUrlSafeSchemes(safeSchemes);
            }
        }
        
        return isSafeUrl(url, safeSchemes);
    }

    /**
     * Creates the error message for unsafe URLs.
     * 
     * @param type the type e.g. src, path
     * @param url the URL
     * @param unsafeMethod the method to use for using as safe URL, e.g. <code>openUnsafe(String, String)</code> or <code>new ExternalResource(String, true)</code>
     * @return the error message
     */
    public static String createUnsafeUrlErrorMessage(String type, String url, String unsafeMethod) {
    	StringBuffer sbf = new StringBuffer();
    	sbf.append("The ");
    	sbf.append("'");
    	sbf.append(url);
    	sbf.append("' is not safe (type: ");
    	sbf.append(type);
    	sbf.append("). Configure the safe schemes with '");
    	sbf.append(Constants.URL_SAFE_SCHEMES);
    	sbf.append("' or use '");
    	sbf.append(unsafeMethod);
    	sbf.append("' if this URL should be safe.");
    	
    	return sbf.toString();
    }

    /**
     * Checks whether the given url contains a valid schema.
     * 
     * @param url the URL
     * @param safeSchemes safe schemes
     */
    public static boolean isSafeUrl(String url, Set<String> safeSchemes) {
        if (url == null) {
            return false;
        }
        
        //Empty and ALL means: every scheme is safe -> backwards compatibility for legacy apps
        if (safeSchemes == null || safeSchemes.isEmpty()) {
        	return true;
        }
        
        if (safeSchemes.contains(Constants.URL_SAFE_SCHEMES_ALL)) {
            return true;
        }
        
        String trimmed = url.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        
        // Don't allow control characters (e.g. "java\tscript:alert(1)")
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isISOControl(trimmed.charAt(i))) {
                return false;
            }
        }
        String scheme = extractScheme(trimmed);
        
        if (scheme == null) {
            // Relative URLs don't have a scheme -> valid
            return true;
        }
        
        return safeSchemes.contains(scheme.toLowerCase(Locale.ROOT));
    }

    /**
     * Extracts the scheme from the given URL, or returns {@code null} if the URL is relative (has no scheme). 
     * The scheme is determined according to RFC 3986: a letter followed by any number of letters, digits,
     * {@code '+'}, {@code '-'} or {@code '.'}, terminated by a {@code ':'} that occurs before any {@code '/'}, 
     * {@code '?'} or {@code '#'}.
     * 
     * The scheme is extracted without parsing the whole URL so that valid relative URLs containing characters 
     * that a strict URI parser would reject (such as spaces) are not falsely flagged.
     */
    private static String extractScheme(String url) {
        int schemeEnd = url.indexOf(':');
        
        if (schemeEnd <= 0) {
            return null;
        }
        
        if (!isLetter(url.charAt(0))) {
            return null;
        }
        
        for (int i = 1; i < schemeEnd; i++) {
            char c = url.charAt(i);
            
            boolean validSchemeChar = isLetter(c) || isDigit(c) || c == '+' || c == '-' || c == '.';
            
            if (!validSchemeChar) {
                // The ':' belongs to the path or query, so there is no scheme
                // and the URL is relative.
                return null;
            }
        }
        
        return url.substring(0, schemeEnd);
    }

}

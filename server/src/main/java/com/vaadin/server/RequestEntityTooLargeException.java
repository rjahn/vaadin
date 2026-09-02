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
package com.vaadin.server;

import java.io.IOException;

/**
 * The <code>RequestEntityTooLargeException</code> should be used when the server
 * should return HTTP status 413 (Request Entity Too Large).
 */
public class RequestEntityTooLargeException extends IOException {

    private final long maxBodySize;

    /**
     * Creates a new instance of <code>RequestEntityTooLargeException</code>.
     *
     * @param maxBodySize the configured maximum request body size
     */
    public RequestEntityTooLargeException(long maxBodySize) {
        super("Request entity too large. It exceeds " + maxBodySize
                + ". Use the configuration property '"
                + Constants.SERVLET_PARAMETER_MAX_REQUEST_BODY_SIZE
                + "' for configuration (-1 to disable).");
        
        this.maxBodySize = maxBodySize;
    }

    /**
     * Gets the configured maximum request body size.
     *
     * @return the maximum request body size
     */
    public long getMaxBodySize() {
        return maxBodySize;
    }
    
}

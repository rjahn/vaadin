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

import java.io.IOException;
import java.io.Reader;

/**
 * RequestHandler which takes care of locking and unlocking of the VaadinSession
 * automatically. The session is locked before
 * {@link #synchronizedHandleRequest(VaadinSession, VaadinRequest, VaadinResponse)}
 * is called and unlocked after it has completed.
 *
 * @author Vaadin Ltd
 * @version @VERSION@
 * @since 7.1
 */
public abstract class SynchronizedRequestHandler implements RequestHandler {
	
    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
        if (!canHandleRequest(request)) {
            return false;
        }
        
        return handleCheckedRequest(session, request, response);
    }
    
    /**
     * Handles the request which is already checked with {@link #canHandleRequest(VaadinRequest)}.
     * 
     * @param session the session
     * @param request the request
     * @param response the response
     * @return <code>true</code> if response has been written
     * @throws IOException
     */
    protected boolean handleCheckedRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
    	session.lock();
        try {
            return synchronizedHandleRequest(session, request, response);
        } finally {
            session.unlock();
        }
    }

    /**
     * Identical to
     * {@link #handleRequest(VaadinSession, VaadinRequest, VaadinResponse)}
     * except the {@link VaadinSession} is locked before this is called and
     * unlocked after this has completed.
     *
     * @see #handleRequest(VaadinSession, VaadinRequest, VaadinResponse)
     * @param session
     *            The session for the request
     * @param request
     *            The request to handle
     * @param response
     *            The response object to which a response can be written.
     * @return true if a response has been written and no further request
     *         handlers should be called, otherwise false
     *
     * @throws IOException
     *             If an IO error occurred
     */
    public abstract boolean synchronizedHandleRequest(VaadinSession session,
            VaadinRequest request, VaadinResponse response) throws IOException;

    /**
     * Check whether a request may be handled by this handler. This can be used
     * as an optimization to avoid locking the session just to investigate some
     * method property. The default implementation just returns
     * <code>true</code> which means that all requests will be handled by
     * calling
     * {@link #synchronizedHandleRequest(VaadinSession, VaadinRequest, VaadinResponse)}
     * with the session locked.
     *
     * @since 7.2
     * @param request
     *            the request to handle
     * @return <code>true</code> if the request handling should continue once
     *         the session has been locked; <code>false</code> if there's no
     *         need to lock the session since the request would still not be
     *         handled.
     */
    protected boolean canHandleRequest(VaadinRequest request) {
        return true;
    }
    
    /**
     * Reads the request body from the given reader without limitation checks.
     *
     * @param reader
     *            the reader to read from
     * @return request body as a String
     * @throws IOException
     *             if reading fails
     */
    public static String getRequestBody(Reader reader) throws IOException {
        return getRequestBody(reader, -1L);
    }    
    
    /**
     * Reads the request body from the given reader as long as smaller than the max body size.
     *
     * @param reader
     *            the reader to read from
     * @param maxBodySize
     *            the maximum body size limit
     * @return the request body as a string
     * @throws IOException
     *             if reading fails
     * @throws RequestEntityTooLargeException
     *             if the body exceeds max body size
     */
    public static String getRequestBody(Reader reader, long maxBodySize) throws IOException {
        StringBuilder sb = new StringBuilder(Constants.MAX_BUFFER_SIZE);
        char[] buffer = new char[Constants.MAX_BUFFER_SIZE];
        long total = 0;

        while (true) {
            int read = reader.read(buffer);
            if (read == -1) {
                break;
            }
            
            total += read;
            
            if (maxBodySize >= 0 && total > maxBodySize) {
                throw new RequestEntityTooLargeException(maxBodySize);
            }
            
            sb.append(buffer, 0, read);
        }

        return sb.toString();
    }

    /**
     * Gets the configured maximum request body size for the given request.
     *
     * @param request
     *            the request
     * @return the maximum request body size in characters or -1 if disabled
     */
    public static long getMaxRequestBodySize(VaadinRequest request) {
        return request.getService().getDeploymentConfiguration().getMaxRequestBodySize();
    }    

}

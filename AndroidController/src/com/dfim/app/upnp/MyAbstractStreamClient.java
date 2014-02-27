/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.dfim.app.upnp;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamClientConfiguration;
import org.seamless.util.Exceptions;

import android.util.Log;

import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;

/**
 * Implements the timeout/callback processing and unifies exception handling.
 * 
 * @author Christian Bauer
 */
// Timeout of // sendBroadcast
public abstract class MyAbstractStreamClient<C extends StreamClientConfiguration, REQUEST> implements StreamClient<C> {

	final private static Logger log = Logger.getLogger(StreamClient.class.getName());
	private static final String TAG = MyAbstractStreamClient.class.getSimpleName() + " ";

	@Override
	public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage) throws InterruptedException {

		if (log.isLoggable(Level.FINE))
			log.fine("Preparing HTTP request: " + requestMessage);

		REQUEST request = createRequest(requestMessage);
		if (request == null)
			return null;

		Callable<StreamResponseMessage> callable = createCallable(requestMessage, request);

		// We want to track how long it takes
		long start = System.currentTimeMillis();

		// Execute the request on a new thread
		Future<StreamResponseMessage> future = getConfiguration().getRequestExecutorService().submit(callable);

		// Wait on the current thread for completion
		try {
			if (log.isLoggable(Level.FINE))
				log.fine("Waiting " + getConfiguration().getTimeoutSeconds() + " seconds for HTTP request to complete: " + requestMessage);
			StreamResponseMessage response = future.get(getConfiguration().getTimeoutSeconds(), TimeUnit.SECONDS);

			// Log a warning if it took too long
			long elapsed = System.currentTimeMillis() - start;
			if (log.isLoggable(Level.FINEST))
				log.finest("Got HTTP response in " + elapsed + "ms: " + requestMessage);
			if (getConfiguration().getLogWarningSeconds() > 0 && elapsed > getConfiguration().getLogWarningSeconds() * 1000) {
				log.warning("HTTP request took a long time (" + elapsed + "ms): " + requestMessage);
			}

			return response;

		} catch (InterruptedException ex) {

			if (log.isLoggable(Level.FINE))
				log.fine("Interruption, aborting request: " + requestMessage);
			abort(request);
			throw new InterruptedException("HTTP request interrupted and aborted");

		} catch (TimeoutException ex) {

			log.info("Timeout of " + getConfiguration().getTimeoutSeconds() + " seconds while waiting for HTTP request to complete, aborting: " + requestMessage);
			if (getIpAddress(requestMessage).equals(Constant.DEVICE_IP_ADDRESS)) { // 条件：当前请求的IP端口等于已经选定设备的IP端口
				UpnpApp.sendBroadcast(Constant.ACTION_DEAL_STREAMCLIENT_TIMEOUT_OR_FAILURE);
				Log.e(TAG, "sendBroadcast --> TimeoutException");
			}
			abort(request);
			return null;

		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (!logExecutionException(cause)) {
				log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(cause));
			}
			return null;
		} finally {
			onFinally(request);
		}
	}

	private String getIpAddress(StreamRequestMessage requestMessage) {
		String str = requestMessage.toString();
		String ip = str.substring(str.indexOf("http://") + 7, str.lastIndexOf(":"));
		Log.e(TAG, "TimeoutException:ip=" + ip);
		return ip;
	}

	/**
	 * Create a proprietary representation of this request, log warnings and
	 * return <code>null</code> if creation fails.
	 */
	abstract protected REQUEST createRequest(StreamRequestMessage requestMessage);

	/**
	 * Create a callable procedure that will execute the request.
	 */
	abstract protected Callable<StreamResponseMessage> createCallable(StreamRequestMessage requestMessage, REQUEST request);

	/**
	 * Cancel and abort the request immediately, with the proprietary API.
	 */
	abstract protected void abort(REQUEST request);

	/**
	 * @return <code>true</code> if no more logging of this exception should be
	 *         done.
	 */
	abstract protected boolean logExecutionException(Throwable t);

	protected void onFinally(REQUEST request) {
		// Do nothing
	}

}

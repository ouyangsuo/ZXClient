package com.kitty.poclient.upnp;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.ContentTypeHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamClient;
import org.seamless.util.Exceptions;
import org.seamless.util.MimeType;

import android.util.Log;

import com.kitty.poclient.common.Constant;
import com.kitty.poclient.common.UpnpApp;

// sendBroadcast
public class MyStreamClientImpl extends MyAbstractStreamClient<StreamClientConfigurationImpl, MyStreamClientImpl.HttpContentExchange> {

	final private static Logger log = Logger.getLogger(StreamClient.class.getName());
	private static final String TAG = MyStreamClientImpl.class.getSimpleName() + " ";

	final protected StreamClientConfigurationImpl configuration;
	final protected HttpClient client;

	public MyStreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
		this.configuration = configuration;

		log.info("Starting Jetty HttpClient...");
		client = new HttpClient();

		// Jetty client needs threads for its internal expiration routines,
		// which we don't need but
		// can't disable, so let's abuse the request executor service for this
		client.setThreadPool(new ExecutorThreadPool(getConfiguration().getRequestExecutorService()) {
			@Override
			protected void doStop() throws Exception {
				// Do nothing, don't shut down the Cling ExecutorService when
				// Jetty stops!
			}
		});

		// These are some safety settings, we should never run into these
		// timeouts as we
		// do our own expiration checking
		client.setTimeout((configuration.getTimeoutSeconds() + 5) * 1000);
		client.setConnectTimeout((configuration.getTimeoutSeconds() + 5) * 1000);

		client.setMaxRetries(configuration.getRequestRetryCount());

		try {
			client.start();
		} catch (Exception ex) {
			throw new InitializationException("Could not start Jetty HTTP client: " + ex, ex);
		}
	}

	@Override
	public StreamClientConfigurationImpl getConfiguration() {
		return configuration;
	}

	@Override
	protected HttpContentExchange createRequest(StreamRequestMessage requestMessage) {
		return new HttpContentExchange(getConfiguration(), client, requestMessage);
	}

	@Override
	protected Callable<StreamResponseMessage> createCallable(final StreamRequestMessage requestMessage, final HttpContentExchange exchange) {
		return new Callable<StreamResponseMessage>() {
			public StreamResponseMessage call() throws Exception {

				if (log.isLoggable(Level.FINE))
					log.fine("Sending HTTP request: " + requestMessage);

				try {
					client.send(exchange);
				} catch (Exception e) {
					e.printStackTrace();
					if(true){// 条件：当前请求的IP端口等于已经选定设备的IP端口
						UpnpApp.sendBroadcast(Constant.ACTION_DEAL_STREAMCLIENT_TIMEOUT_OR_FAILURE);
						Log.e(TAG, "sendBroadcast --> ConnectionFailure");
					}
				}
				int exchangeState = exchange.waitForDone();

				if (exchangeState == HttpExchange.STATUS_COMPLETED) {
					try {
						return exchange.createResponse();
					} catch (Throwable t) {
						log.log(Level.WARNING, "Error reading response: " + requestMessage, Exceptions.unwrap(t));
						return null;
					}
				} else if (exchangeState == HttpExchange.STATUS_CANCELLED) {
					// That's ok, happens when we abort the exchange after
					// timeout
					return null;
				} else if (exchangeState == HttpExchange.STATUS_EXCEPTED) {
					// The warnings of the "excepted" condition are logged in
					// HttpContentExchange
					return null;
				} else {
					log.warning("Unhandled HTTP exchange status: " + exchangeState);
					return null;
				}
			}
		};
	}

	@Override
	protected void abort(HttpContentExchange exchange) {
		exchange.cancel();
	}

	@Override
	protected boolean logExecutionException(Throwable t) {
		return false;
	}

	@Override
	public void stop() {
		try {
			client.stop();
		} catch (Exception ex) {
			log.info("Error stopping HTTP client: " + ex);
		}
	}

	static public class HttpContentExchange extends ContentExchange {

		final protected StreamClientConfigurationImpl configuration;
		final protected HttpClient client;
		final protected StreamRequestMessage requestMessage;

		protected Throwable exception;

		public HttpContentExchange(StreamClientConfigurationImpl configuration, HttpClient client, StreamRequestMessage requestMessage) {
			super(true);
			this.configuration = configuration;
			this.client = client;
			this.requestMessage = requestMessage;
			applyRequestURLMethod();
			applyRequestHeaders();
			applyRequestBody();
		}

		@Override
		protected void onConnectionFailed(Throwable t) {
			log.log(Level.WARNING, "HTTP connection failed: " + requestMessage, Exceptions.unwrap(t));
		}

		@Override
		protected void onException(Throwable t) {
			log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(t));
		}

		public StreamClientConfigurationImpl getConfiguration() {
			return configuration;
		}

		public StreamRequestMessage getRequestMessage() {
			return requestMessage;
		}

		protected void applyRequestURLMethod() {
			final UpnpRequest requestOperation = getRequestMessage().getOperation();
			if (log.isLoggable(Level.FINE))
				log.fine("Preparing HTTP request message with method '" + requestOperation.getHttpMethodName() + "': " + getRequestMessage());

			setURL(requestOperation.getURI().toString());
			setMethod(requestOperation.getHttpMethodName());
		}

		protected void applyRequestHeaders() {
			// Headers
			UpnpHeaders headers = getRequestMessage().getHeaders();
			if (log.isLoggable(Level.FINE))
				log.fine("Writing headers on HttpContentExchange: " + headers.size());
			// TODO Always add the Host header
			// TODO: ? setRequestHeader(UpnpHeader.Type.HOST.getHttpName(), );
			// Add the default user agent if not already set on the message
			if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
				setRequestHeader(UpnpHeader.Type.USER_AGENT.getHttpName(), getConfiguration().getUserAgentValue(getRequestMessage().getUdaMajorVersion(), getRequestMessage().getUdaMinorVersion()));
			}
			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				for (String v : entry.getValue()) {
					String headerName = entry.getKey();
					if (log.isLoggable(Level.FINE))
						log.fine("Setting header '" + headerName + "': " + v);
					addRequestHeader(headerName, v);
				}
			}
		}

		protected void applyRequestBody() {
			// Body
			if (getRequestMessage().hasBody()) {
				if (getRequestMessage().getBodyType() == UpnpMessage.BodyType.STRING) {
					if (log.isLoggable(Level.FINE))
						log.fine("Writing textual request body: " + getRequestMessage());

					MimeType contentType = getRequestMessage().getContentTypeHeader() != null ? getRequestMessage().getContentTypeHeader().getValue() : ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8;

					String charset = getRequestMessage().getContentTypeCharset() != null ? getRequestMessage().getContentTypeCharset() : "UTF-8";

					setRequestContentType(contentType.toString());
					ByteArrayBuffer buffer;
					try {
						buffer = new ByteArrayBuffer(getRequestMessage().getBodyString(), charset);
					} catch (UnsupportedEncodingException ex) {
						throw new RuntimeException("Unsupported character encoding: " + charset, ex);
					}
					setRequestHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
					setRequestContent(buffer);

				} else {
					if (log.isLoggable(Level.FINE))
						log.fine("Writing binary request body: " + getRequestMessage());

					if (getRequestMessage().getContentTypeHeader() == null)
						throw new RuntimeException("Missing content type header in request message: " + requestMessage);
					MimeType contentType = getRequestMessage().getContentTypeHeader().getValue();

					setRequestContentType(contentType.toString());
					ByteArrayBuffer buffer;
					buffer = new ByteArrayBuffer(getRequestMessage().getBodyBytes());
					setRequestHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
					setRequestContent(buffer);
				}
			}
		}

		protected StreamResponseMessage createResponse() {
			// Status
			UpnpResponse responseOperation = new UpnpResponse(getResponseStatus(), UpnpResponse.Status.getByStatusCode(getResponseStatus()).getStatusMsg());

			if (log.isLoggable(Level.FINE))
				log.fine("Received response: " + responseOperation);

			StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);

			// Headers
			UpnpHeaders headers = new UpnpHeaders();
			HttpFields responseFields = getResponseFields();
			for (String name : responseFields.getFieldNamesCollection()) {
				for (String value : responseFields.getValuesCollection(name)) {
					headers.add(name, value);
				}
			}
			responseMessage.setHeaders(headers);

			// Body
			byte[] bytes = getResponseContentBytes();
			if (bytes != null && bytes.length > 0 && responseMessage.isContentTypeMissingOrText()) {

				if (log.isLoggable(Level.FINE))
					log.fine("Response contains textual entity body, converting then setting string on message");
				try {
					responseMessage.setBodyCharacters(bytes);
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException("Unsupported character encoding: " + ex, ex);
				}

			} else if (bytes != null && bytes.length > 0) {

				if (log.isLoggable(Level.FINE))
					log.fine("Response contains binary entity body, setting bytes on message");
				responseMessage.setBody(UpnpMessage.BodyType.BYTES, bytes);

			} else {
				if (log.isLoggable(Level.FINE))
					log.fine("Response did not contain entity body");
			}

			if (log.isLoggable(Level.FINE))
				log.fine("Response message complete: " + responseMessage);
			return responseMessage;
		}
	}
}

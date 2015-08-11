/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.reactive.web.rxnetty;

import java.net.URI;
import java.net.URISyntaxException;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.reactivestreams.Publisher;
import rx.Observable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.reactive.web.ServerHttpRequest;
import org.springframework.util.Assert;

/**
 * @author Rossen Stoyanchev
 */
public class RxNettyServerHttpRequest implements ServerHttpRequest {

	private final HttpServerRequest<ByteBuf> request;

	private HttpHeaders headers;


	public RxNettyServerHttpRequest(HttpServerRequest<ByteBuf> request) {
		Assert.notNull("'request', request must not be null.");
		this.request = request;
	}


	@Override
	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			for (String name : this.request.getHeaderNames()) {
				for (String value : this.request.getAllHeaderValues(name)) {
					this.headers.add(name, value);
				}
			}
		}
		return this.headers;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.valueOf(this.request.getHttpMethod().name());
	}

	@Override
	public URI getURI() {
		try {
			return new URI(this.request.getUri());
		}
		catch (URISyntaxException ex) {
			throw new IllegalStateException("Could not get URI: " + ex.getMessage(), ex);
		}

	}

	@Override
	public Publisher<byte[]> getBody() {
		Observable<byte[]> bytesContent = this.request.getContent().map(byteBuf -> {
			byte[] copy = new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(copy);
			return copy;
		});
		return rx.RxReactiveStreams.toPublisher(bytesContent);
	}

}

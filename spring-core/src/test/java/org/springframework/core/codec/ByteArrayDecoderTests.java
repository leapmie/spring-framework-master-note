/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.core.codec;

import java.util.Collections;

import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.AbstractDataBufferAllocatingTestCase;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeTypeUtils;

import static org.junit.Assert.*;

/**
 * @author Arjen Poutsma
 */
public class ByteArrayDecoderTests extends AbstractDataBufferAllocatingTestCase {

	private final ByteArrayDecoder decoder = new ByteArrayDecoder();


	@Test
	public void canDecode() {
		assertTrue(this.decoder.canDecode(ResolvableType.forClass(byte[].class),
				MimeTypeUtils.TEXT_PLAIN));
		assertFalse(this.decoder.canDecode(ResolvableType.forClass(Integer.class),
				MimeTypeUtils.TEXT_PLAIN));
		assertTrue(this.decoder.canDecode(ResolvableType.forClass(byte[].class),
				MimeTypeUtils.APPLICATION_JSON));
	}

	@Test
	public void decode() {
		DataBuffer fooBuffer = stringBuffer("foo");
		DataBuffer barBuffer = stringBuffer("bar");
		Flux<DataBuffer> source = Flux.just(fooBuffer, barBuffer);
		Flux<byte[]> output = this.decoder.decode(source,
				ResolvableType.forClassWithGenerics(Publisher.class, byte[].class),
				null, Collections.emptyMap());

		StepVerifier.create(output)
				.consumeNextWith(bytes -> assertArrayEquals("foo".getBytes(), bytes))
				.consumeNextWith(bytes -> assertArrayEquals("bar".getBytes(), bytes))
				.expectComplete()
				.verify();
	}

	@Test
	public void decodeError() {
		DataBuffer fooBuffer = stringBuffer("foo");
		Flux<DataBuffer> source =
				Flux.just(fooBuffer).concatWith(Flux.error(new RuntimeException()));
		Flux<byte[]> output = this.decoder.decode(source,
				ResolvableType.forClassWithGenerics(Publisher.class, byte[].class),
				null, Collections.emptyMap());

		StepVerifier.create(output)
				.consumeNextWith(bytes -> assertArrayEquals("foo".getBytes(), bytes))
				.expectError()
				.verify();
	}

	@Test
	public void decodeToMono() {
		DataBuffer fooBuffer = stringBuffer("foo");
		DataBuffer barBuffer = stringBuffer("bar");
		Flux<DataBuffer> source = Flux.just(fooBuffer, barBuffer);
		Mono<byte[]> output = this.decoder.decodeToMono(source,
				ResolvableType.forClassWithGenerics(Publisher.class, byte[].class),
				null, Collections.emptyMap());

		StepVerifier.create(output)
				.consumeNextWith(bytes -> assertArrayEquals("foobar".getBytes(), bytes))
				.expectComplete()
				.verify();
	}

}

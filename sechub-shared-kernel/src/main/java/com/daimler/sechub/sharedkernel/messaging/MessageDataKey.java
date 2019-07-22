// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

/**
 * A message data key represents data inside a message. Where the message itself is represented by its {@link MessageID} the content
 * inside the message is represented by the {@link MessageDataKey}. Each key has an identifier but also an provider which is able
 * to convert data of message to corresponding java type.
 * @author Albert Tregnaghi
 *
 * @param <T>
 */
public class MessageDataKey<T> {

	private String id;
	private MessageDataProvider<T> provider;

	/**
	 * A new message data key
	 * @param id - just an identifier which is used internally for the key
	 * @param provider
	 */
	MessageDataKey(String id, MessageDataProvider<T> provider) {
		if (id==null) {
			throw new IllegalArgumentException("id may not be null");
		}
		if (provider==null) {
			throw new IllegalArgumentException("provider may not be null");
		}
		this.id=id;
		this.provider=provider;
	}

	/**
	 * @return message data provider, never <code>null</code>
	 */
	public MessageDataProvider<T> getProvider() {
		return provider;
	}

	/**
	 *
	 * @return message data key identifier
	 */
	public String getId() {
		return id;
	}
}
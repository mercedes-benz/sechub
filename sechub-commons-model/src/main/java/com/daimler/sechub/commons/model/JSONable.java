// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An interface for objects providing JSON marshaling. Contains also an default
 * implementation. It represents also a marker interface for Objects being used
 * as JSON in SecHub context, so it's easier to find them. <br>
 * <br>
 * Of course spring boot supports REST operations with automatic json
 * marshalling. But using this interface the marshalling mechanism is also
 * available outside. So if you have an object which needs only to be marshalled
 * by spring boot rest magic you do not need to implement the interface. If you
 * want to jsonify/dejsonify objects outside this "magic" you just need to
 * implement the interface to get this out of the box<br>
 * 
 * @author Albert Tregnaghi
 *
 */
public interface JSONable<T> {

	@JsonIgnore
	Class<T> getJSONTargetClass();

	/**
	 * Converts object to JSON - not pretty printed
	 * 
	 * @return
	 * @throws JSONConverterException
	 */
	default String toJSON() throws JSONConverterException {
		return getConverter().toJSON(this);
	}

	/**
	 * @return the json converter
	 */
	@JsonIgnore
	default JSONConverter getConverter() {
		return JSONConverter.get();
	}

	/**
	 * Creates a full new JSON object from given string. <br>
	 * It's a good practice to provide a static default instance (e.g. "INSTANCE" ) of the 
	 * implementing class as a public constant. So you can use this method as a factory... 
	 * 
	 * @param json
	 * @return
	 * @throws JSONConverterException
	 */
	default T fromJSON(String json) throws JSONConverterException {
		return getConverter().fromJSON(getJSONTargetClass(), json);
	}

}

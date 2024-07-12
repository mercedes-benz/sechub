// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

/**
 * Byte array transformer interface. Implementations are able to transform
 * encrypted data (which we always handle as bytes) into an object type and to
 * transform the object type to a byte array.
 *
 * @author Albert Tregnaghi
 *
 * @param <T>
 */
public interface ByteArrayTransformer<T> {

    /**
     * Transform given bytes to target object. If bytes array is null, the object
     * will be <code>null</code>
     *
     * @param bytes
     * @return created object
     */
    public T transformFromBytes(byte[] bytes);

    /**
     * Transforms given object to byte array. If object is <code>null</code>,
     * <code>null</code> will be returned
     *
     * @param object the object to transform (can be <code>null</code>)
     * @return byte array, never <code>null</code>
     */
    public byte[] transformToBytes(T object);

}

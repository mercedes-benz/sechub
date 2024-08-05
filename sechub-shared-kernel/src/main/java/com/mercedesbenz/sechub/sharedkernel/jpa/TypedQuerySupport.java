// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

public class TypedQuerySupport<T> {
    private Class<T> clazz;

    public TypedQuerySupport(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz may not be null!");
        }
        this.clazz = clazz;
    }

    /**
     * @param query
     * @return optional single result
     * @throws IllegalStateException if the query does not return expected type or
     *                               null but another one!
     */
    public Optional<T> getSingleResultAsOptional(Query query) {
        return Optional.ofNullable(getSingleResultOrNull(query));
    }

    /**
     * @param query
     * @return single result or <code>null</code>
     * @throws IllegalStateException if the query does not return expected type or
     *                               null but another one!
     */
    @SuppressWarnings("unchecked")
    public T getSingleResultOrNull(Query query) {
        Object result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            /* ignore, can happen */
        }
        if (result == null) {
            return null;
        }
        if (clazz.isAssignableFrom(result.getClass())) {
            return (T) result;
        }
        throw new IllegalStateException("The given query returns not expected type:" + clazz + " but " + result.getClass());
    }

    @SuppressWarnings("unchecked")
    public List<T> getList(Query query) {
        return query.getResultList();
    }
}

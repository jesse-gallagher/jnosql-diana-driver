/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */

package org.jnosql.diana.hazelcast.key;


import com.hazelcast.core.IAtomicLong;
import org.jnosql.diana.api.key.BucketManagerFactory;

/**
 * The hazelcast implementation of {@link BucketManagerFactory}
 */
public interface HazelCastBucketManagerFactory extends BucketManagerFactory<HazelCastBucketManager> {


    /**
     * Creates a {@link IAtomicLong} implementation
     *
     * @param bucketName the bucket name
     * @return a {@link IAtomicLong} instance
     * @throws NullPointerException when bucketName is null
     */
    IAtomicLong getAtomicLong(String bucketName) throws NullPointerException;
}

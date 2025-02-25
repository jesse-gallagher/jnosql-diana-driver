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
 *   Alessandro Moscatelli
 */
package org.eclipse.jnosql.communication.elasticsearch.document;


import jakarta.nosql.Configurations;
import jakarta.nosql.Settings;
import jakarta.nosql.Settings.SettingsBuilder;
import jakarta.nosql.document.DocumentConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.eclipse.jnosql.communication.driver.ConfigurationReader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * The implementation of {@link DocumentConfiguration}
 * that returns {@link ElasticsearchDocumentCollectionManagerFactory}.
 * It tries to read the configuration properties from diana-elasticsearch.properties file. To get some information:
 * <p>elasticsearch.host.n: the host to client connection, if necessary to define a different port than default just
 * use colon, ':' eg: elasticsearch-host-1=172.17.0.2:1234</p>
 */
public class ElasticsearchDocumentConfiguration implements DocumentConfiguration {

    private static final String FILE_CONFIGURATION = "diana-elasticsearch.properties";
    private static final int DEFAULT_PORT = 9200;

    private List<HttpHost> httpHosts = new ArrayList<>();

    private List<Header> headers = new ArrayList<>();


    public ElasticsearchDocumentConfiguration() {

        Map<String, String> configurations = ConfigurationReader.from(FILE_CONFIGURATION);
        SettingsBuilder builder = Settings.builder();
        configurations.entrySet().forEach(e -> builder.put(e.getKey(), e.getValue()));
        Settings settings = builder.build();

        if (configurations.isEmpty()) {
            return;
        }
        settings.prefix(asList(OldElasticsearchConfigurations.HOST.get(),
                ElasticsearchConfigurations.HOST.get(), Configurations.HOST.get()))
                .stream()
                .map(Object::toString)
                .map(h -> ElasticsearchAddress.of(h, DEFAULT_PORT))
                .map(ElasticsearchAddress::toHttpHost)
                .forEach(httpHosts::add);
    }

    /**
     * Adds a host in the configuration
     *
     * @param host the host
     * @throws NullPointerException when host is null
     */
    public void add(HttpHost host) {
        this.httpHosts.add(Objects.requireNonNull(host, "host is required"));
    }

    /**
     * Adds a header in the configuration
     *
     * @param header the header
     * @throws NullPointerException when header is null
     */
    public void add(Header header) {
        this.headers.add(Objects.requireNonNull(header, "header is required"));
    }

    @Override
    public ElasticsearchDocumentCollectionManagerFactory get() {
        return get(Settings.builder().build());
    }

    @Override
    public ElasticsearchDocumentCollectionManagerFactory get(Settings settings) {
        requireNonNull(settings, "settings is required");

        settings.prefix(asList(OldElasticsearchConfigurations.HOST.get(),
                ElasticsearchConfigurations.HOST.get(), Configurations.HOST.get()))
                .stream()
                .map(Object::toString)
                .map(h -> ElasticsearchAddress.of(h, DEFAULT_PORT))
                .map(ElasticsearchAddress::toHttpHost)
                .forEach(httpHosts::add);

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()]));
        builder.setDefaultHeaders(headers.stream().toArray(Header[]::new));

        final Optional<String> username = settings
                .get(asList(Configurations.USER.get(),
                        ElasticsearchConfigurations.USER.get()))
                .map(Object::toString);
        final Optional<String> password = settings
                .get(asList(Configurations.PASSWORD.get(),
                        ElasticsearchConfigurations.PASSWORD.get()))
                .map(Object::toString);

        if (username.isPresent()) {
            final CredentialsProvider credentialsProvider =
                    new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username.orElse(null), password.orElse(null)));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider));
        }

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return new ElasticsearchDocumentCollectionManagerFactory(client);
    }

    /**
     * returns an {@link ElasticsearchDocumentCollectionManagerFactory} instance
     *
     * @param builder the builder {@link RestClientBuilder}
     * @return a manager factory instance
     * @throws NullPointerException when builder is null
     */
    public ElasticsearchDocumentCollectionManagerFactory get(RestClientBuilder builder) {
        Objects.requireNonNull(builder, "builder is required");
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return new ElasticsearchDocumentCollectionManagerFactory(client);
    }

    /**
     * returns an {@link ElasticsearchDocumentCollectionManagerFactory} instance
     *
     * @param client the client {@link RestHighLevelClient}
     * @return a manager factory instance
     * @throws NullPointerException when client is null
     */
    public ElasticsearchDocumentCollectionManagerFactory get(RestHighLevelClient client) {
        Objects.requireNonNull(client, "client is required");
        return new ElasticsearchDocumentCollectionManagerFactory(client);
    }


}

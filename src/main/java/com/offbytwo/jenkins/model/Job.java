/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.model;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.offbytwo.jenkins.client.JenkinsPostResult;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static org.apache.commons.lang.StringUtils.join;

public class Job extends BaseModel {

    private String name;
    private String url;

    public Job() {}

    public Job(Job from) {
        this(from.getName(), from.getUrl());
    }

    public Job(String name, String url) {
        this();
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public JobWithDetails details() throws IOException {
        return client.get(url, JobWithDetails.class);
    }

    /**
     * Trigger a build without parameters
     */
    public Queue build() throws IOException {
        JenkinsPostResult result = client.post(url + "build");

        // Construct build from url location
        Object location = result.getFirstHeader("Location");
        if (location == null || location.toString() == null || location.toString().isEmpty()) {
            return null;
        }

        // @TODO Isolate queue id from url
        Queue queue = new Queue(-1, location.toString());
        queue.setClient(client);
        return queue;
    }

    /**
     * Trigger a parameterized build
     *
     * @param params the job parameters
     * @throws IOException
     */
    public Queue build(Map<String, String> params, Map<String, File> fileParams) throws IOException {
        // Check that we have not-file params
        String qs = "";
        if(params != null) {
            qs = join(Collections2.transform(params.entrySet(), new MapEntryToQueryStringPair()), "&");
        }

        // Launch build
        JenkinsPostResult result = client.post(url + "buildWithParameters?" + qs, null, null, fileParams);

        // Construct build from url location
        Object location = result.getFirstHeader("Location");
        if (location == null || location.toString() == null || location.toString().isEmpty()) {
            return null;
        }

        // @TODO Isolate queue id from url
        Queue queue = new Queue(-1, location.toString());
        queue.setClient(client);
        return queue;
    }

    private static class MapEntryToQueryStringPair implements Function<Map.Entry<String, String>, String> {
        @Override
        public String apply(Map.Entry<String, String> entry) {
            return encode(entry.getKey()) + "=" + encode(entry.getValue());
        }
    }
}

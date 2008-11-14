package org.reflections.util;

import org.reflections.Configuration;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.filters.Filter;
import org.reflections.scanners.Scanner;

import java.net.URL;
import java.util.Collection;

/**
 *
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public class AbstractConfiguration implements Configuration {
    private Scanner[] scanners;
    private Collection<URL> urls;
    private MetadataAdapter metadataAdapter = new JavassistAdapter();
    private Filter<String> filter;

    public Scanner[] getScanners() {
        return scanners;
    }

    public void setScanners(final Scanner... scanners) {
        this.scanners = scanners;
    }

    public Collection<URL> getUrls() {
        return urls;
    }

    public void setUrls(final Collection<URL> urls) {
        this.urls = urls;
    }

    public MetadataAdapter getMetadataAdapter() {
        return metadataAdapter;
    }

    public void setMetadataAdapter(final MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
    }

    public Filter<String> getFilter() {
        return filter;
    }

    public void setFilter(final Filter<String> filter) {
        this.filter = filter;
    }

}

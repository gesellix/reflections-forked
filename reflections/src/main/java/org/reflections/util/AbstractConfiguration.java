package org.reflections.util;

import org.reflections.Configuration;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.filters.Filter;
import org.reflections.scanners.Scanner;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 *
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public class AbstractConfiguration implements Configuration {
    private List<Scanner> scanners;
    private Collection<URL> urls;
    private MetadataAdapter metadataAdapter = new JavassistAdapter();

    public List<Scanner> getScanners() {
		return scanners;
	}

    public void setScanners(final Scanner... scanners) {
        this.scanners = ImmutableList.of(scanners);
    }

    public Collection<URL> getUrls() {
        return urls;
    }

    public void setUrls(final Collection<URL> urls) {
		this.urls = ImmutableList.copyOf(urls);
	}

    public MetadataAdapter getMetadataAdapter() {
        return metadataAdapter;
    }

    public void setMetadataAdapter(final MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
    }

	public void applyUniversalFilter(Filter<String> filter) {
		for (Scanner scanner : scanners) {
			scanner.filterBy(filter);
		}
	}
}

package org.reflections;

import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.Scanner;
import org.reflections.filters.Filter;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public interface Configuration {
    List<Scanner> getScanners();
    void setScanners(Scanner ... scanners);

    /**
     * urls to be scanned. use ClasspathHelper convenient methods
     */
    void setUrls(Collection<URL> urls);
    Collection<URL> getUrls();

    MetadataAdapter getMetadataAdapter();
    void setMetadataAdapter(MetadataAdapter metadataAdapter);

	void applyUniversalFilter(Filter<String> filter);
}

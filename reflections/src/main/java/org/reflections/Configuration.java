package org.reflections;

import org.reflections.adapters.MetadataAdapter;
import org.reflections.filters.Filter;
import org.reflections.scanners.Scanner;

import java.net.URL;
import java.util.Collection;

/**
 *
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public interface Configuration {
    Scanner[] getScanners();
    void setScanners(Scanner[] scanners);

    /**
     * urls to be scanned. use ClasspathHelper convenient methods
     */
    void setUrls(Collection<URL> urls);
    Collection<URL> getUrls();

    /**
     * class fully qualified name filter
     */
    void setFilter(Filter<String> filter);
    Filter<String> getFilter();

    MetadataAdapter getMetadataAdapter();
    void setMetadataAdapter(MetadataAdapter metadataAdapter);

}

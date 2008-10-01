package org.reflections;

import org.reflections.actors.Marshaller;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.actors.impl.XmlMarshaller;
import org.reflections.actors.impl.QueryDSL;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.meta.meta.FirstClassElement;

/**
 * A one-stop-shop for all reflections
 *
 * To use it, have Reflections.getInstance().setConfiguration(...) only once in your application, preferably at bootstrap
 * than use the QueryDSL api for various queries
 *
 * @author mamo
 */
//todo: make thread safe
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Reflections implements Marshaller {
    private static Reflections instance;
    private boolean configured;

    protected ClasspathMD classpathMD;

    public static Reflections getInstance() {
        if (instance == null) {
            instance = new Reflections() {
            };
        }

        return instance;
    }

    public Reflections setConfiguration(Configuration configuration) {
        if (configured) {
            throw new RuntimeException("Reflections has already been configured");
        }

        initialize(configuration);
        configured = true;

        return this;
    }

    private void initialize(final Configuration configuration) {
        if (configuration == null) {
            throw new RuntimeException("Configuration was not set");
        }

        classpathMD = new ClasspathMD();

        new ClasspathScanner(configuration, classpathMD).scan();

        configured = true;
    }

    //
    public void save(String destination) {
        new XmlMarshaller(classpathMD).save(destination);
    }

    //
    public <T extends FirstClassElement> QueryDSL.S1<T> select(final Class<T> filter) {
        return new QueryDSL(classpathMD).select(filter);
    }
}
package org.reflections;

import org.reflections.model.Configuration;
import org.reflections.model.ClasspathMD;
import org.reflections.model.ClassMD;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.actors.impl.XmlMarshaller;

import java.util.Collection;

/**
 * @author mamo
 */
//todo: make thread safe
//todo: add benchmark logs
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Reflections {
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

    //todo: don't return string based classMD, if the parameter is Class, the return type should be first class oriented as well
    //todo: don't expose classMD
    //todo: create a good query interface, this queries are only temporary
    public ClassMD getClassMD(Class<?> aClass) {
        return classpathMD.getClassMD(aClass.getName());
    }

    public Collection<String> get(Class<?> aClass) {
        return classpathMD.getInvertedMD(aClass.getName());
    }

    public void save(String destination) {
        new XmlMarshaller(classpathMD).save(destination);
    }
}
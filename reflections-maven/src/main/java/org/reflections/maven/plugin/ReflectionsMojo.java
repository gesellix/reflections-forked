package org.reflections.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;
import org.jfrog.jade.plugins.common.injectable.MvnInjectableMojoSupport;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.reflections.Reflections;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author mamo
 */
@MojoGoal("reflections")
public class ReflectionsMojo extends MvnInjectableMojoSupport {

    @MojoParameter
    private String elementTypesToScan;
    @MojoParameter
    private boolean computeTransitiveClosure;
    @MojoParameter
    private String invertedElementTypes;
    @MojoParameter
    private String destinations;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //
        if (StringUtils.isEmpty(destinations)) {
            getLog().warn("no destination was specified. nothing to do, than...");
            return;
        }

        if (StringUtils.isEmpty(destinations)) {
            getLog().error("Reflections plugin is skipping because it should have been configured with a non empty destinations parameter");
            return;
        }

        String outputDirectory = getProject().getBuild().getOutputDirectory();
        if (!new File(outputDirectory).exists()) {
            getLog().warn(String.format("Reflections plugin is skipping because %s was not found", outputDirectory));
            return;
        }

        //
        Reflections reflections = new Reflections() {}
                .setConfiguration(Configuration
                        .build()
                        .addUrls(getOutputDirUrl())
                        .addElementTypesToScan(parseElementTypesToScan())
                        .setComputeTransitiveClosure(computeTransitiveClosure)
                        .addInvertedElementTypes(parseInvertedElementTypesToScan()));

        for (String destination : destinations.split(",")) {
            reflections.save(destination.trim());
        }
    }

    private ElementTypes[] parseElementTypesToScan() throws MojoExecutionException {
        return parseElementTypes(elementTypesToScan);
    }

    private ElementTypes[] parseInvertedElementTypesToScan() throws MojoExecutionException {
        return parseElementTypes(invertedElementTypes);
    }

    private ElementTypes[] parseElementTypes(final String elementTypesParameter) {
        String[] types = elementTypesParameter.split(",");
        ElementTypes[] elementTypes = new ElementTypes[types.length];
        for (int i = 0; i < elementTypes.length; i++) {
            elementTypes[i] = ElementTypes.valueOf(types[i]);
        }
        return elementTypes;
    }

    private URL getOutputDirUrl() throws MojoExecutionException {
        try {
            File outputDirectoryFile = new File(getProject().getBuild().getOutputDirectory() + "/");
            return outputDirectoryFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}

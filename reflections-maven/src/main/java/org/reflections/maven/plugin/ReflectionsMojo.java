package org.reflections.maven.plugin;

import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;
import org.jfrog.jade.plugins.common.injectable.MvnInjectableMojoSupport;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;
import org.reflections.Reflections;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A Maven Mojo that
 *
 * @author mamo
 */
@MojoGoal("reflections")
@MojoPhase("process-classes")
public class ReflectionsMojo extends MvnInjectableMojoSupport {

    @MojoParameter(description = "a comma separated list of org.reflections.model.ElementTypes to scan for each class"
            , defaultValue = "annotations")
    private String elementTypesToScan;

    @MojoParameter(description = "a comma separated list of org.reflections.model.ElementTypes to compute inverted indices for"
            , defaultValue = "annotations")
    private String invertedElementTypes;

    @MojoParameter(description = "should compute transitive closure for classes metadata")
    private boolean computeTransitiveClosure;

    @MojoParameter(description = "a comma separated list of destinations to save metadata to"
            , defaultValue = "${project.build.outputDirectory}/reflections/${project.artifactId}-reflections.xml")
    private String destinations;

    @MojoParameter(description = "a comma separated list of regex that will be used from classes and metadata exclusions"
            , defaultValue = "java\\. , javax\\. , sun\\. , com.sun\\.")
    private String excludePatternStrings;

    @MojoParameter(description = "a comma separated list of regex that will be used from classes and metadata inclusions")
    private String includePatternStrings;

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
        Reflections reflections = new Reflections() {
        }
                .setConfiguration(Configuration
                        .build()
                        .addUrls(getOutputDirUrl())
                        .addElementTypesToScan(parseElementTypesToScan())
                        .setComputeTransitiveClosure(computeTransitiveClosure)
                        .addInvertedElementTypes(parseInvertedElementTypesToScan())
                        .addExcludePatterns(parseExcludePatterns())
                        .addIncludePatterns(parseIncludePatterns())
                );

        for (String destination : parseDestinations()) {
            reflections.save(destination.trim());
        }
    }

    private Pattern[] parseExcludePatterns() {
        return parsePatterns(excludePatternStrings);
    }

    private Pattern[] parseIncludePatterns() {
        return parsePatterns(includePatternStrings);
    }

    private Pattern[] parsePatterns(final String strings) {
        Set<Pattern> patterns = Sets.newHashSet();

        if (StringUtils.isNotEmpty(strings)) {
            final String[] patternStrings = strings.split(",");
            for (String patternString : patternStrings) {
                patterns.add(ClasspathHelper.getPatternForFqnPrefix(patternString));
            }
        }
        
        return patterns.toArray(new Pattern[patterns.size()]);
    }

    private String[] parseDestinations() {
        return destinations.split(",");
    }

    private ElementTypes[] parseElementTypesToScan() throws MojoExecutionException {
        return parseElementTypes(elementTypesToScan);
    }

    private ElementTypes[] parseInvertedElementTypesToScan() throws MojoExecutionException {
        return parseElementTypes(invertedElementTypes);
    }

    private ElementTypes[] parseElementTypes(final String elementTypesParameter) {
        if (StringUtils.isEmpty(elementTypesParameter)) {return null;}

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

package org.reflections.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;
import org.jfrog.jade.plugins.common.injectable.MvnInjectableMojoSupport;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;
import org.reflections.Reflections;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.filters.*;
import org.reflections.scanners.ClassAnnotationsScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.AbstractConfiguration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 *
 */
@MojoGoal("reflections")
@MojoPhase("process-classes")
public class ReflectionsMojo extends MvnInjectableMojoSupport {

    @MojoParameter(description = "a comma separated list of scanner classes"
            , defaultValue = ClassAnnotationsScanner.indexName+","+ SubTypesScanner.indexName)
    private String scanners;

    @MojoParameter(description = "a comma separated list of include exclude filters"
            , defaultValue = "-java., -javax., -sun., -com.sun.")
    private String includeExclude;

    @MojoParameter(description = "a comma separated list of destinations to save metadata to"
            , defaultValue = "${project.build.outputDirectory}/META-INF/reflections/${project.artifactId}-reflections.xml")
    private String destinations;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //
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
        Reflections reflections = new Reflections(
                new AbstractConfiguration() {
                    {
                        setUrls(Arrays.asList(parseOutputDirUrl()));
						setScanners(parseScanners());
                        setMetadataAdapter(new JavassistAdapter());
						applyUniversalFilter(parseFilters());
                    }
                });

        for (String destination : parseDestinations()) {
            reflections.save(destination.trim());
        }
    }

    private Filter<String> parseFilters() throws MojoExecutionException {
        List<IncludeExcludeFilter<String>> filters = new ArrayList<IncludeExcludeFilter<String>>();

        if (StringUtils.isNotEmpty(includeExclude)) {
            for (String string : includeExclude.split(",")) {
                String trimmed = string.trim();
                char prefix = trimmed.charAt(0);
                String pattern = trimmed.substring(1);

                IncludeExcludeFilter<String> filter;
                switch (prefix) {
                    case '+':
                        filter = new IncludePrefix(pattern);
                        break;
                    case '-':
                        filter = new ExcludePrefix(pattern);
                        break;
                    default:
                        throw new MojoExecutionException("includeExclude should start with either + or -");
                }

                filters.add(filter);
            }

            return new IncludeExcludeChain<String>(filters);
        } else {
            return new Any<String>();
        }
    }

    private Scanner[] parseScanners() throws MojoExecutionException {
        Set<Scanner> scannersSet = new HashSet<Scanner>(0);

        if (StringUtils.isNotEmpty(scanners)) {
            String[] scannerClasses = scanners.split(",");
            for (String scannerClass : scannerClasses) {
                String trimmed = scannerClass.trim();
                String className = "org.reflections.scanners." + trimmed + "Scanner";

                try {
                    Scanner scanner = (Scanner) Class.forName(className).newInstance();
                    scannersSet.add(scanner);
                } catch (Exception e) {
                    throw new MojoExecutionException(String.format("could not find scanner %s [%s]",trimmed,scannerClass), e);
                }
            }
        }

        return scannersSet.toArray(new Scanner[scannersSet.size()]);
    }

    private String[] parseDestinations() {
        return destinations.split(",");
    }

    private URL parseOutputDirUrl() throws MojoExecutionException {
        try {
            File outputDirectoryFile = new File(getProject().getBuild().getOutputDirectory() + "/");
            return outputDirectoryFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}

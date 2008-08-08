/**
 * @author mamo
 */
package org.reflections.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.actors.impl.XmlMarshaller;
import org.reflections.actors.impl.PostCompiledResourcesCollector;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.ConfigurationBuilder;
import static org.reflections.model.ConfigurationBuilder.*;
import static org.reflections.model.ConfigurationBuilder.*;
import static org.reflections.model.ConfigurationBuilder.*;
import org.reflections.helper.ClasspathHelper;

import java.io.File;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;

/**
 *
 */
public class MarshallerTest {

    private final static File testResourcesDir =
            new File(System.getProperty("user.dir") + "/reflections/src/test/java/reflections");

    @BeforeClass
    public static void prepareResourcesDir() {
        testResourcesDir.delete();
        testResourcesDir.mkdirs();
    }

    @Test
    public void createTestModelResourcesForScannerScansAnnotationsCorrectly() {
        createTestModelResourceForClass(ScannerScansAnnotationsCorrectly.class);

        Assert.assertTrue(new File(testResourcesDir,ScannerScansAnnotationsCorrectly.class.getSimpleName()+".xml").exists());
    }

    @Test
    public void createTestModelResourcesForInvertedMDComputedCorrectly() {
        createTestModelResourceForClass(InvertedMDComputedCorrectly.class);

        Assert.assertTrue(new File(testResourcesDir,InvertedMDComputedCorrectly.class.getSimpleName()+".xml").exists());
    }

    @Test
    public void loadTestModelResourcesIsCorrect() {
        ClasspathMD classpathMD = new ClasspathMD();

        final XmlMarshaller xmlMarshaller = new XmlMarshaller(classpathMD);

        Assert.assertTrue(classpathMD.getClassesKeys().size()==0);

        xmlMarshaller.load("reflections/"+ScannerScansAnnotationsCorrectly.class.getSimpleName()+".xml");

        final int size1 = classpathMD.getClassesKeys().size();
        Assert.assertTrue(size1 > 0);

        xmlMarshaller.load("reflections/"+InvertedMDComputedCorrectly.class.getSimpleName()+".xml");

        final int size2 = classpathMD.getClassesKeys().size();
        Assert.assertTrue(size2 > size1);
    }

    private void createTestModelResourceForClass(final Class<?> testModelClass) {
        ClasspathMD classpathMD = new ClasspathMD();

        new ClasspathScanner(
                Configuration.build
                        (Annotations, Transitive, ExcludeAll)
                        .addUrls(ClasspathHelper.getUrlForClass(testModelClass))
                        .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(testModelClass)),
                classpathMD).scan();

        new XmlMarshaller(classpathMD).save(testResourcesDir+"/"+ testModelClass.getSimpleName()+".xml");
    }
}
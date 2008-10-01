/**
 * @author mamo
 */
package org.reflections.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.actors.impl.XmlMarshaller;
import org.reflections.helper.ClasspathHelper;
import static org.reflections.helper.DescriptorHelper.className;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.*;

import java.io.File;

/**
 * @author mamo
 */
public class XmlMarshallerTest {

    private final static File testResourcesDir = new File(System.getProperty("user.dir") + "/META-INF/reflections/target/classes");
    private static final String resources_prefix = "";

    @BeforeClass
    public static void prepareResourcesDir() {
        testResourcesDir.delete();
        testResourcesDir.mkdirs();
    }

    @Test
    public void createTestModelResourcesForScannerScansAnnotationsCorrectly() {
        createTestModelResourceForClass(ScannerScansAnnotationsCorrectly.class);

        Assert.assertTrue(
                new File(testResourcesDir,ScannerScansAnnotationsCorrectly.class.getSimpleName()+".xml").exists());
    }

    @Test
    public void createTestModelResourcesForInvertedMDComputedCorrectly() {
        createTestModelResourceForClass(ReverseMDComputedCorrectly.class);

        Assert.assertTrue(
                new File(testResourcesDir, ReverseMDComputedCorrectly.class.getSimpleName()+".xml").exists());
    }

    @Test
    public void mergeTestModelMetaDataIsCorrect() {
        ClasspathMD classpathMD = new ClasspathMD();

        final XmlMarshaller xmlMarshaller = new XmlMarshaller(classpathMD);

        xmlMarshaller.load(resources_prefix +ScannerScansAnnotationsCorrectly.class.getSimpleName()+".xml");

        Assert.assertNotNull(classpathMD.getType(className(ScannerScansAnnotationsCorrectly.TestModel.C1.class)));
        Assert.assertNull(classpathMD.getType(className(ReverseMDComputedCorrectly.TestModel.C1.class)));

        Assert.assertNotNull(classpathMD.getReverseFirstClassElements(className(ScannerScansAnnotationsCorrectly.TestModel.AI1.class)));
        Assert.assertNull(classpathMD.getReverseFirstClassElements(className(ReverseMDComputedCorrectly.TestModel.AI1.class)));

        xmlMarshaller.load(resources_prefix + ReverseMDComputedCorrectly.class.getSimpleName()+".xml");

        Assert.assertNotNull(classpathMD.getType(ReverseMDComputedCorrectly.TestModel.C1.class.getName()));
        Assert.assertNotNull(classpathMD.getReverseFirstClassElements(className(ReverseMDComputedCorrectly.TestModel.AI1.class)));
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
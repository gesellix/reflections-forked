package org.reflections.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.helper.ClasspathHelper;
import static org.reflections.helper.DescriptorHelper.className;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.*;
import static org.reflections.model.ElementTypes.*;
import org.reflections.model.meta.MetaClass;
import org.reflections.model.meta.MetaConstructor;
import org.reflections.model.meta.MetaField;
import org.reflections.model.meta.MetaMethod;
import org.reflections.model.meta.meta.FirstClassElement;
import static org.reflections.tests.ScannerScansFieldsAndMethodsCorrectly.TestModel.C1;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * @author mamo
 */
public class ScannerScansFieldsAndMethodsCorrectly {
    private static ClasspathMD classpathMD = new ClasspathMD();

    @BeforeClass
    public static void scanTestModel() {
        new ClasspathScanner(
                Configuration.build
                        (Transitive, ExcludeAll, ThisUrl)
                        .addElementTypesToScan(annotations,fields, methods)
                        .addReverseElementTypes(annotations, fields, methods)
                        .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(ScannerScansFieldsAndMethodsCorrectly.TestModel.class))
                , classpathMD).scan();
    }

    @SuppressWarnings({"ALL"})
    //kindly leave it unformatted single lined
    public interface TestModel {
        public @Retention(RUNTIME) @Target(FIELD) @interface AF1 {}
        public @Retention(RUNTIME) @Target(METHOD) @interface AM1 {}
        public @Retention(RUNTIME) @Target(PARAMETER) @interface AMP1 {}

        public class C1 {
            @AF1 private String field1;
            @AM1 public void setField1(@AMP1 String field1) {this.field1=field1;}
        }

        public class C2 {
            private byte[][] byteMatrix;
            private String[] stringArray;

            protected C2() {}
            protected void C2() {}
            public Byte[][] getByteMatrix(Object someObject, Object[] someArray) {return null;}
        }
    }

    @Test
    public void descriptorScanIsCorrect() {
        final MetaClass c2 = (MetaClass) classpathMD.getType(className(TestModel.C2.class));

        Assert.assertEquals(c2.getField("byteMatrix").getType(), className(byte[][].class));
        Assert.assertEquals(c2.getField("stringArray").getType(), className(String[].class));

        final MetaMethod getByteMatrix = c2.getMethod("getByteMatrix");
        Assert.assertEquals(getByteMatrix.getType(), className(Byte[][].class));

        final MetaField param1 = getByteMatrix.getParameter(0);
        Assert.assertEquals(param1.getType(), className(Object.class));
        Assert.assertNull(param1.getName()); //what a pity

        final MetaField param2 = getByteMatrix.getParameter(1);
        Assert.assertEquals(param2.getType(), className(Object[].class));
        Assert.assertNull(param1.getName()); //what a pity
    }

    @Test
    public void constructorScanIsRight() {
        final MetaClass c2 = (MetaClass) classpathMD.getType(className(TestModel.C2.class));

        final MetaConstructor constructor = c2.getConstructors().get(0);
        Assert.assertEquals(constructor.getType(), className(TestModel.C2.class));
    }

    @Test
    public void scanFieldIsCorrect() {
        final FirstClassElement c1 = classpathMD.getType(className(C1.class));

        final MetaField field1 = ((MetaClass) c1).getField("field1");
        Assert.assertEquals(field1.getType(), className(String.class));
        Assert.assertEquals(field1.getAnnotations().size(),1);
        Assert.assertNotNull(field1.getAnnotation(className(TestModel.AF1.class)));
    }

    @Test
    public void scanMethodIsCorrect() {
        final MetaClass c1 = (MetaClass) classpathMD.getType(className(C1.class));

        //setField1

        final MetaMethod setField1 = c1.getMethod("setField1");
        Assert.assertEquals(setField1.getType(),"void");
        Assert.assertEquals(setField1.getAnnotations().size(),1);
        Assert.assertNotNull(setField1.getAnnotation(className(TestModel.AM1.class)));

        Assert.assertEquals(setField1.getParameters().size(),1);
        final MetaField param1 = setField1.getParameter(0);
        Assert.assertEquals(param1.getType(), className(String.class));
        Assert.assertEquals(param1.getAnnotations().size(),1);
        Assert.assertNotNull(param1.getAnnotation(className(TestModel.AMP1.class)));
        Assert.assertNull(param1.getName()); //what a pity

        final MetaConstructor init = c1.getConstructors().get(0);
        Assert.assertEquals(init.getType(),className(C1.class));
        Assert.assertEquals(init.getParameters().size(),0);
        Assert.assertEquals(init.getAnnotations().size(),0);
    }
}

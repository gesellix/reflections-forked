package org.reflections.tests;

import com.google.common.base.Nullable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.ExcludeAll;
import static org.reflections.model.ConfigurationBuilder.Transitive;

import java.util.Set;

/**
 * @author mamo
 */
public class IncludeExceludeIsCorrect {

    @Test
    public void includeExcludeIsCorrect() {
        final ClasspathMD classpathMD = new ClasspathMD();

        new ClasspathScanner(
                Configuration.build
                        (Transitive, ExcludeAll)
                        .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(ScannerScansAnnotationsCorrectly.class)) //include this only
                , classpathMD)
                .scan();

        final Set<String> classesKeys = classpathMD.getClassesKeys();

        final boolean all = Iterables.all(classesKeys, new Predicate<String>() {
            public boolean apply(@Nullable String name) {
                return name.startsWith(ScannerScansAnnotationsCorrectly.class.getName());
            }
        });

        Assert.assertTrue(all);
    }

}

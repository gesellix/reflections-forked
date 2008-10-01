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
import org.reflections.model.meta.meta.FirstClassElement;

import java.util.Collection;

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

        final Collection<FirstClassElement> classesKeys = classpathMD.getTypes();

        final boolean all = Iterables.all(classesKeys, new Predicate<FirstClassElement>() {
            public boolean apply(@Nullable FirstClassElement basicType) {
                return basicType.getType().startsWith(ScannerScansAnnotationsCorrectly.class.getName());
            }
        });

        Assert.assertTrue(all);
    }

}

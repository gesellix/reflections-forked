package org.reflections.scanners;

import jsr166y.forkjoin.ParallelArray;
import jsr166y.forkjoin.Ops;
import jsr166y.forkjoin.ForkJoinPool;

import java.util.List;

import static com.google.common.collect.Iterables.newArray;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class ClassAnnotationsScanner extends AbstractScanner {
    public static final String indexName = "ClassAnnotations";

	private final ForkJoinPool forkJoinPool = new ForkJoinPool(16);

    public void scan(final Object cls) {
		final String className = getMetadataAdapter().getClassName(cls);
		List<String> annotationTypes = getMetadataAdapter().getClassAnnotationNames(cls);
		ParallelArray.createFromCopy(newArray(annotationTypes, String.class), forkJoinPool).apply(new Ops.Procedure<String>() {
			public void apply(String annotationType) {
				if (accept(annotationType)) {
					populate(annotationType, className);
				}
			}
		});
    }

    public String getIndexName() {
        return indexName;
    }
}

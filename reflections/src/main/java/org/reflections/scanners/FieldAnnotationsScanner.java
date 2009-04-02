package org.reflections.scanners;

import static com.google.common.collect.Iterables.newArray;
import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.Ops;
import jsr166y.forkjoin.ParallelArray;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class FieldAnnotationsScanner extends AbstractScanner {

	public static final String indexName = "FieldAnnotations";

	private final ForkJoinPool forkJoinPool = new ForkJoinPool(16);

	public void scan(final Object cls) {
		final String className = getMetadataAdapter().getClassName(cls);
		List<Object> fields = getMetadataAdapter().getFields(cls);
		for (final Object field : fields) {
			List<String> fieldAnnotations = getMetadataAdapter().getFieldAnnotationNames(field);
			ParallelArray.createFromCopy(newArray(fieldAnnotations, String.class), forkJoinPool).apply(new Ops.Procedure<String>() {
				public void apply(String fieldAnnotation) {
					if (accept(fieldAnnotation)) {
						String fieldName = getMetadataAdapter().getFieldName(field);
						populate(fieldAnnotation, String.format("%s.%s", className, fieldName));
					}
				}
			});
		}
	}

	public String getIndexName() {
		return "FieldAnnotations";
	}
}

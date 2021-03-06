package org.jboss.errai.common.metadata;

import javassist.bytecode.ClassFile;
import org.jboss.errai.common.rebind.CacheUtil;
import org.jboss.errai.reflections.adapters.MetadataAdapter;
import org.jboss.errai.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Inherited;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Mike Brock
 */
public class ExtendedTypeAnnotationScanner extends TypeAnnotationsScanner {
  @Override
  public void scan(final Object cls) {
    @SuppressWarnings("unchecked") final
    MetadataAdapter adapter = getMetadataAdapter();

    final String className = adapter.getClassName(cls);

    // noinspection unchecked
    for (final String annotationType : (List<String>) adapter.getClassAnnotationNames(cls)) {
      if (acceptResult(annotationType) ||
          annotationType.equals(Inherited.class.getName())) { // as an exception, accept
        // Inherited as well
        getStore().put(annotationType, className);

        if (cls instanceof ClassFile) {
          Set<SortableClassFileWrapper> classes = CacheUtil.getCache(MetaDataScanner.CacheHolder.class).ANNOTATIONS_TO_CLASS.get(annotationType);
          if (classes == null) {
            CacheUtil.getCache(MetaDataScanner.CacheHolder.class).ANNOTATIONS_TO_CLASS.put(annotationType, classes =
                Collections.synchronizedSet(new TreeSet<SortableClassFileWrapper>()));
          }
          classes.add(new SortableClassFileWrapper(className, (ClassFile) cls));
        }
      }
    }
  }
}

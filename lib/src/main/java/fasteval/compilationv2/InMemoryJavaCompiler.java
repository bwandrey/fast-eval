package fasteval.compilationv2;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryJavaCompiler {

    private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
    private static final Map<String, byte[]> compiledClasses = new ConcurrentHashMap<>();

    public static Class<?> compile(String className, String sourceCode) throws ClassNotFoundException {
        JavaFileObject javaFile = new JavaSourceFromString(className, sourceCode);

        JavaFileManager fileManager = new ForwardingJavaFileManager<>(COMPILER.getStandardFileManager(null, null, null)) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) {
                return new SimpleJavaFileObject(URI.create("mem:///" + name.replace('.', '/') + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return new ByteArrayOutputStream() {
                            @Override
                            public void close() {
                                compiledClasses.put(name, toByteArray());
                            }
                        };
                    }
                };
            }
        };

        JavaCompiler.CompilationTask task = COMPILER.getTask(
                null,
                fileManager,
                null,
                null,
                null,
                Collections.singletonList(javaFile)
        );

        if (!task.call()) {
            throw new RuntimeException("Compilation failed for class: " + className);
        }

        return loadClass(className);
    }

    private static Class<?> loadClass(String className) throws ClassNotFoundException {
        return new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) {
                byte[] bytes = compiledClasses.get(name);
                if (bytes == null) throw new RuntimeException("Class not found: " + name);
                return defineClass(name, bytes, 0, bytes.length);
            }
        }.loadClass(className);
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}

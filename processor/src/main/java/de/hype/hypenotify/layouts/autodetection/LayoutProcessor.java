package de.hype.hypenotify.layouts.autodetection;

import com.google.auto.service.AutoService;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_18)
@SupportedAnnotationTypes("de.hype.hypenotify.layouts.autodetection.Layout")
public class LayoutProcessor extends AbstractProcessor {

    private boolean fileGenerated = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (fileGenerated) {
            return true;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("package de.hype.hypenotify.layouts.autodetection;\n\n");
        builder.append("import java.util.HashMap;\n");
        builder.append("import java.util.Map;\n");
        builder.append("import de.hype.hypenotify.layouts.autodetection.Layout;\n\n");
        builder.append("public class LayoutRegistry {\n");
        builder.append("    private static final Map<String, Class<?>> layouts = new HashMap<>();\n\n");
        builder.append("    static {\n");

        for (Element element : roundEnv.getElementsAnnotatedWith(Layout.class)) {
            String className = ((TypeElement) element).getQualifiedName().toString();
            Layout layout = element.getAnnotation(Layout.class);
            builder.append("        layouts.put(\"")
                    .append(layout.name())
                    .append("\", ")
                    .append(className)
                    .append(".class);\n");
        }

        builder.append("    }\n\n");
        builder.append("    public static Class<?> getLayout(String name) {\n");
        builder.append("        return layouts.get(name);\n");
        builder.append("    }\n\n");
        builder.append("    public static Map<String, Class<?>> getAllLayouts() {\n");
        builder.append("        return layouts;\n");
        builder.append("    }\n");
        builder.append("}\n");

        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile("de.hype.hypenotify.layouts.autodetection.LayoutRegistry");
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(builder.toString());
            }
            fileGenerated = true;
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
        }

        return true;
    }
}
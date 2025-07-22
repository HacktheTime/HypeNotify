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
        builder.append("""
                package de.hype.hypenotify.layouts.autodetection;
                
                import java.util.HashMap;
                import java.util.Map;
                import de.hype.hypenotify.layouts.autodetection.Layout;
                import de.hype.hypenotify.app.screen.Screen;
                
                public class LayoutRegistry {
                    private static final Map<String, Class<? extends Screen>> layouts = new HashMap<>();
                
                    static {
                """);

        for (Element element : roundEnv.getElementsAnnotatedWith(Layout.class)) {
            String className = ((TypeElement) element).getQualifiedName().toString();
            Layout layout = element.getAnnotation(Layout.class);
            builder.append("        layouts.put(\"")
                    .append(layout.name())
                    .append("\", ")
                    .append(className)
                    .append(".class);\n");
        }

        builder.append("""
                    }
                
                    public static Class<? extends Screen> getLayout(String name) {
                        return layouts.get(name);
                    }
                
                    public static Map<String, Class<? extends Screen>> getAllLayouts() {
                        return layouts;
                    }
                }
                """);

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
package com.timewastingguru.realmkit.processor;

import com.google.auto.service.AutoService;
import com.timewastingguru.realmkit.annotation.RealmKitObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by artoymtkachenko on 14.05.15.
 */
@AutoService(Processor.class)
public class RalmKitAnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(RealmKitObject.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element e : roundEnv.getElementsAnnotatedWith(RealmKitObject.class)) {
            try {
                messager.printMessage(Diagnostic.Kind.NOTE, "Ololololo");
                RealmKitObjectGenerator generator = new RealmKitObjectGenerator(e);
                generator.writeTo(filer);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


//
//        for (Element e : roundEnv.getElementsAnnotatedWith(Connect.class)) {
//            Connect ca = e.getAnnotation(Connect.class);
//            String name = e.getSimpleName().toString();
//            char[] c = name.toCharArray();
//            c[0] = Character.toUpperCase(c[0]);
//            name = new String(name);
//            TypeElement clazz = (TypeElement) e.getEnclosingElement();
//            try {
//                JavaFileObject f = processingEnv.getFiler().
//                        createSourceFile(clazz.getQualifiedName() + "Autogenerate");
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
//                        "Creating " + f.toUri());
//                Writer w = f.openWriter();
//                try {
//                    String pack = clazz.getQualifiedName().toString();
//                    PrintWriter pw = new PrintWriter(w);
//                    pw.println("package "
//                            + pack.substring(0, pack.lastIndexOf('.')) + ";");
//                    pw.println("\npublic class "
//                            + clazz.getSimpleName() + "Autogenerate {");
//
//                    TypeMirror type = e.asType();
//
//                    pw.println("\n    public " + ca.field() + " result = \"" + ca.field() + "\";");
//
//                    pw.println("    public int type = " + ca.field() + ";");
//
//
//                    pw.println("\n    protected " + clazz.getSimpleName()
//                            + "Autogenerate() {}");
//                    pw.println("\n    /** Handle something. */");
//                    pw.println("    protected final void handle" + name
//                            + "(" + ca.field() + " value" + ") {");
//                    pw.println("\n//" + e);
//                    pw.println("//" + ca);
//                    pw.println("\n        System.out.println(value);");
//                    pw.println("    }");
//                    pw.println("}");
//                    pw.flush();
//                } finally {
//                    w.close();
//                }
//            } catch (IOException x) {
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                        x.toString());
//            }
//        }

        return true;
    }

}

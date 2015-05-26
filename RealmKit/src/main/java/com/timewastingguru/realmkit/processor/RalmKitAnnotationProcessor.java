package com.timewastingguru.realmkit.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.timewastingguru.realmkit.annotation.RealmKitObject;
import io.realm.annotations.RealmClass;
import io.realm.processor.ClassMetaData;
import io.realm.processor.RealmProxyClassGenerator;
import io.realm.processor.RealmVersionChecker;
import io.realm.processor.Utils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by artoymtkachenko on 14.05.15.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.realm.annotations.RealmClass",
})

public class RalmKitAnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private TypeMirror realmObject;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();


    }

//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        Set<String> annotataions = new LinkedHashSet<String>();
//        annotataions.add(RealmKitObject.class.getCanonicalName());
//        return annotataions;
//    }
//
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        RealmVersionChecker updateChecker = RealmVersionChecker.getInstance(processingEnv);
        updateChecker.executeRealmVersionUpdate();
        Utils.initialize(processingEnv);

        realmObject = elementUtils.getTypeElement("io.realm.RealmObject").asType();

        TypeSpec.Builder realmKit = TypeSpec.classBuilder("RealmKit")
                .addModifiers(Modifier.PUBLIC);

        ClassName realmType = ClassName.get("io.realm", "Realm");

        realmKit.addField(realmType, "realm", Modifier.PROTECTED, Modifier.FINAL);


        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmKitObject.class)) {

            // Check the annotation was applied to a Class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmClass annotation can only be applied to classes", classElement);
            }
            ClassMetaData metadata = new ClassMetaData(processingEnv, (TypeElement) classElement);
            if (!metadata.isModelClass()) {
                continue;
            }
            boolean success = metadata.generateMetaData(processingEnv.getMessager());


            ClassName obj = ClassName.get(metadata.getFullyQualifiedClassName().replace("."+metadata.getSimpleClassName(), ""), metadata.getSimpleClassName());
            Name primaryKey = metadata.getPrimaryKey().getSimpleName();



            MethodSpec updateMethod = generateCreateOrUpdateMethod(metadata, obj, primaryKey);
            MethodSpec deleteMethod = genereateDeleteMethod(metadata, obj, primaryKey);

            realmKit.addMethod(updateMethod);
            realmKit.addMethod(deleteMethod);

        }

        MethodSpec beginTransactionMethod = MethodSpec.methodBuilder("beginTransaction")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("realm.beginTransaction()")
                .build();

        MethodSpec commitTransactionMethod = MethodSpec.methodBuilder("commitTransaction")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("realm.commitTransaction()")
                .build();

        MethodSpec constructor = generateConstructor();
        realmKit.addMethod(constructor);
        realmKit.addMethod(beginTransactionMethod);
        realmKit.addMethod(commitTransactionMethod);


        JavaFile javaFile = JavaFile.builder("com.timewastingguru.customannotations", realmKit.build())
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private MethodSpec generateConstructor() {
        ClassName context = ClassName.get("android.content","Context");
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(context, "context")
                .addStatement("realm = Realm.getInstance(context)")
                .build();
    }

    private MethodSpec genereateDeleteMethod(ClassMetaData metadata, ClassName obj, Name primaryKey) {
        String simpleClassName = metadata.getSimpleClassName();
        MethodSpec.Builder delete = MethodSpec.methodBuilder("delete" + simpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(obj, simpleClassName.toLowerCase())
                .addParameter(boolean.class, "cascade")
                .addStatement("$L local = realm.where($L.class).equalTo($S, $L.$L()).findFirst()",
                        simpleClassName, simpleClassName, primaryKey, simpleClassName.toLowerCase(), metadata.getPrimaryKeyGetter())
                .beginControlFlow("if (local != null)");

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String getter = metadata.getGetter(fieldName);
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                delete.beginControlFlow("if (cascade == true)");
                delete.addStatement("delete$L($L.$L(), $L)", getFieldType(field), simpleClassName.toLowerCase(), getter, true);
                delete.endControlFlow();

            }
        }
        delete.addStatement("local.removeFromRealm()")
        .endControlFlow();
        return delete.build();
    }

    private MethodSpec generateCreateOrUpdateMethod(ClassMetaData metadata, ClassName obj, Name primaryKey) {
        String simpleClassName = metadata.getSimpleClassName();
        MethodSpec.Builder createOrUpdate = MethodSpec.methodBuilder("updateOrCreate" + simpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .returns(obj)
                .addParameter(obj, simpleClassName.toLowerCase())
                .addStatement("$L local = realm.where($L.class).equalTo($S, $L.$L()).findFirst()",
                        simpleClassName, simpleClassName, primaryKey, simpleClassName.toLowerCase(), metadata.getPrimaryKeyGetter())
                .beginControlFlow("if (local == null)")
                .addStatement("local = realm.createObject($L.class)", simpleClassName)
                .endControlFlow();

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String setter = metadata.getSetter(fieldName);
            String getter = metadata.getGetter(fieldName);

            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                createOrUpdate.addStatement("local.$L(updateOrCreate$L($L.$L()))", setter, getFieldType(field), simpleClassName.toLowerCase(), getter);
            } else {
                createOrUpdate.addStatement("local.$L($L.$L())", setter, simpleClassName.toLowerCase(), getter);
            }
        }


        createOrUpdate.addStatement("return local");


        return createOrUpdate.build();
    }

    private String getFieldType(VariableElement field) {
        String result = null;

        String full = field.asType().toString();
        int i = full.lastIndexOf(".");

        result = full.substring(i + 1);

        return result;
    }

}

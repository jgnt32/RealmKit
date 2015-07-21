package com.timewastingguru.realmkit.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.timewastingguru.realmkit.annotation.Connect;
import com.timewastingguru.realmkit.annotation.IgnoreField;
import com.timewastingguru.realmkit.annotation.RealmKitObject;
import io.realm.RealmList;
import io.realm.annotations.RealmClass;
import io.realm.processor.ClassMetaData;
import io.realm.processor.RealmProxyClassGenerator;
import io.realm.processor.RealmVersionChecker;
import io.realm.processor.Utils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
        "com.timewastingguru.realmkit.annotation.RealmKitObject",
})

public class RalmKitAnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private TypeMirror realmObject;
    private TypeMirror realmListObject;


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
        realmListObject = elementUtils.getTypeElement("io.realm.RealmList").asType();

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

            boolean success = metadata.generate();

            ClassName obj = ClassName.get(metadata.getFullyQualifiedClassName().replace("."+metadata.getSimpleClassName(), ""), metadata.getSimpleClassName());
            Name primaryKey = metadata.getPrimaryKey().getSimpleName();

            MethodSpec updateMethod = generateCreateOrUpdateMethod(metadata, obj, primaryKey);
            MethodSpec updateCollectionMethod = generateCreateOrUpdateCollectionMethod(metadata, obj, primaryKey);
            MethodSpec deleteMethod = genereateDeleteMethod(metadata, obj, primaryKey);

            realmKit.addMethod(updateMethod);
            realmKit.addMethod(deleteMethod);
            realmKit.addMethod(updateCollectionMethod);
        }

        MethodSpec beginTransactionMethod = generateBeginTransactionMethod();
        MethodSpec commitTransactionMethod = generateCommitTransactionMethod();
        MethodSpec constructor = generateConstructor();

        MethodSpec closeMethod = generateCloseMethod();
        realmKit.addMethod(closeMethod);

        realmKit.addMethod(constructor);
        realmKit.addMethod(beginTransactionMethod);
        realmKit.addMethod(commitTransactionMethod);


        JavaFile javaFile = JavaFile.builder("com.timewastingguru.realmkit", realmKit.build())
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private MethodSpec generateCloseMethod() {
        return MethodSpec.methodBuilder("close")
                .addStatement("realm.close()")
                .build();
    }

    private MethodSpec generateCommitTransactionMethod() {
        return MethodSpec.methodBuilder("commitTransaction")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("realm.commitTransaction()")
                .build();
    }

    private MethodSpec generateBeginTransactionMethod() {
        return MethodSpec.methodBuilder("beginTransaction")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("realm.beginTransaction()")
                .build();
    }

    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("realm = Realm.getDefaultInstance()")
                .build();
    }

    private MethodSpec genereateDeleteMethod(ClassMetaData metadata, ClassName obj, Name primaryKey) {
        String simpleClassName = metadata.getSimpleClassName();
        ClassName deletePredicate = ClassName.get("com.timewastingguru.realmkit", "DeletePredicate");

        MethodSpec.Builder delete = MethodSpec.methodBuilder("delete" + simpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(obj, simpleClassName.toLowerCase())
                .addParameter(boolean.class, "cascade")
                .addParameter(deletePredicate, "deletePredicate")
                .addStatement("$L local = realm.where($L.class).equalTo($S, $L.$L()).findFirst()",
                        simpleClassName, simpleClassName, primaryKey, simpleClassName.toLowerCase(), metadata.getPrimaryKeyGetter())
                .beginControlFlow("if (local != null)");

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String getter = metadata.getGetter(fieldName);
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                delete.beginControlFlow("if (cascade == true)");
                    delete.beginControlFlow("if (deletePredicate == null || deletePredicate.shouldToDelete(local, local.$L()))", getter);
                    delete.addStatement("delete$L($L.$L(), $L, deletePredicate)", getFieldType(field), simpleClassName.toLowerCase(), getter, true);
                    delete.endControlFlow();
                delete.endControlFlow();

            }
        }
        delete.addStatement("local.removeFromRealm()")
        .endControlFlow();
        return delete.build();
    }

    private MethodSpec generateCreateOrUpdateCollectionMethod(ClassMetaData metadata, ClassName obj, Name primaryKey) {

        String simpleClassName = metadata.getSimpleClassName();
        ClassName returnClassName = ClassName.get("io.realm", "RealmList");
        ClassName list = ClassName.get("java.util", "List");

        ParameterizedTypeName result = ParameterizedTypeName.get(returnClassName, obj);
        String toSave = simpleClassName.toLowerCase() + "s";
        MethodSpec.Builder createOrUpdate = MethodSpec.methodBuilder("createOrUpdate" + simpleClassName + "s")
                .addParameter(ParameterizedTypeName.get(list, obj), toSave)
                .returns(result)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("RealmList<$L> result = null", simpleClassName)
                .beginControlFlow("if ($L != null && !$L.isEmpty())", toSave, toSave)
                    .addStatement("result = new RealmList<$L>()", simpleClassName)
                    .beginControlFlow("for (int i = 0; i < $L.size(); i++) ", toSave)
                        .addStatement("result.add(createOrUpdate$L($L.get(i)))", simpleClassName, toSave)
                    .endControlFlow()
                .endControlFlow()

                .addStatement("return result");

        return createOrUpdate.build();
    }

    private MethodSpec generateCreateOrUpdateMethod(ClassMetaData metadata, ClassName obj, Name primaryKey) {
        String simpleClassName = metadata.getSimpleClassName();
        MethodSpec.Builder createOrUpdate = MethodSpec.methodBuilder("createOrUpdate" + simpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .returns(obj)
                .addParameter(obj, simpleClassName.toLowerCase());

        if (metadata.getPrimaryKey() == null) {
            createOrUpdate.addStatement("");
        }


            createOrUpdate.addStatement("$L local = realm.where($L.class).equalTo($S, $L.$L()).findFirst()",
                    simpleClassName, simpleClassName, primaryKey, simpleClassName.toLowerCase(), metadata.getPrimaryKeyGetter())
                .beginControlFlow("if (local == null)")
                .addStatement("local = realm.createObject($L.class)", simpleClassName)
                .endControlFlow();

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String setter = metadata.getSetter(fieldName);
            String getter = metadata.getGetter(fieldName);
            IgnoreField ignore = field.getAnnotation(IgnoreField.class);
            if (ignore == null) {
                if (typeUtils.isAssignable(field.asType(), realmObject)) {

                    Connect annotation = field.getAnnotation(Connect.class);
                    if (annotation != null) {
                        messager.printMessage(Diagnostic.Kind.NOTE, "eto typa connect " + annotation.field());
                    } else {
                        createOrUpdate.beginControlFlow("if ($L.$L() != null)", simpleClassName.toLowerCase(), getter);
                        createOrUpdate.addStatement("local.$L(createOrUpdate$L($L.$L()))", setter, getFieldType(field), simpleClassName.toLowerCase(), getter);
                        createOrUpdate.endControlFlow();
                    }

                } else if (field.asType().toString().contains("io.realm.RealmList")) {
                    createOrUpdate.beginControlFlow("if ($L.$L() != null)", simpleClassName.toLowerCase(), getter);
                    createOrUpdate.addStatement("local.$L(createOrUpdate$Ls($L.$L()))", setter, getFieldType(field), simpleClassName.toLowerCase(), getter);
                    createOrUpdate.endControlFlow();

                } else if (!field.asType().getKind().isPrimitive()) {
                    createOrUpdate.beginControlFlow("if ($L.$L() != null)", simpleClassName.toLowerCase(), getter);
                    createOrUpdate.addStatement("local.$L($L.$L())", setter, simpleClassName.toLowerCase(), getter);
                    createOrUpdate.endControlFlow();
                } else {
                    createOrUpdate.addStatement("local.$L($L.$L())", setter, simpleClassName.toLowerCase(), getter);
                }
            }

        }
        createOrUpdate.addStatement("return local");
        return createOrUpdate.build();
    }

    private String getFieldType(VariableElement field) {
        String result = null;
        String full = field.asType().toString();

        if (!full.contains("io.realm.RealmList")) {
            int i = full.lastIndexOf(".");
            result = full.substring(i + 1);
        } else {
            int i = full.lastIndexOf(".");
            result = full.substring(i + 1, full.length() - 1);
        }



        return result;
    }

}

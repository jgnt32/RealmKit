package com.timewastingguru.realmkit.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.timewastingguru.realmkit.annotation.RealmKitObject;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

public class RealmKitObjectGenerator {

    private JavaFile javaFile;

    public RealmKitObjectGenerator(Element element) {

        String kitName = element.getSimpleName().toString();

        TypeSpec.Builder realmKit = TypeSpec.classBuilder(kitName + "Kit")
                .addModifiers(Modifier.PUBLIC);


        MethodSpec.Builder createOrUpdate = MethodSpec.methodBuilder("createOrUpdate" + kitName)
                .addModifiers(Modifier.PUBLIC)
                .returns(Object.class)
                .addParameter(Object.class, kitName.toLowerCase())
                .addStatement("$T id = (($L) $L).getId()", String.class, kitName, kitName.toLowerCase())
                .addStatement("$L local = realm.where($L.class).equalTo(\"id\", id).findFirst()", kitName, kitName)
                .beginControlFlow("if (local == null)")
                    .addStatement("local = realm.createObject($L.class)", kitName)
                .endControlFlow();





        createOrUpdate.addStatement("return local");





        realmKit.addMethod(createOrUpdate.build());

//        MethodSpec updateMethod = MethodSpec.methodBuilder("update" + kitName)
//                .addModifiers(Modifier.PROTECTED)
//                .returns(void.class)
//                .addParameter(Object.class, "local" + kitName)
//                .addParameter(Object.class, "new" + kitName)
//                .build();
//
//        realmKit.addMethod(updateMethod);


        javaFile = JavaFile.builder("com.timewastingguru.customannotations", realmKit.build())
                .build();
    }

    public void writeTo(Filer filer) throws IOException {
        javaFile.writeTo(filer);

    }

    private boolean isGetter(Method method) {
        if (method.getParameterTypes().length == 0) {
            if (method.getName().startsWith("get") && !method.getReturnType().equals(Void.TYPE)){
                return true;
            }
            if (method.getName().startsWith("is") && method.getReturnType().equals(boolean.class)){
                return true;
            }
        }
        return false;
    }

}
package com.timewastingguru.realmkit.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;

public class RealmKitObjectGenerator {

    private JavaFile javaFile;

    public RealmKitObjectGenerator(Element element) {

        String kitName = element.getSimpleName().toString();


        MethodSpec createOrUpdate = MethodSpec.methodBuilder("createOrUpdate")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                        // .addParameter(type, kitName.toLowerCase())
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet! " + kitName)
                .build();


        TypeSpec realmKit = TypeSpec.classBuilder(kitName + "Kit")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(createOrUpdate)
                .build();


        javaFile = JavaFile.builder("com.timewastingguru.customannotations", realmKit)
                .build();
    }

    public void writeTo(Filer filer) throws IOException {
        javaFile.writeTo(filer);

    }

}
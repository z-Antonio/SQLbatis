package com.antonio.android.sqlbatis.processor

import com.squareup.kotlinpoet.*
import java.io.File

class DatabaseCodeBuilder(
    private val kaptKotlinGeneratedDir: String,
    private val code: CodeBlock
) {
    private val generatedPackage = "com.antonio.android.sqlbatis.impl"
    private val fileName = "DatabaseInitImpl"
    private val annotationInit = ClassName("com.antonio.android.sqlbatis.handle", "DatabaseInit")
    private val annotationHandler = ClassName("com.antonio.android.sqlbatis.handle", "DatabaseHandler")

    fun buildFile() = FileSpec.builder(generatedPackage, fileName)
        .addInitClass()
        .build()
        .writeTo(File(kaptKotlinGeneratedDir))

    private fun FileSpec.Builder.addInitClass() = apply {
        addType(
            TypeSpec.classBuilder(fileName)
            .addSuperinterface(annotationInit)
            .addInitMethod(code)
            .build()
        )
    }

    private fun TypeSpec.Builder.addInitMethod(code: CodeBlock) = apply {
        addFunction(
            FunSpec.builder("initial")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("handler", annotationHandler)
            .returns(UNIT)
            .addCode(code)
            .build()
        )
    }
}
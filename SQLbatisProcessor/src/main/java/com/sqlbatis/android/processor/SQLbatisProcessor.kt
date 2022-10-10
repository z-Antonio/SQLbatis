package com.sqlbatis.android.processor

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import com.sqlbatis.android.annotation.Database
import com.sqlbatis.android.annotation.ColumnName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import java.lang.StringBuilder

@SupportedSourceVersion(SourceVersion.RELEASE_8)
class SQLbatisProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private val tableClass = ClassName("com.sqlbatis.android.handle.TableInfo", "Builder")

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(Database::class.java.canonicalName) // 3


    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (annotations == null || annotations.isEmpty()) return false
        val kaptKotlinGeneratedDir =
            processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false
        val codeBuilder = CodeBlock.Builder()

        roundEnv?.getElementsAnnotatedWith(Database::class.java)
            ?.forEach { element ->
                println("解析 table class ===> $element")
                val annotation = element.getAnnotation(Database::class.java)
                val args = mutableListOf<Any?>().apply {
                    add(annotation.name)
                    add(tableClass)
                    add(element.simpleName)
                }
                parseTableInfo(element, codeBuilder, args)
            }
        DatabaseCodeBuilder(kaptKotlinGeneratedDir, codeBuilder.build()).buildFile()
        return true
    }

    private fun parseTableInfo(
        element: Element,
        codeBuilder: CodeBlock.Builder,
        args: MutableList<Any?>
    ) {
        if (element is TypeElement) {
            val sb = StringBuilder("handler.register(%S, %T(%S)")
            element.enclosedElements.forEach {
                it.getAnnotation(ColumnName::class.java)?.let { c ->
                    sb.append(".addColumn(%S, %L, %L, %L, %S, %S)")
                    args.add(it.simpleName)
                    args.add(c.primaryKey)
                    args.add(c.autoIncr)
                    args.add(c.nonNull)
                    args.add(c.defaultValue)
                    args.add(c.dataType.toString())
                }
            }
            sb.append(".build())")
            codeBuilder.addStatement(sb.toString(), *args.toTypedArray())
        }
    }

}
// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("unused")
package org.jetbrains.kotlin.util

import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import kotlin.reflect.KProperty

fun Annotations.findAnnotation(annotationClass: Class<out Annotation>): AnnotationDescriptor? {
    val fqName = FqName(annotationClass.name)
    return find { it.fqName == fqName }
}

inline fun <reified T : Annotation> Annotations.findAnnotation(): AnnotationDescriptor? = findAnnotation(T::class.java)

fun Annotated.findAnnotation(annotationClass: Class<out Annotation>): AnnotationDescriptor? = annotations.findAnnotation(annotationClass)
inline fun <reified T : Annotation> Annotated.findAnnotation(): AnnotationDescriptor? = findAnnotation(T::class.java)

fun AnnotationDescriptor.getBooleanValue(name: String): Boolean? = argumentValue(name).safeAs<BooleanValue>()?.value
fun AnnotationDescriptor.getStringValue(name: String): String? = argumentValue(name).safeAs<StringValue>()?.value
fun AnnotationDescriptor.getAnnotationValue(name: String): AnnotationDescriptor? = argumentValue(name).safeAs<AnnotationValue>()?.value
fun AnnotationDescriptor.getArrayValue(name: String): List<ConstantValue<*>>? = argumentValue(name).safeAs<ArrayValue>()?.value
fun AnnotationDescriptor.getEnumValue(name: String): EnumValue? = argumentValue(name).safeAs<EnumValue>()

fun AnnotationDescriptor.getBooleanValue(property: KProperty<*>): Boolean? = getBooleanValue(property.name)
fun AnnotationDescriptor.getStringValue(property: KProperty<*>): String? = getStringValue(property.name)
fun AnnotationDescriptor.getAnnotationValue(property: KProperty<*>): AnnotationDescriptor? = getAnnotationValue(property.name)
fun AnnotationDescriptor.getArrayValue(property: KProperty<*>): List<ConstantValue<*>>? = getArrayValue(property.name)
fun AnnotationDescriptor.getEnumValue(property: KProperty<*>): EnumValue? = getEnumValue(property.name)
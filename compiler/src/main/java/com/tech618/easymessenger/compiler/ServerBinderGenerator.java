package com.tech618.easymessenger.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Created by zmy on 2019/2/19.
 * 生成IPC通信的Binder类
 */

public class ServerBinderGenerator
{
    public static TypeSpec generateBinder(TypeElement binderServerTypeElement, List<ExecutableElement> methodElements)
    {
        String generatedClassName = binderServerTypeElement.getSimpleName().toString() + "Binder";
        TypeMirror binderServerTypeMirror = binderServerTypeElement.asType();
        //生成全局属性
        FieldSpec fieldSpecInterfaceImpl = FieldSpec.builder(TypeName.get(binderServerTypeMirror), "mInterfaceImpl", Modifier.PRIVATE).build();
        FieldSpec fieldSpecTransactionCode = FieldSpec.builder(int.class, "TRANSACTION_CODE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                                     .initializer("$T.FIRST_CALL_TRANSACTION + $L", TypeNameHelper.typeNameOfIBinder(), 1)
                                                     .build();
        //生成构造函数
        ParameterSpec parameterSpecInterfaceImpl = ParameterSpec.builder(TypeName.get(binderServerTypeMirror), "interfaceImpl").build();
        MethodSpec methodSpecConstructor = MethodSpec.constructorBuilder()
                                                   .addModifiers(Modifier.PUBLIC)
                                                   .addParameter(parameterSpecInterfaceImpl)
                                                   .addStatement("$N = $N", fieldSpecInterfaceImpl, parameterSpecInterfaceImpl)
                                                   .build();
        //生成Binder类
        TypeSpec.Builder typeImplBuilder = TypeSpec.classBuilder(generatedClassName)
                                                   .addModifiers(Modifier.PUBLIC)
                                                   .superclass(TypeNameHelper.typeNameOfBinder())
                                                   .addField(fieldSpecInterfaceImpl)
                                                   .addField(fieldSpecTransactionCode)
                                                   .addMethod(methodSpecConstructor);
        //生成onTransact方法
        ParameterSpec onTransactMethodCodeParameter = ParameterSpec.builder(int.class, "code").build();
        ParameterSpec onTransactMethodDataParameter = ParameterSpec.builder(TypeNameHelper.typeNameOfParcel(), "data").build();
        ParameterSpec onTransactMethodReplyParameter = ParameterSpec.builder(TypeNameHelper.typeNameOfParcel(), "reply").build();
        ParameterSpec onTransactMethodFlagsParameter = ParameterSpec.builder(int.class, "flags").build();
        MethodSpec.Builder onTransactMethodBuilder = MethodSpec.methodBuilder("onTransact")
                                                             .addModifiers(Modifier.PUBLIC)
                                                             .returns(boolean.class)
                                                             .addAnnotation(Override.class)
                                                             .addException(TypeNameHelper.typeNameOfRemoteException())
                                                             .addParameter(onTransactMethodCodeParameter)
                                                             .addParameter(onTransactMethodDataParameter)
                                                             .addParameter(onTransactMethodReplyParameter)
                                                             .addParameter(onTransactMethodFlagsParameter);
        onTransactMethodBuilder.beginControlFlow("if($N != $N)", onTransactMethodCodeParameter, fieldSpecTransactionCode);
        onTransactMethodBuilder.addStatement("return super.onTransact($N, $N, $N, $N)", onTransactMethodCodeParameter,
                onTransactMethodDataParameter, onTransactMethodReplyParameter, onTransactMethodFlagsParameter);
        onTransactMethodBuilder.endControlFlow();
        onTransactMethodBuilder.addStatement("String methodName = $N.readString()", onTransactMethodDataParameter);
        onTransactMethodBuilder.beginControlFlow("switch($L)", "methodName");
        for (int i = 0; i < methodElements.size(); i++)
        {
            ExecutableElement methodElement = methodElements.get(i);
            onTransactMethodBuilder.beginControlFlow("case $S:", methodElement.getSimpleName().toString());
            List<String> parameterNames = new ArrayList<>(methodElement.getParameters().size());
            boolean isNullFlagDefined = false;
            for (VariableElement parameterElement : methodElement.getParameters())
            {
                readDataBaseOnParameter(onTransactMethodBuilder, parameterElement);
                parameterNames.add(parameterElement.getSimpleName().toString());
            }

            TypeMirror methodReturnTypeMirror = methodElement.getReturnType();
            TypeKind methodReturnType = methodReturnTypeMirror.getKind();
            Name methodName = methodElement.getSimpleName();
            if (methodReturnType == TypeKind.VOID)
            {
                onTransactMethodBuilder.addStatement("$N.$N($L)", fieldSpecInterfaceImpl, methodName,
                        ParameterHelper.getMethodParameterStringByParameterNames(parameterNames));
            }
            else
            {
                onTransactMethodBuilder.addStatement("$T __result = $N.$N($L)", methodReturnTypeMirror,
                        fieldSpecInterfaceImpl, methodName, ParameterHelper.getMethodParameterStringByParameterNames(parameterNames));
            }
            onTransactMethodBuilder.addStatement("$N.writeNoException()", onTransactMethodReplyParameter);
            writeReplyBaseOnParameter(onTransactMethodBuilder, methodReturnTypeMirror);
            onTransactMethodBuilder.addStatement("return true");
            onTransactMethodBuilder.endControlFlow();
        }
        onTransactMethodBuilder.endControlFlow();
        onTransactMethodBuilder.addStatement("return false");
        typeImplBuilder.addMethod(onTransactMethodBuilder.build());
        return typeImplBuilder.build();
    }

    private static void readDataBaseOnParameter(MethodSpec.Builder methodBuilder, VariableElement parameterElement)
    {
        TypeMirror parameterTypeMirror = parameterElement.asType();
        TypeKind parameterTypeKind = parameterTypeMirror.getKind();
        Name parameterName = parameterElement.getSimpleName();
        switch (parameterTypeKind)
        {
            //region 值类型
            case BYTE:
            {
                methodBuilder.addStatement("$T $N = data.readByte()", parameterTypeMirror, parameterName);
                return;
            }
            case CHAR:
            case SHORT:
            case INT:
            {
                methodBuilder.addStatement("$T $N = ($T)data.readInt()", parameterTypeMirror,
                        parameterName, parameterTypeMirror);
                return;
            }
            case BOOLEAN:
            {
                methodBuilder.addStatement("$T $N = data.readInt() > 0", parameterTypeMirror, parameterName);
                return;
            }
            case LONG:
            {
                methodBuilder.addStatement("$T $N = data.readLong()", parameterTypeMirror, parameterName);
                return;
            }
            case FLOAT:
            {
                methodBuilder.addStatement("$T $N = data.readFloat()", parameterTypeMirror, parameterName);
                return;
            }
            case DOUBLE:
            {
                methodBuilder.addStatement("$T $N = data.readDouble()", parameterTypeMirror, parameterName);
                return;
            }
            //endregion

            //region 引用类型
            default:
            {
                //String
                if (TypeMirrorHelper.isString(parameterTypeMirror))
                {
                    methodBuilder.addStatement("$T $N = data.readString()", parameterTypeMirror, parameterName);
                    return;
                }
                //Parcelable
                if (TypeMirrorHelper.isParcelable(parameterTypeMirror))
                {
                    methodBuilder.addStatement("$T $N = null", parameterTypeMirror, parameterName);
                    methodBuilder.addStatement("int __nullFlag = data.readInt()");
                    methodBuilder.beginControlFlow("if (__nullFlag > 0)");
                    methodBuilder.addStatement("$N = $T.CREATOR.createFromParcel(data)", parameterName, parameterTypeMirror);
                    methodBuilder.endControlFlow();
                    return;
                }
                //Serializable
                if (TypeMirrorHelper.isSerializable(parameterTypeMirror))
                {
                    methodBuilder.addStatement("$T $N = data.readSerializable()", parameterTypeMirror, parameterName);
                    return;
                }
                //List
                if (TypeMirrorHelper.isList(parameterTypeMirror))
                {
                    methodBuilder.addStatement("$T $N = new $T<>()", parameterTypeMirror, parameterName,
                            ArrayList.class);
                    methodBuilder.addStatement("data.readList($N, getClass().getClassLoader())", parameterName);
                    return;
                }
            }
            //endregion
        }
    }

    private static void writeReplyBaseOnParameter(MethodSpec.Builder methodBuilder, TypeMirror methodReturnTypeMirror)
    {
        TypeKind returnTypeKind = methodReturnTypeMirror.getKind();
        switch (returnTypeKind)
        {
            case VOID:
            {
                return;
            }
            //region 值类型
            case BYTE:
            {
                methodBuilder.addStatement("reply.writeByte(__result)");
                return;
            }
            case CHAR:
            case SHORT:
            case INT:
            {
                methodBuilder.addStatement("reply.writeInt(($T)__result)", methodReturnTypeMirror);
                return;
            }
            case BOOLEAN:
            {
                methodBuilder.addStatement("reply.writeInt(__result ? 1 : 0)");
                return;
            }
            case LONG:
            {
                methodBuilder.addStatement("reply.writeLong(__result)");
                return;
            }
            case FLOAT:
            {
                methodBuilder.addStatement("reply.writeFloat(__result)");
                return;
            }
            case DOUBLE:
            {
                methodBuilder.addStatement("reply.writeDouble(__result)");
                return;
            }
            //endregion

            //region 引用类型
            default:
            {
                //String
                if (TypeMirrorHelper.isString(methodReturnTypeMirror))
                {
                    methodBuilder.addStatement("reply.writeString(__result)");
                    return;
                }
                //Parcelable
                if (TypeMirrorHelper.isParcelable(methodReturnTypeMirror))
                {
                    methodBuilder.addStatement("reply.writeInt(__result == null ? 0 : 1)");
                    methodBuilder.beginControlFlow("if (__result != null)");
                    methodBuilder.addStatement("__result.writeToParcel(reply, $T.PARCELABLE_WRITE_RETURN_VALUE)",
                            TypeNameHelper.typeNameOfParcelable());
                    methodBuilder.endControlFlow();
                    return;
                }
                //Serializable
                if (TypeMirrorHelper.isSerializable(methodReturnTypeMirror))
                {
                    methodBuilder.addStatement("reply.writeSerializable(__result)");
                    return;
                }
                //List
                if (TypeMirrorHelper.isList(methodReturnTypeMirror))
                {
                    methodBuilder.addStatement("reply.writeList(__result)");
                    return;
                }
            }
            //endregion
        }
    }
}

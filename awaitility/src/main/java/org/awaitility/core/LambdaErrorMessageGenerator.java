/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.awaitility.core;

import java.lang.reflect.Method;

/**
 * Helper class for generating nicer error messages when lambda expression are used
 */
class LambdaErrorMessageGenerator {

    private static final String LAMBDA_CLASS_NAME = "$$Lambda$";
    private static final String LAMBDA_METHOD_NAME = "$Lambda";


    static boolean isLambdaClass(Class<?> cls) {
        return cls.getSimpleName().contains(LAMBDA_CLASS_NAME);
    }

    static String generateLambdaErrorMessagePrefix(Class<?> lambdaClass, boolean firstLetterLowerCaseAndEndWithColon) {
        String name = lambdaClass.getName();
        int indexOfLambda = name.indexOf(LAMBDA_CLASS_NAME);
        String nameWithoutLambda = name.substring(0, indexOfLambda);
        nameWithoutLambda = addLambdaDetailsIfFound(lambdaClass, nameWithoutLambda, firstLetterLowerCaseAndEndWithColon);
        return nameWithoutLambda;
    }

    private static String addLambdaDetailsIfFound(Class<?> supplierClass, String nameWithoutLambda, boolean firstLetterUpperCaseAndEndWithColon) {
        String nameToReturn = nameWithoutLambda;
        Method[] declaredMethods = supplierClass.getDeclaredMethods();
        Method lambdaMethod = null;
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().contains(LAMBDA_METHOD_NAME)) {
                lambdaMethod = declaredMethod;
                break;
            }
        }
        if (lambdaMethod != null) {
            Class<?>[] lambdaParams = lambdaMethod.getParameterTypes();
            if (lambdaParams.length > 0) {
                StringBuilder nameToReturnBuilder = new StringBuilder(firstLetterUpperCaseAndEndWithColon ? "L" : "l")
                        .append("ambda expression in ").append(nameToReturn);
                if (nameWithoutLambda.equals(lambdaParams[0].getName())) {
                    if (firstLetterUpperCaseAndEndWithColon) {
                        nameToReturnBuilder.append(':');
                    }
                } else {
                    nameToReturnBuilder.append(" that uses ");
                    for (int i = 0; i < lambdaParams.length; i++) {
                        Class<?> lambdaParam = lambdaParams[i];
                        nameToReturnBuilder.append(lambdaParam.getName());
                        if (i + 1 == lambdaParams.length) {
                            if (firstLetterUpperCaseAndEndWithColon) {
                                nameToReturnBuilder.append(':');
                            }
                        } else {
                            nameToReturnBuilder.append(", ").append(lambdaParam.getName());
                        }
                    }
                }
                nameToReturn = nameToReturnBuilder.toString();
            }
        }
        return nameToReturn;
    }
}

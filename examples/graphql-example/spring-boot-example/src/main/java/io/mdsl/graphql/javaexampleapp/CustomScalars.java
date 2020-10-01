/*
 * Copyright 2020 The Context Mapper Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mdsl.graphql.javaexampleapp;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

public class CustomScalars {

    public static final GraphQLScalarType VOID = new GraphQLScalarType.Builder().name("VoidResponse").coercing(new Coercing() {
        @Override
        public Object serialize(Object dataFetcherResult) {
            return "Void";
        }

        @Override
        public Object parseValue(Object input) {
            return "Void";
        }

        @Override
        public Object parseLiteral(Object input) {
            return "Void";
        }
    }).build();

    public static final GraphQLScalarType RAW = new GraphQLScalarType.Builder().name("Raw").coercing(new Coercing() {
        @Override
        public Object serialize(Object dataFetcherResult) {
            return "Raw";
        }

        @Override
        public Object parseValue(Object input) {
            return "Raw";
        }

        @Override
        public Object parseLiteral(Object input) {
            return "Raw";
        }
    }).build();

}

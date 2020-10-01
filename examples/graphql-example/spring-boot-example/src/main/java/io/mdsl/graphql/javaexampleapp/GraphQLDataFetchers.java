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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import io.mdsl.graphql.javaexampleapp.generated.Resolvers;
import io.mdsl.graphql.javaexampleapp.generated.Types;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GraphQLDataFetchers implements Resolvers.Query {

    private static List<Map<String, String>> papers = Arrays.asList(
            ImmutableMap.of(
                    "title", "Domain-driven Service Design - Context Modeling, Model Refactoring and Contract Generation",
                    "authors", "Stefan Kapferer and Olaf Zimmermann",
                    "venue", "OST"),
            ImmutableMap.of(
                    "title", "Dimensions of Successful Web API Design and Evolution: Context, Contracts, Components",
                    "authors", "Olaf Zimmermann",
                    "venue", "OST")
    );

    @Override
    public DataFetcher<Object> lookupPapersFromAuthor() {
        return dataFetchingEnvironment -> {
            Types.QueryLookupPapersFromAuthorArgs input = new Types.QueryLookupPapersFromAuthorArgs(dataFetchingEnvironment.getArguments());
            String author = input.getAnonymousInput().getAnonymous1();
            return ImmutableMap.of("entries", papers
                    .stream()
                    .filter(paper -> paper.get("authors").contains(author))
                    .collect(Collectors.toList()));
        };
    }
}

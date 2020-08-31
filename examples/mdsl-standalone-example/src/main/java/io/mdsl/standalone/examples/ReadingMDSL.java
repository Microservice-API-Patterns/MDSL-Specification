/*
 * Copyright 2020 The MDSL Project Team
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
package io.mdsl.standalone.examples;

import io.mdsl.MDSLResource;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.standalone.MDSLStandaloneAPI;
import io.mdsl.standalone.MDSLStandaloneSetup;

/**
 * An example that illustrates how you can read an MDSL model in Java.
 *
 * @author Stefan Kapferer
 */
public class ReadingMDSL {

    public final static String HELLO_WORLD_EXAMPLE_URI = "./src/main/mdsl/hello-world.mdsl";

    public static void main(String[] args) {
        // load the MDSL model
        MDSLStandaloneAPI mdslAPI = MDSLStandaloneSetup.getStandaloneAPI();
        MDSLResource mdsl = mdslAPI.loadMDSL(HELLO_WORLD_EXAMPLE_URI);

        // reading the model
        ServiceSpecification serviceSpecification = mdsl.getServiceSpecification();

        // do something with your model
        System.out.println("We have read the service specification with the name: " + serviceSpecification.getName());
    }

}

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
import io.mdsl.generator.ProtocolBuffersGenerator;
import io.mdsl.standalone.MDSLStandaloneAPI;
import io.mdsl.standalone.MDSLStandaloneSetup;

/**
 * A simple example that illustrates how to use the MDSL generators.
 * In this case the Protocol Buffers (*.proto) generator.
 *
 * @author Stefan Kapferer
 */
public class ProtocolBuffersGeneration {

    public static void main(String[] args) {
        // read an MDSL model
        MDSLStandaloneAPI mdslAPI = MDSLStandaloneSetup.getStandaloneAPI();
        MDSLResource mdsl = mdslAPI.loadMDSL(ReadingMDSL.HELLO_WORLD_EXAMPLE_URI);

        // call the generator
        mdslAPI.callGenerator(mdsl, new ProtocolBuffersGenerator());

        // hint: in case you want to generate into another folder than "./src-gen", use the following call:
        // mdslAPI.callGenerator(mdsl, new ProtocolBuffersGenerator(), "./out-dir");
    }

}

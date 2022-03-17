package io.mdsl.jaamsim;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import io.mdsl.apiDescription.EndpointContract;
import io.mdsl.apiDescription.ServiceSpecification;
import io.mdsl.generator.jaamsim.JaamSimToMDSL;

public class JaamSim2MDSLFileGenerationTest {

	private static final String CONFIG_FILE_SUFFIX = ".cfg";
	private static final String TEST_DATA_PATH = "src/test/resources/"; // TODO adopt as needed

	@Test
	public void canCreateMDSLFileForJaamSimTestCase1SequentialFlow() {
		// given
		String testInputFileName = TEST_DATA_PATH + "flowtest1-jaamsim" + CONFIG_FILE_SUFFIX;
		String testOutputFileName = "flowtest1-jaamsim.mdsl";

		try {		
			// when
			JaamSimToMDSL j2m = new JaamSimToMDSL(testInputFileName);
			ServiceSpecification mdsl = j2m.convert();
			j2m.writeToFile(mdsl, testOutputFileName); // goes to main directory of project

			// then
			assertEquals(getExpectedTestResult("flowtest1-jaamsim-expected.mdsl"), getGeneratedFileContent(testOutputFileName));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void canCreateMDSLFileForJaamSimTestCase2ParallelSplit() {
		// given
		String testInputFileName = TEST_DATA_PATH + "flowtest2-jaamsim" + CONFIG_FILE_SUFFIX;
		String testOutputFileName = "flowtest2-jaamsim.mdsl";

		// when
		try {		
			JaamSimToMDSL j2m = new JaamSimToMDSL(testInputFileName);
			ServiceSpecification mdsl = j2m.convert();
			j2m.writeToFile(mdsl, testOutputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// then
		assertEquals(getExpectedTestResult("flowtest2-jaamsim-expected.mdsl"), getGeneratedFileContent(testOutputFileName));
	}
	
	@Test
	public void canCreateMDSLFileForJaamSimTestCase3a() {
		// given
		String testInputFileName = TEST_DATA_PATH + "flowtest3a-jaamsim" + CONFIG_FILE_SUFFIX;
		String testOutputFileName = "flowtest3a-jaamsim.mdsl";

		try {		
			// when
			JaamSimToMDSL j2m = new JaamSimToMDSL(testInputFileName);
			ServiceSpecification mdsl = j2m.convert();
			j2m.writeToFile(mdsl, testOutputFileName);

			// then
			assertEquals(getExpectedTestResult("flowtest3a-jaamsim-expected.mdsl"), getGeneratedFileContent(testOutputFileName));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void canCreateMDSLFileForJaamSimTestCase4aFlow5() {
		// given
		String testInputFileName = TEST_DATA_PATH + "flowtest4aflow5-jaamsim" + CONFIG_FILE_SUFFIX;
		String testOutputFileName = "flowtest4aflow5-jaamsim.mdsl";

		try {		
			// when
			JaamSimToMDSL j2m = new JaamSimToMDSL(testInputFileName);
			ServiceSpecification mdsl = j2m.convert();
			j2m.writeToFile(mdsl, testOutputFileName);

			// then
			assertEquals(getExpectedTestResult("flowtest4aflow5-jaamsim-expected.mdsl"), getGeneratedFileContent(testOutputFileName));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void canCreateMDSLFileForPSOADDemoFlow() {
		// given
		String testInputFileName = TEST_DATA_PATH + "process-driven-SOAD-final-jaamsim" + CONFIG_FILE_SUFFIX;
		String testOutputFileName = "process-driven-SOAD-final-jaamsim.mdsl";

		try {		
			// when
			JaamSimToMDSL j2m = new JaamSimToMDSL(testInputFileName);
			ServiceSpecification mdsl = j2m.convert();
			j2m.writeToFile(mdsl, testOutputFileName);

			// then
			assertEquals(getExpectedTestResult("process-driven-SOAD-final-jaamsim-expected.mdsl"), getGeneratedFileContent(testOutputFileName));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String getExpectedTestResult(String testOracle) {
		try {
			return FileUtils.readFileToString(new File(Paths.get("").toAbsolutePath().toString(), TEST_DATA_PATH + testOracle), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Error: Test oracle unavailable";
	}

	protected String getGeneratedFileContent(String testOutput) {
		String generatedFileContent;
		try {
			generatedFileContent = FileUtils.readFileToString(new File(Paths.get("").toAbsolutePath().toString(), testOutput), "UTF-8");
			return generatedFileContent.replaceFirst("// Generated with MDSL2JaamSim .*?(\\r?\\n|\\r)", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Error: Can't read generated test outout.";
	}
}

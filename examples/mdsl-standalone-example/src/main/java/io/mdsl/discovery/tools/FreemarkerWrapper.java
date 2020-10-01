package io.mdsl.discovery.tools;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
// import io.mdsl.discovery.example.fmpoc.ValueExampleObject;

// started from https://www.vogella.com/tutorials/FreeMarker/article.html 
public class FreemarkerWrapper {

	public static void generate(String templateName, Map<String, Object> input, String outputFileName) throws Exception {

		Version versionInfo = new Version(2, 3, 0);
		Configuration cfg = new Configuration(versionInfo);
		cfg.setClassForTemplateLoading(FreemarkerWrapper.class, ".");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		Template template = cfg.getTemplate(templateName);

		// write output to the console
		// Writer consoleWriter = new OutputStreamWriter(System.out);
		// template.process(input, consoleWriter);

		Writer fileWriter = new FileWriter(new File(outputFileName));
		try {
			template.process(input, fileWriter);
		} finally {
			fileWriter.close();
		}
	}
}
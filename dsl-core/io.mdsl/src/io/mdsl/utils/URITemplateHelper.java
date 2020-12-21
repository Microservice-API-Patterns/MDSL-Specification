package io.mdsl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URITemplateHelper {

	/*
	public static void main(String[] args) {
		String text = "Test string, with occurrences of {id}/{subid}.";
		List<String> strarr = findTemplateParameters(text);
		strarr.forEach(template->System.out.println("Template: " + template));
	}
	*/

	public static List<String> findTemplateParameters(String uriSnippet) {
		List<String> result = new ArrayList<String>();
		String regex = "\\{\\w*\\}";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(uriSnippet);

		while(matcher.find()) {
			String nextTemplate = uriSnippet.substring(matcher.start(), matcher.end());
			result.add(nextTemplate);
		}
		return result;
	}
}

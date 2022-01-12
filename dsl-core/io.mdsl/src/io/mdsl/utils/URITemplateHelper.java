package io.mdsl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URITemplateHelper {

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

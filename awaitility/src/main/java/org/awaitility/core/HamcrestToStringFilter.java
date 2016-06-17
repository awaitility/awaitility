/*
 * Copyright 2010 the original author or authors.
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

import org.hamcrest.Matcher;

import java.util.LinkedList;
import java.util.List;

/**
 * The Class HamcrestToStringFilter.
 */
class HamcrestToStringFilter {
	private static final List<String> wordsToRemove = new LinkedList<String>();
	static {
		wordsToRemove.add("not not ");
		wordsToRemove.add("is ");
	}

	/**
	 * Filter words from the <code>matcher.toString()</code> so it looks nicer
	 * when printed out. E.g. "not not" is removed and "is" are removed.
	 * 
	 * @param matcher
	 *            the matcher
	 * @return A filtered version of the {@link Matcher#toString()}.
	 */
	static String filter(Matcher<?> matcher) {
		String matcherToString = matcher.toString();
		for (String wordToRemove : wordsToRemove) {
			matcherToString = matcherToString.replaceAll(wordToRemove, "");
		}
		return matcherToString;
	}
}

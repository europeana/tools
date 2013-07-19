/*
 * Copyright 2005-2009 the original author or authors.
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
package eu.annocultor.utils;

import java.lang.reflect.Constructor;
import java.util.List;

import eu.annocultor.api.CustomConverter;
import eu.annocultor.common.Helper;


public class WriterUsageStats
{

	public static void main(String... args) throws Exception
	{
		List<Class> converters =
				Helper
						.findAllClasses(CustomConverter.class, "nl.multimedian.eculture.annocultor.eculture.converters");
		for (Class converter : converters)
		{
			Constructor constructor = converter.getConstructor(new Class[]
			{});
			CustomConverter conv = (CustomConverter) constructor.newInstance(new Object[]
			{});
		}

		// System.out.println(ConvertObjectRule.mappingReport.report(true));

		// System.out.println(ConvertObjectRule.mappingReport.printTermOccurranceMatrix());
	}

}

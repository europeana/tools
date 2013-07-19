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
package eu.annocultor.xconverter.impl;

import java.io.IOException;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream
{
	private StringBuilder string = new StringBuilder();

	@Override
	public void write(int b) throws IOException
	{
		string.append((char)b);
	}

	@Override
	public String toString()
	{
		return string.toString();
	}


}

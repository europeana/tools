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
package eu.europeana.enrichment.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import eu.europeana.enrichment.utils.OntologySubtractor;

/**
 * CLI client for AnnoCultor tools.
 * 
 * @author Borys Omelayenko
 * 
 */

// TODO: analyze and pretty
public class Cli {

	public static interface CliExecutable {
		public abstract void mainMethod(String... args) throws Exception;
	}

	public static Map<String, CliExecutable> commands = new HashMap<String, CliExecutable>();

	static {
		commands.put("subtract", new OntologySubtractor());
	}

	static void error(String command) {
		System.err.println("Unknown command: '" + command
				+ "', expected one of:");
		for (String allowedCommand : commands.keySet()) {
			System.err.println("  " + allowedCommand);
		}
	}

	static public void main(String... args) throws Exception {

		String command = args[0];

		if (commands.containsKey(command)) {
			CliExecutable executable = commands.get(command);
			executable.mainMethod(Arrays.copyOfRange(args, 1, args.length - 1));
		} else {
			error(command);
		}
	}
}
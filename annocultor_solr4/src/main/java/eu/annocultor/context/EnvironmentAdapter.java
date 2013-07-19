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
package eu.annocultor.context;

import java.io.File;



/**
 * Customized environment. 
 * 
 * @author Borys Omelayenko
 * 
 */
public class EnvironmentAdapter implements Environment
{

	Environment environment;
	
	public EnvironmentAdapter(Environment environment) {
		this.environment = environment;
	}

	// overrides
	
	public void completeParameter(PARAMETERS parameter, String value) {
		environment.completeParameter(parameter, value);
	}

	public void completeWithDefaults() throws Exception {
		environment.completeWithDefaults();
	}

	public File getAnnoCultorHome() {
		return environment.getAnnoCultorHome();
	}

	public File getCollectionDir() {
		return environment.getCollectionDir();
	}

	public File getDocDir() {
		return environment.getDocDir();
	}

	public Namespaces getNamespaces() {
		return environment.getNamespaces();
	}

	public File getOutputDir() {
		return environment.getOutputDir();
	}

	public String getParameter(PARAMETERS parameter) {
		return environment.getParameter(parameter);
	}

	public File getPreviousDir() {
		return environment.getPreviousDir();
	}

	public File getTmpDir() {
		return environment.getTmpDir();
	}

	public File getVocabularyDir() {
		return environment.getVocabularyDir();
	}

	public void init() {
		environment.init();
	}

	public void initializeVocabularies() throws Exception {
		environment.initializeVocabularies();
	}

	public void setParameter(PARAMETERS parameter, String value) {
		environment.setParameter(parameter, value);
	}

	@Override
	public Concepts getConcepts() {
		return environment.getConcepts();
	}

	@Override
	public String toString() {
		return environment.toString();
	}

	@Override
	public boolean checkSignatureForDuplicates(String signature) {
		return environment.checkSignatureForDuplicates(signature);
	}

    @Override
    public String getBuildSignature() {
        return environment.getBuildSignature();
    }
	
	
}

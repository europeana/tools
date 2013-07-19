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
 * Provides deployment- and run-specific parameters for running a converter.
 * 
 * The converter performs the following steps to the RDF file (that represents a
 * named graph) that are controlled by the Environment:
 * <ul>
 * <li>If directory {@link Environment#getOutputDir()} contains the named graph
 * (as a file in RDF) then this file is copied to
 * {@link Environment#getPreviousDir()} (the directory is created if needed).</li>
 * <li>During the conversion run the new version of the named graph is created
 * and saved in RDF to directory {@link Environment#getTmpDir()}. Occasional
 * existing graphs there are overwritten.</li>
 * <li>The graph stored in the {@link Environment#getTmpDir()} is compared with
 * the version from directory {@link Environment#getOutputDir()}. If they differ
 * then the version from {@link Environment#getOutputDir()} is overwritten with
 * the new one. If they do not differ then it is left untouched (and would not
 * confuse your version control system, if you use one for your RDF files).</li>
 * <li>If directory {@link Environment#getDocDir()} exists then a conversion
 * report is generated and saved there.</li>
 * <li>If directory {@link Environment#getDiffDir()} exists and there is a
 * version of this named graph stored in the
 * {@link Environment#getPreviousDir()} then they are compared and a diff report
 * is produced and stored in {@link Environment#getDiffDir()}.</li>
 * <li>If directory {@link Environment#getOutputOntologyDir()} exists then schema concepts
 * (ontology) defined by {@link Environment#getConcepts()} are serialized in RDF
 * and stored there.</li>
 * </ul>
 * 
 * On construction, file <code>annocultor.properties</code> is read from the
 * local directory. If exists, it may contain custom values for specific
 * properties.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface Environment
{
	public Namespaces getNamespaces();
	
	public String toString();

	public static final String ANNOCULTOR = "ANNOCULTOR_";


	/*
	 * Environment properties. Their names are linked to their external names.
	 */
	public enum PARAMETERS
	{
		ANNOCULTOR_HOME,
		ANNOCULTOR_HOME_SOURCE,
		ANNOCULTOR_COLLECTION_DIR,
		ANNOCULTOR_COLLECTION_DIR_SOURCE,
		ANNOCULTOR_DOC_DIR,
		ANNOCULTOR_PREVIOUS_DIR,
		ANNOCULTOR_TMP_DIR,
		ANNOCULTOR_LOCAL_PROFILE_FILE,
		ANNOCULTOR_KEEP_PREVIOUS,
		ANNOCULTOR_INPUT_DIR,
		ANNOCULTOR_OUTPUT_DIR,
		ANNOCULTOR_VOCABULARY_DIR,

		// AnnoCultor model 
		ANNOCULTOR_MODEL_PERSON,
		ANNOCULTOR_MODEL_PERSON_NAME,
		ANNOCULTOR_MODEL_PERSON_BIRTH_DATE,
		ANNOCULTOR_MODEL_PERSON_DEATH_DATE
		;
	}

	public void completeWithDefaults() throws Exception;

	public void completeParameter(PARAMETERS parameter, String value);

	public void init();
	
	public void setParameter(PARAMETERS parameter, String value);

	public String getParameter(PARAMETERS parameter);

	/*
	 * Directories-parameters
	 */
	public File getAnnoCultorHome();

	public File getCollectionDir();

	public File getDocDir();

	public File getOutputDir();
	
	public File getPreviousDir();

	public File getTmpDir();

	public File getVocabularyDir();

	/**
	 * Assigns vocabularies of terms, places, and people to the corresponding
	 * lookup rules.
	 */
	public void initializeVocabularies() throws Exception;

	public Concepts getConcepts();

	/**
	 * Signatures of datasets and  tasks are checked for uniqueness.
	 */
	public boolean checkSignatureForDuplicates(String signature);
	
	public String getBuildSignature();
}

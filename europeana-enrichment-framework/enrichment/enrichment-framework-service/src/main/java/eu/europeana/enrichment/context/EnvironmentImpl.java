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
package eu.europeana.enrichment.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import eu.europeana.enrichment.common.Utils;


/**
 * Environment implementation.
 * 
 * @author Borys Omelayenko
 * 
 */
public class EnvironmentImpl implements Environment
{

    Namespaces namespaces = new Namespaces();

    public Namespaces getNamespaces() {
        return namespaces;
    }

    @Override
    public String toString()
    {
        String result = "AnnoCultor parameters\n";

        for (PARAMETERS param : EnvironmentImpl.PARAMETERS.values())
        {
            result += param.name() + "=" + getParameter(param) + "\n";
        }

        return result;
    }

    private Map<PARAMETERS, String> parameters;

    private static String getRequiredEnvironmentVariable(String parameter) throws Exception
    {
        if (Utils.getLocalOrGlobalEnvironmentVariable(parameter) == null)
            throw new RuntimeException("Environment variable "
                    + parameter
                    + " is neither set up nor found in annocultor.properties");
        if (!new File(Utils.getLocalOrGlobalEnvironmentVariable(parameter)).exists())
            throw new RuntimeException("Environment variable "
                    + parameter
                    + " points to a directory that does not exist: "
                    + Utils.getLocalOrGlobalEnvironmentVariable(parameter)
                    + " that now means: "
                    + new File(Utils.getLocalOrGlobalEnvironmentVariable(parameter)).getCanonicalPath());
        return Utils.getLocalOrGlobalEnvironmentVariable(parameter);
    }

    @Override
    public void completeWithDefaults() throws Exception
    {
        // collection dir
        File homeDir = getAnnoCultorHome();
        // set defaults
        completeParameter(PARAMETERS.ANNOCULTOR_COLLECTION_DIR, homeDir.getCanonicalPath());
        completeParameter(PARAMETERS.ANNOCULTOR_TMP_DIR, new File(homeDir, "tmp").getCanonicalPath());
        completeParameter(PARAMETERS.ANNOCULTOR_DOC_DIR, new File(homeDir, "doc").getCanonicalPath());
        completeParameter(PARAMETERS.ANNOCULTOR_OUTPUT_DIR, new File(homeDir, "rdf").getCanonicalPath());
        completeParameter(PARAMETERS.ANNOCULTOR_PREVIOUS_DIR, new File(homeDir, "prev").getCanonicalPath());
        completeParameter(PARAMETERS.ANNOCULTOR_VOCABULARY_DIR, new File(getAnnoCultorHome(), "../vocabularies/").getCanonicalPath());
        completeParameter(PARAMETERS.ANNOCULTOR_KEEP_PREVIOUS, "false");
        completeParameter(PARAMETERS.ANNOCULTOR_MODEL_PERSON, Namespaces.ANNOCULTOR_PEOPLE + "Person");
        completeParameter(PARAMETERS.ANNOCULTOR_MODEL_PERSON_NAME, Namespaces.ANNOCULTOR_PEOPLE + "person.name");
        completeParameter(PARAMETERS.ANNOCULTOR_MODEL_PERSON_BIRTH_DATE, Namespaces.ANNOCULTOR_PEOPLE + "person.birth.date");
        completeParameter(PARAMETERS.ANNOCULTOR_MODEL_PERSON_DEATH_DATE, Namespaces.ANNOCULTOR_PEOPLE + "person.death.date");
    }

    @Override
    public void completeParameter(PARAMETERS parameter, String value)
    {
        if (getParameter(parameter) == null)
        {
            setParameter(parameter, value);
        }
    }

    @Override
    public void init()
    {
        // extension point
    }

    public EnvironmentImpl() {
        parameters = new HashMap<PARAMETERS, String>();

        try {

            // programmatic assignments
            init();

            // load HOME
            loadHomeFromEnvironment();

            // file annocultor.properties
            loadParametersFromFile();

            // environment
            loadParametersFromEnvironment();

            // complete other properties with default (relative) paths, if not yet set
            completeWithDefaults();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHomeFromEnvironment() throws Exception {
        completeParameter(PARAMETERS.ANNOCULTOR_HOME, getRequiredEnvironmentVariable(PARAMETERS.ANNOCULTOR_HOME.name()));
    }

    private void loadParametersFromFile() throws Exception {
        Properties props = new Properties();
        File propertiesFile = new File(getCollectionDir(), "annocultor.properties");
        if (propertiesFile.exists()) {
            // load properties
            FileInputStream inStream = new FileInputStream(propertiesFile);
            props.load(inStream);
            inStream.close();
            for (Object key : props.keySet()) {
                PARAMETERS parameterName = PARAMETERS.valueOf(key.toString());
                if (parameterName == null)
                    throw new Exception("Unknown parameter " + key);
                String parameterValue = props.getProperty(key.toString());
                setParameter(parameterName, parameterValue);
            }
            setParameter(PARAMETERS.ANNOCULTOR_LOCAL_PROFILE_FILE, propertiesFile.getCanonicalPath());
            setParameter(PARAMETERS.ANNOCULTOR_HOME_SOURCE, (getParameter(PARAMETERS.ANNOCULTOR_HOME) == null)
                    ? "ERROR! not set up"
                            : "file annocultor.properties");
        };
    }

    private void loadParametersFromEnvironment() throws Exception {
        for (PARAMETERS parameter : PARAMETERS.values()) {
            completeParameter(parameter, Utils.getLocalOrGlobalEnvironmentVariable(parameter.name()));
        }
    }

    // ensure uniqueness of tasks
    private Set<String> signatures = new HashSet<String>();

    @Override
    public boolean checkSignatureForDuplicates(String signature) {
        return signatures.add(signature);
    }

    @Override
    public void setParameter(PARAMETERS parameter, String value)
    {
        parameters.put(parameter, value);
    }

    @Override
    public String getParameter(PARAMETERS parameter)
    {
        return parameters.get(parameter);
    }

    /*
     * Concepts accesses from static context
     */
    protected static Concepts concepts;

    public Concepts getConcepts()
    {
        if (concepts == null)
            concepts = new Concepts();
        return concepts;
    }

    /*
     * Directories-parameters
     */
    @Override
    public final File getAnnoCultorHome()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_HOME);
    }

    @Override
    public final File getCollectionDir()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_COLLECTION_DIR);
    }

    @Override
    public final File getDocDir()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_DOC_DIR);
    }

    @Override
    public final File getOutputDir()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_OUTPUT_DIR);
    }

    @Override
    public final File getPreviousDir()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_PREVIOUS_DIR);
    }

    @Override
    public final File getTmpDir()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_TMP_DIR);
    }

    @Override
    public final File getVocabularyDir()
    {
        return getParameterAsFile(PARAMETERS.ANNOCULTOR_VOCABULARY_DIR);
    }

    private File getParameterAsFile(PARAMETERS parameter)
    {
        return 
        (parameters.get(parameter) == null || parameters.get(parameter).length() == 0) 
        ? null : new File(parameters.get(parameter));
    }

    /*
     * Vocabulary initialize
     */
    protected static boolean isVocabularyInitialized = false;

    /**
     * Assigns vocabularies of terms, places, and people to the corresponding
     * lookup rules.
     */
    @Override
    public void initializeVocabularies() throws Exception
    {
        isVocabularyInitialized = true;
    }

    @Override
    public String getBuildSignature() {
        try {
            InputStream is = getClass().getResourceAsStream("/build.txt");
            if (is == null) {
                return "(build signature could not be read)";
            }
            return IOUtils.readLines(is).get(0);
        } catch (Exception e) {
            return null;
        }
    }

}

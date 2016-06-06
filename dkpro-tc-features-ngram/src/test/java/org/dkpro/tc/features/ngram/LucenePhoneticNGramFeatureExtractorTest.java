/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.features.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.task.uima.DocumentTextClassificationUnitAnnotator;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.features.ngram.LucenePhoneticNGram;
import org.dkpro.tc.features.ngram.io.TestReaderSingleLabel;
import org.dkpro.tc.features.ngram.meta.LucenePhoneticNGramMetaCollector;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;

public class LucenePhoneticNGramFeatureExtractorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    @Test
    public void lucenePhoneticNGramFeatureExtractorTest()
        throws Exception
    {

        File luceneFolder = folder.newFolder();
        File outputPath = folder.newFolder();

        Object[] parameters = new Object[] { LucenePhoneticNGram.PARAM_PHONETIC_NGRAM_USE_TOP_K, 10,
        		LucenePhoneticNGram.PARAM_LUCENE_DIR, luceneFolder };
        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en", TestReaderSingleLabel.PARAM_SOURCE_LOCATION,
                "src/test/resources/data/text*.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription doc = AnalysisEngineFactory.createEngineDescription(
                DocumentTextClassificationUnitAnnotator.class,
                DocumentTextClassificationUnitAnnotator.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT);
        
        AnalysisEngineDescription metaCollector = AnalysisEngineFactory.createEngineDescription(
        		LucenePhoneticNGramMetaCollector.class, parameterList.toArray());

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                parameterList, outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_DOCUMENT,
                DenseFeatureStore.class.getName(), false, false, false, false,
                LucenePhoneticNGram.class.getName());

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, doc, metaCollector);

        // run FE(s)
        SimplePipeline.runPipeline(reader, segmenter, doc, featExtractorConnector);

        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(
                FileUtils.readFileToString(new File(outputPath, JsonDataWriter.JSON_FILE_NAME)),
                DenseFeatureStore.class);
        System.out.println(fs);
        assertEquals(2, fs.getNumberOfInstances());
        assertEquals(1, fs.getUniqueOutcomes().size());
    }
}

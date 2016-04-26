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
package org.dkpro.tc.ml;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;

public class FoldUtil
{
    public static File createMinimalSplit(String inputFolder, int numFolds, int numAvailableJCas, boolean isSequence)
        throws Exception
    {
        File outputFolder = new File(inputFolder, "output");
        int splitNum = (int) Math.ceil(numFolds / (double) numAvailableJCas);

        CollectionReaderDescription createReader = CollectionReaderFactory.createReaderDescription(
                BinaryCasReader.class, BinaryCasReader.PARAM_SOURCE_LOCATION, inputFolder,
                BinaryCasReader.PARAM_PATTERNS, "*.bin");

        AnalysisEngineDescription multiplier = AnalysisEngineFactory.createEngineDescription(
                FoldClassificationUnitCasMultiplier.class,
                FoldClassificationUnitCasMultiplier.PARAM_REQUESTED_SPLITS, splitNum,
                FoldClassificationUnitCasMultiplier.PARAM_USE_SEQUENCES,isSequence);

        AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(
                BinaryCasWriter.class, BinaryCasWriter.PARAM_TARGET_LOCATION,
                outputFolder.getAbsolutePath(), BinaryCasWriter.PARAM_FORMAT, "6+");

        AnalysisEngineDescription both = AnalysisEngineFactory.createEngineDescription(multiplier,
                xmiWriter);

        SimplePipeline.runPipeline(createReader, both);

        return outputFolder;
    }

}
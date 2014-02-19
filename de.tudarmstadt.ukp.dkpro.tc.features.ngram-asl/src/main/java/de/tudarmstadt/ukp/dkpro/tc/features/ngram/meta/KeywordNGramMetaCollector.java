package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.util.FeatureUtil;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.KeywordNGramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.KeywordNGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneSkipNgramFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.NGramUtils;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneBasedMetaCollector;

public class KeywordNGramMetaCollector
	extends LuceneBasedMetaCollector
{    
	@ConfigurationParameter(name = KeywordNGramFeatureExtractor.PARAM_KEYWORD_NGRAM_MIN_N, mandatory = true, defaultValue = "1")
	private int minN;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractor.PARAM_KEYWORD_NGRAM_MAX_N, mandatory = true, defaultValue = "3")
	private int maxN;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractor.PARAM_NGRAM_KEYWORDS_FILE, mandatory = true)
	private String keywordsFile;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractor.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_BOUNDARY, mandatory = false, defaultValue = "true")
	private boolean markSentenceBoundary;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractor.PARAM_KEYWORD_NGRAM_MARK_SENTENCE_LOCATION, mandatory = false, defaultValue = "false")
	private boolean markSentenceLocation;
	
	@ConfigurationParameter(name = KeywordNGramFeatureExtractor.PARAM_KEYWORD_NGRAM_INCLUDE_COMMAS, mandatory = false, defaultValue = "false")
	private boolean includeCommas;


	//private Set<String> stopwords;
	private Set<String> keywords;
	
	@Override
	public void initialize(UimaContext context)
	    throws ResourceInitializationException
	{
	    super.initialize(context);
	    
	    try {
	        keywords = FeatureUtil.getStopwords(keywordsFile, true);
	    }
	    catch (IOException e) {
	        throw new ResourceInitializationException(e);
	    }
	}
	
	@Override
	public void process(JCas jcas)
	    throws AnalysisEngineProcessException
	{
	    FrequencyDistribution<String> documentNGrams = KeywordNGramUtils.getDocumentKeywordNgrams(
	            jcas, minN, maxN, markSentenceBoundary, markSentenceLocation, includeCommas, keywords);
	
	    for (String ngram : documentNGrams.getKeys()) {
	        addField(jcas, KeywordNGramFeatureExtractor.KEYWORD_NGRAM_FIELD, ngram); 
	    }
	   
	    try {
	        writeToIndex();
	    }
	    catch (IOException e) {
	        throw new AnalysisEngineProcessException(e);
	    }
	}
}

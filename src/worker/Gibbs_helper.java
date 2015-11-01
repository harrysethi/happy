/**
 * 
 */
package worker;

import helper.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.AccuracyType;
import constants.Consts;
import constants.ModelType;
import constants.WordNumType;
import domain.Gibbs_sample;
import domain.InGraph;
import domain.InGraphNode;
import domain.Pair_truth;
import domain.Potentials;

/**
 * @author harinder
 *
 */
public class Gibbs_helper {
	
	public static double startWork(InGraph inGraph, ModelType modelType, List<Pair_truth> mostProbablePairs, boolean isRandom, 
			AccuracyType accuracyType, double loglikelihood, int index, List<Pair_truth> truthPairs) {
		
		List<Gibbs_sample> samples = getSamples(inGraph, modelType, isRandom);
		
		if(accuracyType == AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD) {
			Pair_truth pair_truth = truthPairs.get(index);
			
			loglikelihood += getWordProb_log(pair_truth, inGraph, samples);
		}
		else  {
			myModelAccuracy_helper(inGraph, modelType, mostProbablePairs, isRandom, samples);
		}
	
		return loglikelihood;
	}
	
	private static double getWordProb_log(Pair_truth truthPair, InGraph inGraph, List<Gibbs_sample> samples){
		double wordProb = 0.0;

		for(int i=0;i<inGraph.nodes_w1.length;i++) {
				InGraphNode inGraphNode = inGraph.nodes_w1[i];
				char curr_char = truthPair.first.charAt(i);
				wordProb += Math.log(getCharacterProb_w1(inGraphNode, curr_char, samples, i));
		}
		
		for(int i=0;i<inGraph.nodes_w2.length;i++) {
			InGraphNode inGraphNode = inGraph.nodes_w2[i];
			char curr_char = truthPair.second.charAt(i);
			wordProb += Math.log(getCharacterProb_w2(inGraphNode, curr_char, samples, i));
	}
		
		return wordProb;
	}
	
	private static double getCharacterProb_w1(InGraphNode inGraphNode, char curr_char, List<Gibbs_sample> samples, int index) {
		Map<Character, Integer> countMap = new HashMap<Character, Integer>();
		for(Gibbs_sample sample : samples) {
			Integer count = countMap.get(sample.sample_w1[index]);
			if(count==null) countMap.put(sample.sample_w1[index],1);
			else countMap.put(sample.sample_w1[index], count+1);
		}
		
		Integer count = countMap.get(curr_char);
		if(count==null) count = 0;
		if(count == 0) return 0.01;
		return (double)count/samples.size();
	}
	
	private static double getCharacterProb_w2(InGraphNode inGraphNode, char curr_char, List<Gibbs_sample> samples, int index) {
		Map<Character, Integer> countMap = new HashMap<Character, Integer>();
		for(Gibbs_sample sample : samples) {
			Integer count = countMap.get(sample.sample_w2[index]);
			if(count==null) countMap.put(sample.sample_w2[index],1);
			else countMap.put(sample.sample_w2[index], count+1);
		}
		
		Integer count = countMap.get(curr_char);
		if(count==null) count = 0;
		if(count == 0) return 0.01;
		return (double)count/samples.size();
	}
	

	private static void myModelAccuracy_helper(InGraph inGraph,	ModelType modelType, List<Pair_truth> mostProbablePairs,
			boolean isRandom, List<Gibbs_sample> samples) {
		
		Gibbs_sample atLast = new Gibbs_sample(inGraph.nodes_w1.length, inGraph.nodes_w2.length);
		
		for(int i=0;i<inGraph.nodes_w1.length;i++) {
			Map<Character, Integer> countMap = new HashMap<Character, Integer>();
			for(Gibbs_sample sample : samples) {
				Integer count = countMap.get(sample.sample_w1[i]);
				if(count==null) countMap.put(sample.sample_w1[i],1);
				else countMap.put(sample.sample_w1[i], count+1);
			}
			
			char maxChar = 'd';
			int maxCount = 0;
			for(Map.Entry<Character, Integer> entry : countMap.entrySet()) {
				char c = entry.getKey();
				int count = entry.getValue();
				if(maxCount<count) {maxCount = count; maxChar=c;}
			}
			
			atLast.sample_w1[i] = maxChar;
		}
		
		for(int i=0;i<inGraph.nodes_w2.length;i++) {
			Map<Character, Integer> countMap = new HashMap<Character, Integer>();
			for(Gibbs_sample sample : samples) {
				Integer count = countMap.get(sample.sample_w2[i]);
				if(count==null) countMap.put(sample.sample_w2[i],1);
				else countMap.put(sample.sample_w2[i], count+1);
			}
			
			char maxChar = 'd';
			int maxCount = 0;
			for(Map.Entry<Character, Integer> entry : countMap.entrySet()) {
				char c = entry.getKey();
				int count = entry.getValue();
				if(maxCount<count) {maxCount = count; maxChar=c;}
			}
			
			atLast.sample_w2[i] = maxChar;
		}
		
		Pair_truth mostProbablePair = new Pair_truth();
		mostProbablePair.first = String.valueOf(atLast.sample_w1);
		mostProbablePair.second = String.valueOf(atLast.sample_w2);
		
		mostProbablePairs.add(mostProbablePair);
	}

	private static List<Gibbs_sample> getSamples(InGraph inGraph, ModelType modelType, boolean isRandom) {

		// generating samples
		List<Gibbs_sample> samples = new ArrayList<Gibbs_sample>();
		samples.add(generateRandomSample(inGraph));

		Gibbs_sample lastSample = samples.get(0);

		int numOfNodes = inGraph.nodes_w1.length + inGraph.nodes_w2.length;
		int t = 0;
		int r = 500; //burn-in phase time
		//int T = r/numOfNodes;
		int T = 1000;
		int TT = T + r;
		boolean burnInOver = false;
		
		do {
			t++;
			
			if((t-1)*numOfNodes>r){
				burnInOver = true;
				//t=0;
			}
			
			lastSample = sampleVariables(samples, inGraph, modelType, lastSample, burnInOver, isRandom);
			
		} while(!burnInOver || t<TT);
		
		return samples;
		
		//System.out.println(lastSample);
	}
	
	private static Gibbs_sample sampleVariables(List<Gibbs_sample> samples, InGraph inGraph, ModelType modelType, Gibbs_sample lastSample, boolean burnInOver, boolean isRandom) {

		if(!isRandom) {
			lastSample = sampleVariables_helper(samples, inGraph, modelType, lastSample, WordNumType.WORD_NUM_1, burnInOver);
			lastSample = sampleVariables_helper(samples, inGraph, modelType, lastSample, WordNumType.WORD_NUM_2, burnInOver);
		}
		
		else{
			int numOfNodes = inGraph.nodes_w1.length+inGraph.nodes_w2.length;
			
			for(int i=0;i<numOfNodes;i++){
				int randomWord = Util.randInt(1, 2);
				WordNumType wordToSample;
				int sizeOfWordToSample;
				
				if(randomWord==1) {wordToSample = WordNumType.WORD_NUM_1; sizeOfWordToSample=inGraph.nodes_w1.length;}
				else {wordToSample = WordNumType.WORD_NUM_2; sizeOfWordToSample=inGraph.nodes_w2.length;}
				
				int randomIndex = Util.randInt(0, sizeOfWordToSample-1);
				lastSample = sampleVariable(samples, inGraph, modelType, lastSample, wordToSample, burnInOver, randomIndex);
			}
		}
		
		return lastSample;
	}
	
	private static Gibbs_sample sampleVariables_helper(List<Gibbs_sample> samples, InGraph inGraph, ModelType modelType, 
			Gibbs_sample lastSample, WordNumType wordNumType, boolean burnInOver) {
		
		int size = inGraph.nodes_w1.length;
		if(wordNumType == WordNumType.WORD_NUM_2)
			size = inGraph.nodes_w2.length;
		
		for (int i = 0; i < size; i++) {
			lastSample = sampleVariable(samples, inGraph, modelType, lastSample, wordNumType, burnInOver, i);
		}
		
		return lastSample;
	}

	private static Gibbs_sample sampleVariable(List<Gibbs_sample> samples, InGraph inGraph, ModelType modelType, Gibbs_sample lastSample,
			WordNumType wordNumType, boolean burnInOver, int i) {
		
		double prob[] = new double[Consts.characters.length];
		double totalProb = 0.0;
		
		//get numerator probs
		for (int j = 0; j < Consts.characters.length; j++) {
			char newChar = Consts.characters[j];
			prob[j] = getNumeratorForProb(inGraph, modelType, lastSample, wordNumType, i, newChar);
			totalProb+=prob[j];
		}
		
		//get probs -> numeratorProbs/totalNumeratorProb
		for(int j=0;j<Consts.characters.length;j++) {
			prob[j] /= totalProb;
		}
		
		//selecting the character
		double randomProb = Math.random();
		Character newChar = null;
		if(randomProb<prob[0]) {
			newChar = Consts.characters[0];
		}
		
		double cummProb = prob[0];
		for(int j=1;j<Consts.characters.length;j++){
			cummProb += prob[j];
			if(randomProb<cummProb) {
				newChar = Consts.characters[j];
				break;
			}
		}
		
		lastSample = new Gibbs_sample(lastSample, i, newChar, wordNumType);
		if(burnInOver) samples.add(lastSample);
		return lastSample;
	}
	
	private static double getNumeratorForProb (InGraph inGraph, ModelType modelType, 
			Gibbs_sample lastSample, WordNumType wordNumType, int indexInFocus, char newChar) {
		Gibbs_sample newSample = new Gibbs_sample(lastSample, indexInFocus, newChar, wordNumType);
		double prob = 1.0;
		
		switch(modelType) {
		case PAIR_SKIP_MODEL:
			for(int i=0;i<inGraph.nodes_w1.length;i++){
				for(int j=0;j<inGraph.nodes_w2.length;j++){
					if (inGraph.nodes_w1[i].imgID.equals(inGraph.nodes_w2[j].imgID))
						prob *= Potentials.getPairSkipFactor(newSample.sample_w1[i], newSample.sample_w2[j]);
				}
			}
			//intentionally missing break
			
		case COMBINED_MODEL:
			for(int i=0;i<inGraph.nodes_w1.length-1;i++){
				for(int j=i+1;j<inGraph.nodes_w1.length;j++){
					if (inGraph.nodes_w1[i].imgID.equals(inGraph.nodes_w1[j].imgID))
						prob *= Potentials.getSkipFactor(newSample.sample_w1[i], newSample.sample_w1[j]);
				}
			}
			
			for(int i=0;i<inGraph.nodes_w2.length-1;i++){
				for(int j=i+1;j<inGraph.nodes_w2.length;j++){
					if (inGraph.nodes_w2[i].imgID.equals(inGraph.nodes_w2[j].imgID))
						prob *= Potentials.getSkipFactor(newSample.sample_w2[i], newSample.sample_w2[j]);
				}
			}
			//intentionally missing break
			
		case TRANSITION_MODEL:
			for (int i = 0; i < inGraph.nodes_w1.length-1; i++) {
				prob *= Potentials.getTransFactor(newSample.sample_w1[i], newSample.sample_w1[i+1]);
			}
			
			for (int i = 0; i < inGraph.nodes_w2.length-1; i++) {
				prob *= Potentials.getTransFactor(newSample.sample_w2[i], newSample.sample_w2[i+1]);
			}
			//intentionally missing break
			
		case OCR_MODEL:
			for(int i=0;i<inGraph.nodes_w1.length;i++){
				prob *= Potentials.getOcrFactor(inGraph.nodes_w1[i].imgID, newSample.sample_w1[i]);
			}
			
			for(int i=0;i<inGraph.nodes_w2.length;i++){
				prob *= Potentials.getOcrFactor(inGraph.nodes_w2[i].imgID, newSample.sample_w2[i]);
			}
			//intentionally missing break
		}
		
		
		return prob;
	}
	
	private static Gibbs_sample generateRandomSample(InGraph inGraph) {
		int size_w1 = inGraph.nodes_w1.length;
		int size_w2 = inGraph.nodes_w2.length;
		
		Gibbs_sample gibbs_sample = new Gibbs_sample(size_w1, size_w2);
		
		for(int i=0; i<size_w1; i++) {
			gibbs_sample.sample_w1[i] = Util.getRandomCharacter();
		}
		
		for(int i=0; i<size_w2; i++) {
			gibbs_sample.sample_w2[i] = Util.getRandomCharacter();
		}
		
		return gibbs_sample;
	}
	
}

/**
 * 
 */
package worker;

import helper.Util;

import java.util.ArrayList;
import java.util.List;

import constants.Consts;
import constants.ModelType;
import constants.WordNumType;
import domain.Gibbs_sample;
import domain.InGraph;
import domain.Potentials;

/**
 * @author harinder
 *
 */
public class Gibbs_helper {

	public static void startWork(InGraph inGraph, ModelType modelType) {

		// generating samples
		List<Gibbs_sample> samples = new ArrayList<Gibbs_sample>();
		samples.add(generateRandomSample(inGraph));

		Gibbs_sample lastSample = samples.get(0);

		while (true) { // TODO
			lastSample = sampleVariables(samples, inGraph, modelType, lastSample);
		}
	}
	
	private static Gibbs_sample sampleVariables(List<Gibbs_sample> samples, InGraph inGraph, ModelType modelType, Gibbs_sample lastSample) {
		lastSample = sampleVariables_helper(samples, inGraph, modelType, lastSample, WordNumType.WORD_NUM_1);
		lastSample = sampleVariables_helper(samples, inGraph, modelType, lastSample, WordNumType.WORD_NUM_2);
		
		return lastSample;
	}
	
	private static Gibbs_sample sampleVariables_helper(List<Gibbs_sample> samples, InGraph inGraph, ModelType modelType, 
			Gibbs_sample lastSample, WordNumType wordNumType) {
		
		int size = inGraph.nodes_w1.length;
		if(wordNumType == WordNumType.WORD_NUM_2)
			size = inGraph.nodes_w2.length;
		
		for (int i = 0; i < size; i++) {
			
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
			samples.add(lastSample);
		}
		
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

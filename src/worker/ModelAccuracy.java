/**
 * 
 */
package worker;

import helper.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import constants.AccuracyType;
import constants.InferenceType;
import constants.ModelType;
import domain.CliqueTree;
import domain.InGraph;
import domain.InGraphNode;
import domain.LB_graph;
import domain.Pair_data;
import domain.Pair_truth;

/**
 * @author harinder
 *
 */
public class ModelAccuracy {
	
	public static double getModelAccuracy(String dataTreePath, String truthTreePath, 
			ModelType modelType, AccuracyType accuracyType, InferenceType inferenceType, boolean isMAP) throws IOException {
		
		List<Pair_data> dataPairs = IO.readDataPairs(dataTreePath);
		List<Pair_truth> truthPairs = IO.readTruthPairs(truthTreePath);
		
		List<InGraph> inGraphs = InGraphHelper.makeInGraph(dataPairs, modelType);
		
		double modelAccuracy = 0.0;
		
		switch(inferenceType) {
		case JUNCTION_TREE_MP:
			modelAccuracy = getModelAccuracy_JunctionTreeMP(modelType, accuracyType, dataPairs, truthPairs, inGraphs, isMAP);
			break;
		
		case LB:
			modelAccuracy = getModelAccuracy_LB(modelType, accuracyType, dataPairs, truthPairs, inGraphs, isMAP);
			break;
			
		case GIBBS:
			modelAccuracy = getModelAccuracy_Gibbs(modelType, accuracyType, dataPairs, truthPairs, inGraphs);
			break;
		}
		
		return modelAccuracy;
	}
	
	private static double getModelAccuracy_LB(ModelType modelType, AccuracyType accuracyType, 
			List<Pair_data> dataPairs, List<Pair_truth> truthPairs, List<InGraph> inGraphs, boolean isMAP) throws IOException {
		
		Map<InGraph, LB_graph> lb_graph_map = LB_helper.create_LB_graphs(inGraphs, modelType);
		
		List<Pair_truth> mostProbablePairs = new ArrayList<Pair_truth>();
		double loglikelihood = 0.0; //used for AVERAGE_DATASET_LOGLIKELIHOOD
		
		long startTime = System.nanoTime();
		
		for(int i=0;i<dataPairs.size();i++) {
			InGraph inGraph = inGraphs.get(i);
			
			LB_graph lb_graph = lb_graph_map.get(inGraph);
			Map<InGraphNode, Map<Object, List<Object>>> beliefs = LB_helper.runAlgo_calculateBelief(inGraph, lb_graph, modelType, isMAP);
			
			loglikelihood = getModelAccuracy_helper(accuracyType, truthPairs, mostProbablePairs, loglikelihood, i, inGraph, beliefs);
		}
		
		return finalizeModelAccuracy_logLikelihood(accuracyType, dataPairs, truthPairs, mostProbablePairs, loglikelihood, startTime);
	}
	
	
	private static double getModelAccuracy_Gibbs(ModelType modelType, AccuracyType accuracyType, 
			List<Pair_data> dataPairs, List<Pair_truth> truthPairs, List<InGraph> inGraphs) throws IOException {
		
		//Map<InGraph, LB_graph> lb_graph_map = LB_helper.create_LB_graphs(inGraphs, modelType);
		
		List<Pair_truth> mostProbablePairs = new ArrayList<Pair_truth>();
		double loglikelihood = 0.0; //used for AVERAGE_DATASET_LOGLIKELIHOOD
		
		//long startTime = System.nanoTime();
		
		for(int i=0;i<dataPairs.size();i++) {
			InGraph inGraph = inGraphs.get(i);
			
			Gibbs_helper.startWork(inGraph, modelType);
			//LB_graph lb_graph = lb_graph_map.get(inGraph);
			//Map<InGraphNode, Map<Object, List<Object>>> beliefs = LB_helper.runAlgo_calculateBelief(inGraph, lb_graph, modelType, isMAP);
			
			//loglikelihood = getModelAccuracy_helper(accuracyType, truthPairs, mostProbablePairs, loglikelihood, i, inGraph, beliefs);
		}
		
		//return finalizeModelAccuracy_logLikelihood(accuracyType, dataPairs, truthPairs, mostProbablePairs, loglikelihood, startTime);
		return 0.0;
	}

	private static double getModelAccuracy_JunctionTreeMP(ModelType modelType, AccuracyType accuracyType, 
			List<Pair_data> dataPairs, List<Pair_truth> truthPairs, List<InGraph> inGraphs, boolean isMAP) throws IOException {
		
		Map<InGraph, List<CliqueTree>> cliqueTreesMap = CliqueTreeHelper.makeCliqueTrees(inGraphs, modelType);
		
		List<Pair_truth> mostProbablePairs = new ArrayList<Pair_truth>();
		double loglikelihood = 0.0; //used for AVERAGE_DATASET_LOGLIKELIHOOD
		
		long startTime = System.nanoTime();

		for(int i=0;i<dataPairs.size();i++) {
			InGraph inGraph = inGraphs.get(i);
			
			List<CliqueTree> cliqueTrees = cliqueTreesMap.get(inGraph);
			Map<InGraphNode, Map<Object, List<Object>>> beliefs = CliqueTreeHelper.msgPassing_calcBelief(inGraph, modelType, cliqueTrees, isMAP);
			
			loglikelihood = getModelAccuracy_helper(accuracyType, truthPairs, mostProbablePairs, loglikelihood, i, inGraph, beliefs);
		}
		
		return finalizeModelAccuracy_logLikelihood(accuracyType, dataPairs, truthPairs, mostProbablePairs, loglikelihood, startTime);
	}

	private static double getModelAccuracy_helper(AccuracyType accuracyType, List<Pair_truth> truthPairs, List<Pair_truth> mostProbablePairs, 
			double loglikelihood, int i, InGraph inGraph, Map<InGraphNode, Map<Object, List<Object>>> beliefs) {
		if(accuracyType == AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD) {
			Pair_truth pair_truth = truthPairs.get(i);
			
			loglikelihood += getWordProb_log(pair_truth.first, inGraph.nodes_w1, beliefs);
			loglikelihood += getWordProb_log(pair_truth.second, inGraph.nodes_w2, beliefs);
		}
		
		else {
			Pair_truth mostProbablePair = getMostProbablePair(beliefs, inGraph);
			mostProbablePairs.add(mostProbablePair);
		}
		
		return loglikelihood;
	}

	private static double finalizeModelAccuracy_logLikelihood(AccuracyType accuracyType, List<Pair_data> dataPairs,
			List<Pair_truth> truthPairs, List<Pair_truth> mostProbablePairs, double loglikelihood, long startTime) throws IOException {
		
		if(accuracyType == AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD) {
			loglikelihood /= (dataPairs.size()*2);
			return loglikelihood;
		}
		
		else {
			List<String> goldWords = truthPairs2Words(truthPairs);
			List<String> mostProbableWords = truthPairs2Words(mostProbablePairs);
		
			double modelAccuracy = getProbableWordsAccuracy(goldWords, mostProbableWords, accuracyType);
		
			long endTime = System.nanoTime();
			System.out.println("Time taken:- " + (endTime-startTime) + "ns");
		
			return modelAccuracy;
		}
	}
	
	private static double getWordProb_log(String word, InGraphNode[] inGraphNodes,
			Map<InGraphNode, Map<Object, List<Object>>> beliefs) {
		double wordProb = 1.0;

		for(int i=0;i<inGraphNodes.length;i++) {
				InGraphNode inGraphNode = inGraphNodes[i];
				char curr_char = word.charAt(i);
				wordProb += Math.log(getCharacterProb(inGraphNode, curr_char, beliefs));
			}
		
		return wordProb;
	}
	
	private static double getCharacterProb(InGraphNode inGraphNode, char req_char,
			Map<InGraphNode, Map<Object, List<Object>>> beliefs) {

		Map<Object, List<Object>> factorProduct = beliefs.get(inGraphNode);
		List<Object> characterList = factorProduct.get(inGraphNode);
		List<Object> valueList = factorProduct.get("Value");
		
		for(int i=0;i<characterList.size();i++) {
			char curr_char = (char)characterList.get(i);
			if(req_char == curr_char) 
				return (double)valueList.get(i);
		}
		
		return 0.0;
	}
	
	private static Pair_truth getMostProbablePair(Map<InGraphNode, Map<Object, List<Object>>> beliefs, InGraph inGraph) {
		Pair_truth pair_truth = new Pair_truth();
		
		pair_truth.first = getMostProbableWord(beliefs, inGraph.nodes_w1);
		pair_truth.second = getMostProbableWord(beliefs, inGraph.nodes_w2);
		
		return pair_truth;
	}

	private static String getMostProbableWord(
			Map<InGraphNode, Map<Object, List<Object>>> beliefs, InGraphNode[] inGraphNodes) {
		StringBuilder word = new StringBuilder("");
		for (InGraphNode inGraphNode : inGraphNodes) {
			word.append(getMostProbableCharacter(inGraphNode, beliefs));
		}
		return word.toString();
	}
	
	private static char getMostProbableCharacter(InGraphNode inGraphNode, Map<InGraphNode, Map<Object, List<Object>>> beliefs) {
		Map<Object, List<Object>> factorProduct = beliefs.get(inGraphNode);
		
		List<Object> characterList = factorProduct.get(inGraphNode);
		List<Object> valueList = factorProduct.get("Value");
		
		double maxProb = (double)valueList.get(0);
		char bestChar = (char)characterList.get(0);
		
		for(int i=1;i<characterList.size();i++) {
			double prob = (double)valueList.get(i);
			if(maxProb <= prob) {
				maxProb = prob;
				bestChar = (char)characterList.get(i);
			}
		}
		
		return bestChar;
	}
	
	private static List<String> truthPairs2Words(List<Pair_truth> truthPairs) {
		List<String> goldWords = new ArrayList<String>();
		
		for (Pair_truth Pair_truth : truthPairs) {
			goldWords.add(Pair_truth.first);
			goldWords.add(Pair_truth.second);
		}
		
		return goldWords;
 	}
	
	private static double getProbableWordsAccuracy(List<String> goldWords, List<String> probableWords, AccuracyType accuracyType) 
			throws IOException {
		double accuracy = 0.0;
		
		switch(accuracyType){
		case CHARACTER_WISE:
			accuracy = getCharacterWiseAccuracy(goldWords, probableWords);
			break;
		case WORD_WISE:
			accuracy = getWordWiseAccuracy(goldWords, probableWords);
			break;
		case AVERAGE_DATASET_LOGLIKELIHOOD:
			//implemented there itself...no need to coming to this function
			break;
		}
		
		return accuracy;
	}

	private static double getCharacterWiseAccuracy(List<String> goldWords, List<String> probableWords) {
		int similarCharsCount = 0;
		int totalCharsCount = 0;
		
		for(int i=0;i<probableWords.size();i++){
			//System.out.println(goldWords.get(i));//TO-DO: remove
			//System.out.println(probableWords.get(i));//TO-DO: remove
			similarCharsCount += getSimilarCharCount(goldWords.get(i), probableWords.get(i));
			totalCharsCount += goldWords.get(i).length();
		}
		
		double accuracy = ((double)similarCharsCount/totalCharsCount)*100;
		return accuracy;
	}
	
	private static double getWordWiseAccuracy(List<String> goldWords, List<String> probableWords) {
		int similarWordsCount = 0;
		int totalWordsCount = goldWords.size();
		
		for(int i=0;i<probableWords.size();i++){
			if(goldWords.get(i).equals(probableWords.get(i))) similarWordsCount++;
		}
		
		double accuracy = ((double)similarWordsCount/totalWordsCount)*100;
		return accuracy;
	}
	
	private static int getSimilarCharCount(String word1, String word2) {
		int count = 0;
		
		for(int i=0;i<word1.length();i++){
			if(word1.charAt(i) == word2.charAt(i)) count++;
		}
		
		return count;
	}
}

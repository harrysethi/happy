/**
 * 
 */
package worker;

import java.util.ArrayList;
import java.util.List;

import constants.FactorType;
import constants.ModelType;
import constants.WordNumType;
import domain.InGraph;
import domain.InGraphNode;
import domain.InGraphNodeAdjacency;
import domain.Pair_data;

/**
 * @author harinder
 *
 */
public class InGraphHelper {
	public static List<InGraph> makeInGraph(List<Pair_data> dataPairs, ModelType modelType) {
		
		List<InGraph> inGraphs = new ArrayList<InGraph>();
		
		for(Pair_data pair_data : dataPairs) {
			int len1 = pair_data.first.size();
			int len2 = pair_data.second.size();
			
			int id_w1 = 0;
			int id_w2 = 0;
			InGraph inGraph = new InGraph(len1, len2);
			
			for(String imgID : pair_data.first) {
				InGraphNode inGraphNode = new InGraphNode(id_w1++, imgID, WordNumType.WORD_NUM_1);
				inGraph.nodes_w1[id_w1-1] = inGraphNode;
			}
			
			for(String imgID : pair_data.second) {
				InGraphNode inGraphNode = new InGraphNode(id_w2++, imgID, WordNumType.WORD_NUM_2);
				inGraph.nodes_w2[id_w2-1] = inGraphNode;
			}

			switch (modelType) {
			case PAIR_SKIP_MODEL:
				createPairSkipEdges(len1, len2, inGraph);
				// intentionally missed 'break'
			case COMBINED_MODEL:
				createSkipEdges(len1, len2, inGraph);
				// intentionally missed 'break'
			case TRANSITION_MODEL:
				createTransitionEdges(len1, len2, inGraph);
				// intentionally missed 'break'
			case OCR_MODEL:
				//need not do anything
				break;
			}
			inGraphs.add(inGraph);
		}
		
		return inGraphs;
	}

	private static void createPairSkipEdges(int len1, int len2, InGraph inGraph) {
		//pair-skip edges
		for(int i=0; i<len1; i++) {
			for(int j=0; j<len2; j++) {
				InGraphNode first = inGraph.nodes_w1[i];
				InGraphNode second = inGraph.nodes_w2[j];
				
				addSkipAdjencies(inGraph, first, second, FactorType.PAIR_SKIP);
			}
		}
	}

	private static void createSkipEdges(int len1, int len2, InGraph inGraph) {
		//skip edges - word1
		for(int i=0; i<len1-1; i++) {
			for(int j=i+1; j<len1; j++) {
				InGraphNode first = inGraph.nodes_w1[i];
				InGraphNode second = inGraph.nodes_w1[j];
				
				addSkipAdjencies(inGraph, first, second, FactorType.SKIP);
			}
		}
		
		//skip edges - word2
		for(int i=0; i<len2-1; i++) {
			for(int j=i+1; j<len2; j++) {
				InGraphNode first = inGraph.nodes_w2[i];
				InGraphNode second = inGraph.nodes_w2[j];
				
				addSkipAdjencies(inGraph, first, second, FactorType.SKIP);
			}
		}
	}

	private static void createTransitionEdges(int len1, int len2,
			InGraph inGraph) {
		//transition edges - word1
		for(int i=0; i<len1-1; i++) {
			addTransitionAdjencies(inGraph, i, inGraph.nodes_w1);
		}
		
		//transition edges - word2
		for(int i=0; i<len2-1; i++) {
			addTransitionAdjencies(inGraph, i, inGraph.nodes_w2);
		}
	}

	private static void addTransitionAdjencies(InGraph inGraph, int i, InGraphNode[] nodes) {
		InGraphNode first = nodes[i];
		InGraphNode second = nodes[i+1];
		
		InGraphNodeAdjacency inGraphNodeAdjacency_right = new InGraphNodeAdjacency(FactorType.TRANSITION, second);
		InGraphNodeAdjacency inGraphNodeAdjacency_left = new InGraphNodeAdjacency(FactorType.TRANSITION, first);
		
		first.adjList.add(inGraphNodeAdjacency_right);
		second.adjList.add(inGraphNodeAdjacency_left);
	}
	
	private static void addSkipAdjencies(InGraph inGraph, InGraphNode first, InGraphNode second, FactorType factorType) {
		if(!first.imgID.equals(second.imgID)) return;
		
		InGraphNodeAdjacency inGraphNodeAdjacency_right = new InGraphNodeAdjacency(factorType, second);
		InGraphNodeAdjacency inGraphNodeAdjacency_left = new InGraphNodeAdjacency(factorType, first);
		
		first.adjList.add(inGraphNodeAdjacency_right);
		second.adjList.add(inGraphNodeAdjacency_left);
	}
}

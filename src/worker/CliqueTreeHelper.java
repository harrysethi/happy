/**
 * 
 */
package worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import constants.FactorType;
import constants.ModelType;
import constants.OperateType;
import constants.WordNumType;
import domain.CliqueTree;
import domain.CliqueTreeEdge;
import domain.CliqueTreeNode;
import domain.Factor;
import domain.InGraph;
import domain.InGraphNode;
import domain.InGraphNodeAdjacency;
import domain.Message;

/**
 * @author harinder
 *
 */
public class CliqueTreeHelper {
	
	public static Map<InGraph, List<CliqueTree>> makeCliqueTrees(List<InGraph> inGraphs, ModelType modelType) {
		Map<InGraph, List<CliqueTree>> cliqueTreesMap = new HashMap<InGraph, List<CliqueTree>>();
		
		for (InGraph inGraph : inGraphs) {
			List<CliqueTree> cliqueTrees = makeCliqueTree(inGraph, modelType);
			cliqueTreesMap.put(inGraph, cliqueTrees);
		}
		
		return cliqueTreesMap;
	}
	
	public static Map<InGraphNode, Map<Object, List<Object>>> msgPassing_calcBelief(InGraph inGraph,
			ModelType modelType, List<CliqueTree> cliqueTrees, boolean isMAP) {
		messagePassing(inGraph, modelType, cliqueTrees, isMAP);
		return calculateBelief(inGraph, cliqueTrees, modelType, isMAP);
	}
	
	private static Map<InGraphNode, Map<Object, List<Object>>> calculateBelief(InGraph inGraph, 
			List<CliqueTree> cliqueTrees, ModelType modelType, boolean isMAP) {
		Map<InGraphNode, Map<Object, List<Object>>> beliefMap = new HashMap<InGraphNode, Map<Object, List<Object>>>();
		
		CliqueTree cliqueTree1 = cliqueTrees.get(0);
		CliqueTree cliqueTree2 = cliqueTrees.get(0);
		
		if(modelType == ModelType.TRANSITION_MODEL || modelType == ModelType.COMBINED_MODEL)
			cliqueTree2 = cliqueTrees.get(1);
		
		for(InGraphNode inGraphNode : inGraph.nodes_w1) {
			calculateBeliefHelper(beliefMap, cliqueTree1, inGraphNode, isMAP);
		}
		
		for(InGraphNode inGraphNode : inGraph.nodes_w2) {
			calculateBeliefHelper(beliefMap, cliqueTree2, inGraphNode, isMAP);
		}
		
		return beliefMap;
	}

	private static void calculateBeliefHelper(Map<InGraphNode, Map<Object, List<Object>>> beliefMap,
			CliqueTree cliqueTree, InGraphNode inGraphNode, boolean isMAP) {
		
		for(CliqueTreeNode cliqueTreeNode : cliqueTree.nodes) {
			if(!cliqueTreeNode.belongingNodes.contains(inGraphNode)) continue;
			
			Set<InGraphNode> nodesAfterSummingOut = new HashSet<InGraphNode>();
			nodesAfterSummingOut.add(inGraphNode);
			Map<Object, List<Object>> factorProduct_summedOut = new HashMap<Object, List<Object>>();
			FactorHelper.createFactorProduct(factorProduct_summedOut, nodesAfterSummingOut, 0.0);
			
			List<Object> valueList_summedOut = factorProduct_summedOut.get("Value");
			
			FactorHelper.operateTwoFactors(nodesAfterSummingOut, cliqueTreeNode.belongingNodes, factorProduct_summedOut, 
					cliqueTreeNode.factorProduct, valueList_summedOut, OperateType.OPERATE_SUM, isMAP);
			
			beliefMap.put(inGraphNode, factorProduct_summedOut);
			break;
		}
	}
	
	private static void messagePassing(InGraph inGraph, ModelType modelType, List<CliqueTree> cliqueTrees, boolean isMAP) {
		
		for (CliqueTree cliqueTree : cliqueTrees) {
			//no need of message passing in OCR_MODEL
			if(modelType == ModelType.OCR_MODEL || cliqueTree.nodes.size()==1) continue;
			
			CliqueTreeNode root = upwardMessagePassing(cliqueTree, isMAP);
			downwardMessagePassing(root, isMAP);
		}
	}
	
	private static List<CliqueTree> makeCliqueTree(InGraph inGraph, ModelType modelType) {
		List<CliqueTree> cliqueTrees = makeCliqueTree_withoutFactors(inGraph, modelType);
		
		for (CliqueTree cliqueTree : cliqueTrees) {
			assignFactors(cliqueTree, modelType);
			factorMultiplication(cliqueTree);
		}
		
		return cliqueTrees;
	}
	
	private static void downwardMessagePassing(CliqueTreeNode root, boolean isMAP) {
		Queue<CliqueTreeNode> myQ = new LinkedList<CliqueTreeNode>();
		myQ.add(root);
		
		while(!myQ.isEmpty()) {
			CliqueTreeNode cliqueTreeNode = myQ.remove();
			cliqueTreeNode.isVisited = true;
			
			for(CliqueTreeEdge cliqueTreeEdge : cliqueTreeNode.adjList) {
				if(!cliqueTreeEdge.isPresent) continue;
				//if(cliqueTreeEdge.downwardMessage != null) continue;
				if(cliqueTreeEdge.getDest().isVisited) continue;
					
				CliqueTreeNode adjacent = cliqueTreeEdge.getDest();
				Message upMsg = cliqueTreeEdge.upwardMessage;
				
				Map<Object, List<Object>> factorProduct_node_temp = new HashMap<Object, List<Object>>(cliqueTreeNode.factorProduct);
				List<Object> valueList_node_temp = factorProduct_node_temp.get("Value");
				
				//Divide the factorProduct by nodes upward message
				FactorHelper.operateTwoFactors(upMsg.nodes, cliqueTreeNode.belongingNodes, upMsg.factorProduct, 
						factorProduct_node_temp, valueList_node_temp, OperateType.OPERATE_DIVIDE, false);
				
				//sumOut
				InGraphNode nodeToSumOut = getNodeToSumOut(cliqueTreeNode, adjacent);
				
				Set<InGraphNode> nodesAfterSummingOut = new HashSet<InGraphNode>(cliqueTreeNode.belongingNodes);
				nodesAfterSummingOut.remove(nodeToSumOut);
				Map<Object, List<Object>> factorProduct_summedOut = new HashMap<Object, List<Object>>();
				FactorHelper.createFactorProduct(factorProduct_summedOut, nodesAfterSummingOut, 0.0);
				
				List<Object> valueList_summedOut = factorProduct_summedOut.get("Value");
				List<Object> valueList_adjacent = adjacent.factorProduct.get("Value");

				//populate factorTable of nodesAfterSummingOut
				FactorHelper.operateTwoFactors(nodesAfterSummingOut, cliqueTreeNode.belongingNodes, factorProduct_summedOut, 
						factorProduct_node_temp, valueList_summedOut, OperateType.OPERATE_SUM, isMAP);

				//multiply to adjacent
				FactorHelper.operateTwoFactors(nodesAfterSummingOut, adjacent.belongingNodes, factorProduct_summedOut, 
						adjacent.factorProduct, valueList_adjacent, OperateType.OPERATE_MULTIPLY, false);
				
				CliqueTreeEdge backEdge = adjacent.getCliqueTreeEdge(cliqueTreeNode);
				backEdge.downwardMessage = new Message(factorProduct_summedOut, nodesAfterSummingOut);
				//cliqueTreeEdge.downwardMessage = new Message(factorProduct_summedOut, nodesAfterSummingOut);
				
				myQ.add(adjacent);
			}
		}
	}
	
	private static CliqueTreeNode upwardMessagePassing(CliqueTree cliqueTree, boolean isMAP) {
		CliqueTreeNode root = cliqueTree.getNewRoot();
		
		while(root.getAdjacencyCount() != 0) {
			CliqueTreeNode leaf = cliqueTree.getLeaf();
			CliqueTreeEdge edgeFromLeaf = leaf.getEdgeFromLeaf();
			edgeFromLeaf.isBroken = true;
			
			CliqueTreeNode adjacent = edgeFromLeaf.getDest();
			
			//getting node to sum out
			InGraphNode nodeToSumOut = getNodeToSumOut(leaf, adjacent);
			
			//create factorTable of nodesAfterSummingOut
			Set<InGraphNode> nodesAfterSummingOut = new HashSet<InGraphNode>(leaf.belongingNodes);
			nodesAfterSummingOut.remove(nodeToSumOut);
			Map<Object, List<Object>> factorProduct_summedOut = new HashMap<Object, List<Object>>();
			FactorHelper.createFactorProduct(factorProduct_summedOut, nodesAfterSummingOut, 0.0);
			
			List<Object> valueList_adjacent = adjacent.factorProduct.get("Value");
			List<Object> valueList_summedOut = factorProduct_summedOut.get("Value");

			//populate factorTable of nodesAfterSummingOut
			FactorHelper.operateTwoFactors(nodesAfterSummingOut, leaf.belongingNodes, factorProduct_summedOut, leaf.factorProduct,
					valueList_summedOut, OperateType.OPERATE_SUM, isMAP);
			
			//multiply summedOutFactorProduct with the adjacent node
			FactorHelper.operateTwoFactors(nodesAfterSummingOut, adjacent.belongingNodes, factorProduct_summedOut, 
					adjacent.factorProduct, valueList_adjacent, OperateType.OPERATE_MULTIPLY, false);
			
			//backEdge needs to be broken now :)
			CliqueTreeEdge backEdge = adjacent.getCliqueTreeEdge(leaf);
			backEdge.upwardMessage = new Message(factorProduct_summedOut, nodesAfterSummingOut);
			backEdge.isBroken = true;
		}
		
		return root;
	}

	private static InGraphNode getNodeToSumOut(CliqueTreeNode node,
			CliqueTreeNode adjacent) {
		
		Set<InGraphNode> tempBelongingNodesSet = new HashSet<InGraphNode>(node.belongingNodes);
		tempBelongingNodesSet.removeAll(adjacent.belongingNodes);
		InGraphNode nodeToSumOut = new ArrayList<InGraphNode>(tempBelongingNodesSet).get(0);
		
		return nodeToSumOut;
	}

	private static void factorMultiplication(CliqueTree cliqueTree) {
		for(CliqueTreeNode cliqueTreeNode : cliqueTree.nodes) {
			
			FactorHelper.createFactorProduct(cliqueTreeNode.factorProduct, cliqueTreeNode.belongingNodes, 1.0);
			List<Object> valueList = cliqueTreeNode.factorProduct.get("Value");

			for(Factor factor : cliqueTreeNode.factors) {
				List<Object> factorList_specific2Node_1 = cliqueTreeNode.factorProduct.get(factor.inGraphNode1);
				List<Object> factorList_specific2Node_2 = cliqueTreeNode.factorProduct.get(factor.inGraphNode2);
				
				FactorHelper.setValueBasedOnFactor(valueList, factor, factorList_specific2Node_1, factorList_specific2Node_2);
			}
		}
	}

	private static void assignFactors(CliqueTree cliqueTree, ModelType modelType) {
		switch (modelType) {
		case PAIR_SKIP_MODEL:
			assignPairSkipFactors(cliqueTree);
			//intentionally missing 'break'
			
		case COMBINED_MODEL:
			assignSkipFactors(cliqueTree);
			//intentionally missing 'break'
			
		case TRANSITION_MODEL:
			assignTransitionFactors(cliqueTree);
			//intentionally missing 'break'
			
		case OCR_MODEL:
			assignOCRfactors(cliqueTree);
			break;
		}
	}
	
	private static String getKey(InGraphNode belongingNode1,
			InGraphNode belongingNode2) {
		return belongingNode1.getKey() + ":" + belongingNode2.getKey();
	}

	private static void assignPairSkipFactors(CliqueTree cliqueTree) {
		Set<String> pairSkipFactorsAssigned = new HashSet<String>();
		
		for (CliqueTreeNode cliqueTreeNode : cliqueTree.nodes) {
			List<InGraphNode> belongingNodes = new ArrayList<InGraphNode>(cliqueTreeNode.belongingNodes);
			
			for(int i=0; i<belongingNodes.size(); i++) {
				for(int j=0; j<belongingNodes.size(); j++) {
					if(i==j) continue;
					
					InGraphNode belongingNode1 = belongingNodes.get(i);
					InGraphNode belongingNode2 = belongingNodes.get(j);
					
					if(!belongingNode1.imgID.equals(belongingNode2.imgID)) continue;
					if(belongingNode1.wordNumType == belongingNode2.wordNumType) continue;
					
					//there exists a pair_skip factor
					String key1 = getKey(belongingNode1, belongingNode2);
					String key2 = getKey(belongingNode2, belongingNode1);
					if(pairSkipFactorsAssigned.contains(key1) || pairSkipFactorsAssigned.contains(key2)) continue;
					
					pairSkipFactorsAssigned.add(key1);
					Factor factor = new Factor();
					cliqueTreeNode.factors.add(factor);
					factor.inGraphNode1 = belongingNode1;
					factor.inGraphNode2 = belongingNode2;
					
					factor.factorType = FactorType.PAIR_SKIP;
				}
			}
		}
	}

	private static void assignSkipFactors(CliqueTree cliqueTree) {
		Set<String> skipFactorsAssigned = new HashSet<String>();
		
		for (CliqueTreeNode cliqueTreeNode : cliqueTree.nodes) {
			List<InGraphNode> belongingNodes = new ArrayList<InGraphNode>(cliqueTreeNode.belongingNodes);
			
			for(int i=0; i<belongingNodes.size(); i++) {
				for(int j=0; j<belongingNodes.size(); j++) {
					if(i==j) continue;
					
					InGraphNode belongingNode1 = belongingNodes.get(i);
					InGraphNode belongingNode2 = belongingNodes.get(j);
					
					if(!belongingNode1.imgID.equals(belongingNode2.imgID)) continue;
					if(belongingNode1.wordNumType != belongingNode2.wordNumType) continue;
					
					//there exists a skip factor
					String key1 = getKey(belongingNode1, belongingNode2);
					String key2 = getKey(belongingNode2, belongingNode1);
					if(skipFactorsAssigned.contains(key1) || skipFactorsAssigned.contains(key2)) continue;
					
					skipFactorsAssigned.add(key1);
					Factor factor = new Factor();
					cliqueTreeNode.factors.add(factor);
					factor.inGraphNode1 = belongingNode1;
					factor.inGraphNode2 = belongingNode2;
					
					factor.factorType = FactorType.SKIP;
				}
			}
		}
	}

	private static void assignTransitionFactors(CliqueTree cliqueTree) {
		Set<String> transitionFactorsAssigned = new HashSet<String>();
		
		for (CliqueTreeNode cliqueTreeNode : cliqueTree.nodes) {
			List<InGraphNode> belongingNodes = new ArrayList<InGraphNode>(cliqueTreeNode.belongingNodes);
			
			for(int i=0; i<belongingNodes.size(); i++) {
				for(int j=0; j<belongingNodes.size(); j++) {
					if(i==j) continue;
					
					InGraphNode belongingNode1 = belongingNodes.get(i);
					InGraphNode belongingNode2 = belongingNodes.get(j);
					
					if(Math.abs(belongingNode1.nodeID-belongingNode2.nodeID) != 1) continue;
					if(belongingNode1.nodeID > belongingNode2.nodeID) continue; //transition factor is left to right
					if(belongingNode1.wordNumType != belongingNode2.wordNumType) continue;
					//transition factors exist only between nodes of a same word
					
					//there exists a transition factor
					String key1 = getKey(belongingNode1, belongingNode2);
					String key2 = getKey(belongingNode2, belongingNode1);
					if(transitionFactorsAssigned.contains(key1) || transitionFactorsAssigned.contains(key2)) continue;
					
					transitionFactorsAssigned.add(key1);
					Factor factor = new Factor();
					
					factor.factorType = FactorType.TRANSITION;
					factor.inGraphNode1 = belongingNode1;
					factor.inGraphNode2 = belongingNode2;
					
					cliqueTreeNode.factors.add(factor);
				}
			}
		}
	}

	private static void assignOCRfactors(CliqueTree cliqueTree) {
		Set<String> ocrFactorsAssigned = new HashSet<String>();
		
		for (CliqueTreeNode cliqueTreeNode : cliqueTree.nodes) {
			Set<InGraphNode> belongingNodes = cliqueTreeNode.belongingNodes;
			for (InGraphNode inGraphNode : belongingNodes) {
				if(ocrFactorsAssigned.contains(inGraphNode.getKey())) continue;
				
				ocrFactorsAssigned.add(inGraphNode.getKey());
				
				Factor ocrFactor = new Factor();
				cliqueTreeNode.factors.add(ocrFactor);
				ocrFactor.factorType = FactorType.OCR;
				ocrFactor.inGraphNode1 = inGraphNode;
			}
		}
	}

	private static List<CliqueTree> makeCliqueTree_withoutFactors(
			InGraph inGraph, ModelType modelType) {
		List<CliqueTree> cliqueTrees = new ArrayList<CliqueTree>();
		
		switch (modelType) {
		case PAIR_SKIP_MODEL:
			CliqueTree cliqueTree_pairSkipModel = getCliqueTreeRemovingSubsets(inGraph, cliqueTrees, WordNumType.BOTH);
			joinAllEdges_kruskals(cliqueTree_pairSkipModel);
			break;
			
		case COMBINED_MODEL:
			// intentionally missed 'break'
			
		case TRANSITION_MODEL:
			CliqueTree cliqueTree1 = getCliqueTreeRemovingSubsets(inGraph, cliqueTrees, WordNumType.WORD_NUM_1);
			CliqueTree cliqueTree2 = getCliqueTreeRemovingSubsets(inGraph, cliqueTrees, WordNumType.WORD_NUM_2);
			
			joinAllEdges_kruskals(cliqueTree1);
			joinAllEdges_kruskals(cliqueTree2);
			break;
			
		case OCR_MODEL:
			getCliqueTreeRemovingSubsets(inGraph, cliqueTrees, WordNumType.BOTH);
			//returning the completely disconnected clique tree
			break;
		}
		
		return cliqueTrees;
	}

	private static void joinAllEdges_kruskals(CliqueTree cliqueTree) {
		List<CliqueTreeEdge> cliqueTreeEdges = joinAllEdges(cliqueTree.nodes);
		if(cliqueTreeEdges.size()==0) return;
		
		kruskals(cliqueTreeEdges, cliqueTree.nodes);
		backEdgesArePresentAsWell(cliqueTree);
	}

	private static void backEdgesArePresentAsWell(CliqueTree cliqueTree) {
		for(int i=0;i<cliqueTree.nodes.size();i++) {
				CliqueTreeNode cliqueTreeNode = cliqueTree.nodes.get(i);
				for (CliqueTreeEdge cliqueTreeEdge : cliqueTreeNode.adjList) {
					if(!cliqueTreeEdge.isPresent) continue;
					CliqueTreeEdge backEdge = cliqueTreeEdge.getDest().getCliqueTreeEdge(cliqueTreeNode);
					backEdge.isPresent = true;
				}
		}
	}

	private static CliqueTree getCliqueTreeRemovingSubsets(
			InGraph inGraph, List<CliqueTree> cliqueTrees, WordNumType wordNumType) {
		CliqueTree cliqueTree = new CliqueTree();
		cliqueTrees.add(cliqueTree);
		List<CliqueTreeNode> cliqueTreeNodes = getCliqueTreeNodes(inGraph, wordNumType);
		cliqueTree.nodes = cliqueTreeNodes;
		
		removeSubsets(cliqueTreeNodes);
		return cliqueTree;
	}
	
	static class Subset
	{
	    int parent;
	    int rank;
	}
	
	private static int find(Map<Integer, Subset> subsets, int i)
	{
	    // find root and make root as parent of i (path compression)
	    if (subsets.get(i).parent != i)
	        subsets.get(i).parent = find(subsets, subsets.get(i).parent);
	 
	    return subsets.get(i).parent;
	}
	
	private static void union(Map<Integer, Subset> subsets, int x, int y)
	{
	    int xroot = find(subsets, x);
	    int yroot = find(subsets, y);
	 
	    if (subsets.get(xroot).rank < subsets.get(yroot).rank)
	        subsets.get(xroot).parent = yroot;
	    else if (subsets.get(xroot).rank > subsets.get(yroot).rank)
	        subsets.get(yroot).parent = xroot;
	 
	    else
	    {
	        subsets.get(yroot).parent = xroot;
	        subsets.get(xroot).rank++;
	    }
	}
	
	
	private static void kruskals(List<CliqueTreeEdge> cliqueTreeEdges, List<CliqueTreeNode> cliqueTreeNodes) {
		List<CliqueTreeEdge> spanningTreeEdges = new ArrayList<CliqueTreeEdge>();
		
		int maxEdgeWeight = getMaxEdgeWeight(cliqueTreeEdges);
		setEdgeWeight_maxMinus(cliqueTreeEdges, maxEdgeWeight);
		
		Collections.sort(cliqueTreeEdges);
		
		Map<Integer, Subset> subsets = new HashMap<Integer, Subset>();
	    
	    for (CliqueTreeNode cliqueTreeNode : cliqueTreeNodes) {
	    	Subset subset = new Subset();
			subsets.put(cliqueTreeNode.getNodeID(), subset);
			subset.parent = cliqueTreeNode.getNodeID();
			subset.rank = 0;
		}
		
		int edgePtr = 0;
		
		while(spanningTreeEdges.size() < cliqueTreeNodes.size()-1) {
			CliqueTreeEdge nextEdge = cliqueTreeEdges.get(edgePtr++);
			
			int x = find(subsets, nextEdge.getSrc().getNodeID());
	        int y = find(subsets, nextEdge.getDest().getNodeID());
			
	        if (x != y)
	        {
	        	spanningTreeEdges.add(nextEdge);
	        	nextEdge.isPresent = true;
	            union(subsets, x, y);
	        }
			
		}
		
		//return spanningTreeEdges;
	}

	private static void setEdgeWeight_maxMinus(List<CliqueTreeEdge> cliqueTEdges, int maxEdgeWt) {
		for(CliqueTreeEdge cliqueTreeEdge : cliqueTEdges)
			cliqueTreeEdge.edgeWeight_maxMinus = maxEdgeWt - cliqueTreeEdge.getEdgeWeight();
	}

	private static int getMaxEdgeWeight(List<CliqueTreeEdge> cliqueTreeEdges) {
		int maxEdgeWeight = cliqueTreeEdges.get(0).getEdgeWeight();
		for(int i=1;i<cliqueTreeEdges.size();i++) {
			if(maxEdgeWeight<cliqueTreeEdges.get(i).getEdgeWeight()) 
				maxEdgeWeight = cliqueTreeEdges.get(i).getEdgeWeight();
		}
		
		return maxEdgeWeight;
	}
	
	private static List<CliqueTreeEdge> joinAllEdges(List<CliqueTreeNode> cliqueTreeNodes) {
		List<CliqueTreeEdge> cliqueTreeEdges = new ArrayList<CliqueTreeEdge>();
				
		for (int i = 0; i < cliqueTreeNodes.size(); i++) {
			for (int j = i + 1; j < cliqueTreeNodes.size(); j++) {
				
				CliqueTreeNode cliqueTreeNode1 = cliqueTreeNodes.get(i);
				CliqueTreeNode cliqueTreeNode2 = cliqueTreeNodes.get(j);
				
				Set<InGraphNode> intersection = new HashSet<InGraphNode>(cliqueTreeNode1.belongingNodes); // use the copy constructor
				intersection.retainAll(cliqueTreeNode2.belongingNodes);
				int edgeWeight = intersection.size();
				
				CliqueTreeEdge cliqueTreeEdge1 = new CliqueTreeEdge(cliqueTreeNode1, cliqueTreeNode2, edgeWeight);
				CliqueTreeEdge cliqueTreeEdge2 = new CliqueTreeEdge(cliqueTreeNode2, cliqueTreeNode1, edgeWeight);
				
				cliqueTreeEdges.add(cliqueTreeEdge1);
				cliqueTreeNode1.adjList.add(cliqueTreeEdge1);
				cliqueTreeNode2.adjList.add(cliqueTreeEdge2);
			}
		}
		
		return cliqueTreeEdges;
	}
	
	private static void removeSubsets(List<CliqueTreeNode> cliqueTreeNodes) {
		List<CliqueTreeNode> nodesToRemove = new ArrayList<CliqueTreeNode>();
		for(CliqueTreeNode cliqueTreeNode1 : cliqueTreeNodes) {
			for(CliqueTreeNode cliqueTreeNode2 : cliqueTreeNodes) {
				if(cliqueTreeNode1 == cliqueTreeNode2) continue;

				Set<InGraphNode> belongingNodes1 = cliqueTreeNode1.belongingNodes;
				Set<InGraphNode> belongingNodes2 = cliqueTreeNode2.belongingNodes;
				
				Set<InGraphNode> intersection = new HashSet<InGraphNode>(belongingNodes1); // use the copy constructor
				intersection.retainAll(belongingNodes2);
				
				if(intersection.size()==belongingNodes1.size()){
					nodesToRemove.add(cliqueTreeNode1);
					continue;
				}
				
				if(intersection.size()==belongingNodes2.size()){
					nodesToRemove.add(cliqueTreeNode2);
					continue;
				}
			}
		}
		
		cliqueTreeNodes.removeAll(nodesToRemove);
	}
	
	private static List<CliqueTreeNode> getCliqueTreeNodes(InGraph inGraph, WordNumType wordNumType) {
		
		List<CliqueTreeNode> cliqueTreeNodes = new ArrayList<CliqueTreeNode>();
		
		while (true) {
			InGraphNode minFillNode = getMinFillNode(inGraph, wordNumType);
			if(minFillNode == null) break; //no more active nodes - break

			minFillNode.isActive = false;
			CliqueTreeNode cliqueTreeNode = new CliqueTreeNode();
			cliqueTreeNodes.add(cliqueTreeNode);
			
			//adding belonging nodes
			cliqueTreeNode.belongingNodes.add(minFillNode);
			for(InGraphNodeAdjacency inGraphNodeAdjacency : minFillNode.adjList) {
				InGraphNode inGraphNode = inGraphNodeAdjacency.inGraphNode;
				if(!inGraphNode.isActive) continue;
				cliqueTreeNode.belongingNodes.add(inGraphNode);
			}
		}
		
		
		return cliqueTreeNodes;
	}
	
	public static InGraphNode getMinFillNode(InGraph inGraph, WordNumType wordNumType) {
		int minFillEdges = Integer.MAX_VALUE;
		InGraphNode minFillNode = null;
		
		if(wordNumType != WordNumType.WORD_NUM_2) {
			for (int i = 0; i < inGraph.nodes_w1.length; i++) {
				int fillEdgesReq = 0;
				InGraphNode inGraphNode = inGraph.nodes_w1[i];
				if(!inGraphNode.isActive) continue; //we are only interested in active nodes
				
				int adjLen = inGraphNode.adjList.size();
	
				for (int j = 0; j < adjLen - 1; j++) {
					InGraphNode firstNode = inGraphNode.adjList.get(j).inGraphNode;
					if(!firstNode.isActive) continue; //we are only interested in active nodes
					
					for (int k = j + 1; k < adjLen; k++) {
						InGraphNode secondNode = inGraphNode.adjList.get(k).inGraphNode;
						if(!secondNode.isActive) continue;
						
						if(!firstNode.isAdjacent(secondNode)) fillEdgesReq ++;
					}
				}
				
				if(minFillEdges>fillEdgesReq) {
					minFillEdges = fillEdgesReq;
					minFillNode = inGraph.nodes_w1[i];
					
					if(fillEdgesReq == 0) return minFillNode; //We can't have -ve fillEdges..this is min
				}
			}
		}
		
		if(wordNumType != WordNumType.WORD_NUM_1) {
			for (int i = 0; i < inGraph.nodes_w2.length; i++) {
				int fillEdgesReq = 0;
				InGraphNode inGraphNode = inGraph.nodes_w2[i];
				if(!inGraphNode.isActive) continue; //we are only interested in active nodes
				
				int adjLen = inGraphNode.adjList.size();
	
				for (int j = 0; j < adjLen - 1; j++) {
					InGraphNode firstNode = inGraphNode.adjList.get(j).inGraphNode;
					if(!firstNode.isActive) continue; //we are only interested in active nodes
					
					for (int k = j + 1; k < adjLen; k++) {
						InGraphNode secondNode = inGraphNode.adjList.get(k).inGraphNode;
						if(!secondNode.isActive) continue;
						
						if(!firstNode.isAdjacent(secondNode)) fillEdgesReq ++;
					}
				}
				
				if(minFillEdges > fillEdgesReq) {
					minFillEdges = fillEdgesReq;
					minFillNode = inGraph.nodes_w2[i];
					
					if(fillEdgesReq == 0) return minFillNode; //We can't have -ve fillEdges..this is min
				}
			}
		}
	
		return minFillNode;
	}
}

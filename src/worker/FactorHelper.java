/**
 * 
 */
package worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constants.Consts;
import constants.OperateType;
import domain.Factor;
import domain.InGraphNode;
import domain.Potentials;

/**
 * @author harinder
 *
 */
public class FactorHelper {
	
	public static void setValueBasedOnFactor(List<Object> valueList, Factor factor, List<Object> factorList_specific2Node_1,
			List<Object> factorList_specific2Node_2) {
		switch (factor.factorType) {
		case OCR:
			for(int i=0; i<factorList_specific2Node_1.size(); i++) {
				char currChar_ocr = (char)factorList_specific2Node_1.get(i);
				Double prob_ocr = Potentials.getOcrFactor(factor.inGraphNode1.imgID, currChar_ocr);
				
				double currProb_ocr = (double)valueList.get(i);
				valueList.set(i, currProb_ocr*prob_ocr);
			}
			
			break;
			
		case TRANSITION:
			for(int i=0; i<factorList_specific2Node_1.size(); i++) {
				char currChar_transition_1 = (char)factorList_specific2Node_1.get(i);
				char currChar_transition_2 = (char)factorList_specific2Node_2.get(i);
				
				Double prob_transition = Potentials.getTransFactor(currChar_transition_1, currChar_transition_2);
				
				double currProb_transition = (double)valueList.get(i);
				valueList.set(i, currProb_transition*prob_transition);
			}
			
			break;
			
		case SKIP:
			for(int i=0; i<factorList_specific2Node_1.size(); i++) {
				char currChar_skip_1 = (char)factorList_specific2Node_1.get(i);
				char currChar_skip_2 = (char)factorList_specific2Node_2.get(i);
				
				Double prob_skip = Potentials.getSkipFactor(currChar_skip_1, currChar_skip_2);
				
				double currProb_skip = (double)valueList.get(i);
				valueList.set(i, currProb_skip*prob_skip);
			}
			
			break;

		case PAIR_SKIP:
			for(int i=0; i<factorList_specific2Node_1.size(); i++) {
				char currChar_pairSkip_1 = (char)factorList_specific2Node_1.get(i);
				char currChar_pairSkip_2 = (char)factorList_specific2Node_2.get(i);
				
				Double prob_pairSkip = Potentials.getPairSkipFactor(currChar_pairSkip_1, currChar_pairSkip_2);
				
				double currProb_pairSkip = (double)valueList.get(i);
				valueList.set(i, currProb_pairSkip*prob_pairSkip);
			}
			
			break;
		}
	}
	
	public static void normalizeFactorProduct(Map<Object, List<Object>> factorProduct) {
		double sum = 0.0;
		
		List<Object> valueList = factorProduct.get("Value");
		for (Object value : valueList) {
			sum += (double)value;
		}
		
		for (int i=0;i<valueList.size();i++) {
			valueList.set(i, ((double)valueList.get(i))/sum);
		}
	}
	
	public static void createFactorProduct(Map<Object, List<Object>> factorProduct, 
			InGraphNode inGraphNode, double defaultProb) {
		
		checkFactorProductNotNull(factorProduct);
		
		Set<InGraphNode> belongingNodes = new HashSet<InGraphNode>();
		belongingNodes.add(inGraphNode);
		
		addTitlesInFactorProduct(factorProduct, belongingNodes);
		addCharactersInFactorProduct(factorProduct, belongingNodes, defaultProb);
	}

	public static void createFactorProduct(Map<Object, List<Object>> factorProduct, 
			Set<InGraphNode> belongingNodes, double defaultProb) {
		
		checkFactorProductNotNull(factorProduct);
		
		addTitlesInFactorProduct(factorProduct, belongingNodes);
		addCharactersInFactorProduct(factorProduct, belongingNodes, defaultProb);
	}
	
	private static void checkFactorProductNotNull(
			Map<Object, List<Object>> factorProduct) {
		if(factorProduct==null) {
			System.err.println("FactorProduct should not be null over here");
			System.exit(1);
		}
	}

	private static void addTitlesInFactorProduct(Map<Object, List<Object>> factorProduct, 
			Set<InGraphNode> belongingNodes) {
		
		for(InGraphNode inGraphNode : belongingNodes) {
			List<Object> belongingNodeList = new ArrayList<Object>();
			factorProduct.put(inGraphNode, belongingNodeList);
		}
		
		List<Object> valueList = new ArrayList<Object>();
		factorProduct.put("Value", valueList);
	}
	
	private static void addCharactersInFactorProduct(Map<Object, List<Object>> factorProduct, 
			Set<InGraphNode> belongingNodes, double defaultProb) {
		List<InGraphNode> belongingNodesList = new ArrayList<InGraphNode>(belongingNodes);
		addCharactersHelper(factorProduct, belongingNodesList, 0, belongingNodes.size()-1, defaultProb);
	}
	
	private static void addCharactersHelper(Map<Object, List<Object>> factorProduct, 
			List<InGraphNode> belongingNodes, int l_index, int r_index, double defaultProb) {
		if(l_index == belongingNodes.size()) {
			for(int i=0;i<Math.pow(10, l_index);i++)
				factorProduct.get("Value").add(defaultProb);
			return;
		}
		
		for(int k=0;k<Math.pow(10, l_index);k++) {
			for(int i=0;i<10;i++) {
				for(int j=0;j<Math.pow(10, r_index);j++) { 
					factorProduct.get(belongingNodes.get(l_index)).add(Consts.characters[i]);
				}
			}
		}
		addCharactersHelper(factorProduct, belongingNodes, ++l_index, --r_index, defaultProb);
	}
	
	public static void operateTwoFactors(Set<InGraphNode> nodes1, Set<InGraphNode> nodes2,
			Map<Object, List<Object>> factorProduct1, Map<Object, List<Object>> factorProduct2 , 
			List<Object> valueList_to, OperateType opType, boolean isMAP) {
		
		List<Object> valueList1 = factorProduct1.get("Value");
		List<Object> valueList2 = factorProduct2.get("Value");
		
		for(int i=0;i<Math.pow(10, nodes1.size());i++) {
			Map<InGraphNode, Character> key1 = getFactorRowKey(nodes1, factorProduct1, i, nodes1);
			
			double maxProb = 0.0;
			for(int j=0;j<Math.pow(10, nodes2.size());j++) {
				Map<InGraphNode, Character> key2 = getFactorRowKey(nodes2, factorProduct2, j, nodes1);
				
				maxProb = operateTwoFactorsHelper(valueList1, valueList2, valueList_to ,opType, i, key1, j, key2, isMAP, maxProb);
			}
			if(isMAP && opType == OperateType.OPERATE_SUM) valueList_to.set(i, maxProb); //imp: assuming 'sum' is the first node
		}
	}
	
	private static double operateTwoFactorsHelper(List<Object> valueList1, List<Object> valueList2, List<Object> valueList_to, 
			OperateType opType, int i, Map<InGraphNode, Character> key1, int j, Map<InGraphNode, Character> key2, boolean isMAP, double maxProb) {

		if (!key1.equals(key2)) return maxProb;
			
		double prob1 = (double) valueList1.get(i);
		double prob2 = (double) valueList2.get(j);
		
		int index = i;
		if(valueList_to == valueList2) index = j;
			
		switch (opType) {
		case OPERATE_SUM:
			if(!isMAP) valueList_to.set(index, prob2 + prob1);
			else {if(maxProb < prob2) maxProb = prob2;}
			break;
		case OPERATE_MULTIPLY:
			valueList_to.set(index, prob2 * prob1);
			break;
		case OPERATE_DIVIDE:
			valueList_to.set(index, prob2 / prob1);
			break;
		}
		
		return maxProb;
	}

	private static Map<InGraphNode, Character> getFactorRowKey(Set<InGraphNode> nodes, 
			Map<Object, List<Object>> factorProduct, int i, Set<InGraphNode> nodesToInclude) {
		
		Map<InGraphNode, Character> key = new HashMap<InGraphNode, Character>();
		for (InGraphNode inGraphNode : nodes) {
			if(!nodesToInclude.contains(inGraphNode)) continue;
			char c = (char) factorProduct.get(inGraphNode).get(i);
			key.put(inGraphNode, c);
		}
		
		return key;
	}
	
	/*public Factor getFactor(FactorType factorType) {
		Factor factor = null;
		
		switch (factorType) {
		case OCR:
			factor = getOcrFactor();
			break;
		
		case TRANSITION:
			factor = getTransitionFactor();
			break;
		
		case SKIP:
			factor = getSkipFactor();
			break;
		
		case PAIR_SKIP:
			factor = getPairSkipFactor();
			break;
		}
		
		return factor;
	}
	
	

	public static Factor getTransitionFactor(int i, int j, List<InGraphNode> belongingNodes) {
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
	}

	public static Factor getSkipFactor() {

	}

	public static Factor getPairSkipFactor() {

	}
	
	public static Factor getOcrFactor() {

	}*/
}

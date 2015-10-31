/**
 * 
 */
package domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author harinder
 *
 */
public class Potentials {
	private static final String delimeter = ":";

	private static Map<String, Double> ocrPotentialsMap = new HashMap<String, Double>();
	private static Map<String, Double> transPotentialsMap = new HashMap<String, Double>();

	public static void addOcrPotentialEntry(String imageID, String character,
			String prob) {
		Double prob_double = Double.parseDouble(prob);
		Potentials.ocrPotentialsMap.put(imageID + delimeter + character,
				prob_double);
	}

	public static void addTransPotentialEntry(String charI, String charIplus1,
			String value) {
		Double value_double = Double.parseDouble(value);
		Potentials.transPotentialsMap.put(charI + delimeter + charIplus1,
				value_double);
	}

	public static Double getOcrFactor(String imageID, char character) {
		return Potentials.ocrPotentialsMap.get(imageID + delimeter + character);
	}

	public static Double getTransFactor(char charI, char charIplus1) {
		return Potentials.transPotentialsMap
				.get(charI + delimeter + charIplus1);
	}

	public static Double getSkipFactor(char char1, char char2) {
		if (char1 == char2)
			return 5.0;
		return 1.0;
	}
	
	public static Double getPairSkipFactor(char char1, char char2) {
		if (char1 == char2)
			return 5.0;
		return 1.0;
	}
}

/**
 * 
 */
package worker;

import java.util.List;

import constants.Consts;
import constants.ModelType;
import domain.Potentials;
import domain.Z;

/**
 * @author harinder
 *
 */
public class ProbCalc {
	public static double CalculateWordProbability(String word, List<String> imageIDs,
			ModelType modelType) {
		int len = word.length();

		double prob = getWordProbabilityNumerator(word, imageIDs, modelType, len);

		char[] wordArray = new char[len];
		Z z = new Z();
		calculateZ(modelType, len, 0, imageIDs, z, wordArray);
		prob /= z.Z;
		return prob;
	}
	
	public static double getWordProbabilityNumerator(String word, List<String> imageIDs,
			ModelType modelType, int len) {
		double prob = 1.0;

		switch (modelType) {
		case COMBINED_MODEL:
			for (int i = 0; i < len - 1; i++) {
				for (int j = i + 1; j < len; j++) {
					if (imageIDs.get(i).equals(imageIDs.get(j))) {
						prob *= Potentials.getSkipFactor(word.charAt(i),
								word.charAt(j));
					}
				}
			}
			// intentionally omitted "break"

		case TRANSITION_MODEL:
			for (int i = 0; i < len - 1; i++) {
				prob *= Potentials.getTransFactor(word.charAt(i),
						word.charAt(i + 1));
			}
			// intentionally omitted "break"

		case OCR_MODEL:
			for (int i = 0; i < len; i++) {
				prob *= Potentials.getOcrFactor(imageIDs.get(i),
						word.charAt(i));
			}
			break;
		}
		return prob;
	}

	private static void calculateZ(ModelType modelType, int len, int idx,
			List<String> imageIDs, Z z, char[] wordArray) {
		if (idx == len) {
			z.Z += getWordProbabilityNumerator(String.valueOf(wordArray), imageIDs, modelType, len);
			return;
		}

		for (int i = 0; i < Consts.characters.length; i++) {
			wordArray[idx] = Consts.characters[i];
			calculateZ(modelType, len, idx + 1, imageIDs, z, wordArray);
		}
	}
}

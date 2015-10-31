/**
 * 
 */
package test;

import java.io.IOException;

import constants.AccuracyType;
import constants.InferenceType;
import constants.ModelType;
import worker.ModelAccuracy;

/**
 * @author harinder
 *
 */

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("------started------");
		
		readPotentials();
		
		String dataTreePath = "data/OCRdataset-2/data/data-tree.dat";
		String truthTreePath = "data/OCRdataset-2/data/truth-tree.dat";
		
		ModelAccuracy.getModelAccuracy(dataTreePath, truthTreePath, ModelType.PAIR_SKIP_MODEL, AccuracyType.WORD_WISE, InferenceType.GIBBS, false);
		
		System.out.println("------completed------");
	}

	private static void readPotentials() throws IOException {
		String ocrPotentialsPath = "data/OCRdataset-2/potentials/ocr.dat";
		String transPotentialsPath = "data/OCRdataset-2/potentials/trans.dat";

		helper.IO.readPotentials(ocrPotentialsPath, transPotentialsPath);
	}

}

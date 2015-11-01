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
		
		String dataTreePath1 = "data/OCRdataset-2/data/data-tree.dat";
		String truthTreePath1 = "data/OCRdataset-2/data/truth-tree.dat";
		
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath1, truthTreePath1, ModelType.PAIR_SKIP_MODEL, AccuracyType.CHARACTER_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath1, truthTreePath1, ModelType.PAIR_SKIP_MODEL, AccuracyType.WORD_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath1, truthTreePath1, ModelType.PAIR_SKIP_MODEL, AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD, InferenceType.GIBBS, false));
		
		String dataTreePath2 = "data/OCRdataset-2/data/data-treeWS.dat";
		String truthTreePath2 = "data/OCRdataset-2/data/truth-treeWS.dat";
		
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath2, truthTreePath2, ModelType.PAIR_SKIP_MODEL, AccuracyType.CHARACTER_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath2, truthTreePath2, ModelType.PAIR_SKIP_MODEL, AccuracyType.WORD_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath2, truthTreePath2, ModelType.PAIR_SKIP_MODEL, AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD, InferenceType.GIBBS, false));
		
		String dataTreePath3 = "data/OCRdataset-2/data/data-loops.dat";
		String truthTreePath3 = "data/OCRdataset-2/data/truth-loops.dat";
		
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath3, truthTreePath3, ModelType.PAIR_SKIP_MODEL, AccuracyType.CHARACTER_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath3, truthTreePath3, ModelType.PAIR_SKIP_MODEL, AccuracyType.WORD_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath3, truthTreePath3, ModelType.PAIR_SKIP_MODEL, AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD, InferenceType.GIBBS, false));
		
		String dataTreePath4 = "data/OCRdataset-2/data/data-loopsWS.dat";
		String truthTreePath4 = "data/OCRdataset-2/data/truth-loopsWS.dat";
		
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath4, truthTreePath4, ModelType.PAIR_SKIP_MODEL, AccuracyType.CHARACTER_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath4, truthTreePath4, ModelType.PAIR_SKIP_MODEL, AccuracyType.WORD_WISE, InferenceType.GIBBS, false));
		System.out.println(ModelAccuracy.getModelAccuracy(dataTreePath4, truthTreePath4, ModelType.PAIR_SKIP_MODEL, AccuracyType.AVERAGE_DATASET_LOGLIKELIHOOD, InferenceType.GIBBS, false));
		
		System.out.println("------completed------");
	}

	private static void readPotentials() throws IOException {
		String ocrPotentialsPath = "data/OCRdataset-2/potentials/ocr.dat";
		String transPotentialsPath = "data/OCRdataset-2/potentials/trans.dat";

		helper.IO.readPotentials(ocrPotentialsPath, transPotentialsPath);
	}

}

/**
 * 
 */
package test;

import java.io.IOException;

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
		
		
		System.out.println("------completed------");
	}

	private static void readPotentials() throws IOException {
		String ocrPotentialsPath = "data/OCRdataset-2/potentials/ocr.dat";
		String transPotentialsPath = "data/OCRdataset-2/potentials/trans.dat";

		helper.IO.readPotentials(ocrPotentialsPath, transPotentialsPath);
	}

}

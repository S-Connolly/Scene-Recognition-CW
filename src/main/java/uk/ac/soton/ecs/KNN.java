package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import java.io.*;
import java.util.*;

public class KNN
{
	private final int k;

	private Map<String, List<double[]>> dataset;

	public KNN(int k)
	{
		this.k = k;
	}

	/**
	 * Trains calculates all the tiny image vectors for the dataset given
	 * @param trainingDataset The dataset to train on
	 */
	public void train(VFSGroupDataset<FImage> trainingDataset)
	{
		dataset = new HashMap<>();
		for(String groupName : trainingDataset.getGroups())
		{
			List<double[]> group = new ArrayList<>();
			for(FImage image : trainingDataset.get(groupName))
			{
				group.add(unitLengthVector(zeroMeanVector(tinyImage(image))));
			}
			dataset.put(groupName, group);
		}
	}

	public Map<String, String> test(VFSListDataset<FImage> testingDataset)
	{

		Map<String, String> predictions = new HashMap<>();

		String name;
		String pred;
		double distance;
		Double[] kNearest = new Double[k];
		String[] kNearestStrings;
		FImage testImage;
		String testImageName;
		double max = Double.MAX_VALUE;


		for (int i = 0; i < testingDataset.size(); i++) {
			testImage = testingDataset.get(i);
			testImageName = testingDataset.getID(i);
			System.out.println();
			System.out.println("Testing " + testImageName + "		" + i);
			Arrays.fill(kNearest, max);
			kNearestStrings = new String[k];
			Arrays.fill(kNearestStrings, "");

			for(Map.Entry<String, List<double[]>> entry : dataset.entrySet()) {
				name = entry.getKey();

				for (double[] d : entry.getValue()) {
					distance = Math.abs(euclid(d, unitLengthVector(zeroMeanVector(tinyImage(testImage)))));
					addKNearest(distance, kNearest, kNearestStrings, name);
				}
			}

			pred = highestFrequency(kNearestStrings);
			System.out.println("Predicted " + pred);
			predictions.put(testImageName, pred);
		}

		System.out.println("Testing Completed!");
		return predictions;
	}

	/**
	 * Computes the euclidean distance between 2 vectors of the same length
	 * @param d1 Vector A
	 * @param d2 Vector B
	 * @return The euclidean distance between A and B
	 */
	private static double euclid(double[] d1, double[] d2) {
		double distance = 0;
		for (int i = 0; i < d1.length; i++) {
			distance += Math.pow(d1[i]-d2[i], 2);
		}
		return Math.sqrt(distance);
	}

	/**
	 * Crops and resizes the given image into a 256 length vector square
	 * @param image Image to be made tiny
	 * @return The vector of the tiny image
	 */
	private static double[] tinyImage(FImage image)
	{
		int size = Math.min(image.getHeight(), image.getRows());
		FImage square = image.extractCenter(size, size);
		new ResizeProcessor(16f, 16f).processImage(square);

		return square.getDoublePixelVector();
	}

	/**
	 * Creates a zero mean vector from the vector provided
	 * @param vector The vector to be made zero mean
	 * @return The zero mean vector
	 */
	private static double[] zeroMeanVector(double[] vector)
	{
		double mean = 0d;
		for(double value : vector)
		{
			mean += value;
		}
		mean /= vector.length;

		double[] zeroMeanVector = new double[vector.length];
		for(int i = 0; i < zeroMeanVector.length; i++)
		{
			zeroMeanVector[i] = vector[i] - mean;
		}

		return zeroMeanVector;
	}

	/**
	 * Creates a unit length vector from the vector provided
	 * @param vector The vector to be made unit length
	 * @return The unit length vector
	 */
	private static double[] unitLengthVector(double[] vector)
	{
		double length = euclid(vector, new double[vector.length]);

		double[] unitLengthVector = new double[vector.length];
		for(int i = 0; i < unitLengthVector.length; i++)
		{
			unitLengthVector[i] = vector[i] / length;
		}

		return unitLengthVector;
	}

	private static void addKNearest(double input, Double[] kNearest, String[] kNearestStrings, String name) {
		int indexOfLargest = getIndexOfLargest(kNearest);

		if (kNearest[indexOfLargest] > input) {
			kNearest[indexOfLargest] = input;
			kNearestStrings[indexOfLargest] = name;
		}
	}

	private static int getIndexOfLargest(Double[] arr) {
		if ( arr== null || arr.length == 0 ) return -1;

		int largest = 0;
		for ( int i = 1; i < arr.length; i++ ) {
			if ( arr[i] > arr[largest] ) {
				largest = i;
			}
		}
		return largest;
	}

	private static String highestFrequency(String[] kNearestStrings) {

		if (kNearestStrings == null || kNearestStrings.length == 0)
			return "";

		Arrays.sort(kNearestStrings);

		String previous = kNearestStrings[0];
		String popular = kNearestStrings[0];
		int count = 1;
		int maxCount = 1;

		for (int i = 1; i < kNearestStrings.length; i++) {
			if (kNearestStrings[i].equals(previous))
				count++;
			else {
				if (count > maxCount) {
					popular = kNearestStrings[i-1];
					maxCount = count;
				}
				previous = kNearestStrings[i];
				count = 1;
			}
		}

		return count > maxCount ? kNearestStrings[kNearestStrings.length-1] : popular;
	}
}

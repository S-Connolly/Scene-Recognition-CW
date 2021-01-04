package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

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
		int correct = 0;
		int wrong = 0;
		String name;
		double distance;


		for (FImage testImage: testingDataset) {

			for(Map.Entry<String, List<double[]>> entry : dataset.entrySet()) {
				name = entry.getKey();

				for (double[] d : entry.getValue()) {
					distance = euclid(d, unitLengthVector(zeroMeanVector(tinyImage(testImage))));

				}

			}

			
		}
		return new HashMap<>();
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

	public static void main(String[] args) throws FileSystemException
	{
		VFSGroupDataset<FImage> trainingData = Main.loadTrainingData();
		KNN knn = new KNN(10);
		knn.train(trainingData);
	}
}

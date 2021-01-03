package uk.ac.soton.ecs;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.image.FImage;

import java.util.HashMap;
import java.util.Map;

public class KNN
{
	public KNN(int k)
	{

	}

	public void train(GroupedDataset trainingDataset)
	{

	}

	public Map<String, String> test(ListDataset testingDataset)
	{
		return new HashMap<>();
	}

	public static FImage tinyImage(FImage image) {
		double[] result;

		int size = Math.min(image.getHeight(), image.getRows());
		FImage sixteen = image.extractCenter(size,size);
		result = sixteen.getDoublePixelVector();

		return sixteen;
	}
}

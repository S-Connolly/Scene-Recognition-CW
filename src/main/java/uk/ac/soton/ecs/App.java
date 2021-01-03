package uk.ac.soton.ecs;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {

        String trainingFolderPath = new File("training").getAbsolutePath();
        String testingFolderPath = new File("testing").getAbsolutePath();

        List<String> labels = Arrays.asList("bedroom", "Coast", "Forest",
                "Highway", "industrial", "Insidecity", "kitchen",
                "livingroom", "Mountain", "Office", "OpenCountry",
                "store", "Street", "Suburb", "TallBuilding");

    }
}

package ir.ac.ut.fml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import com.google.common.io.Files;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.SimpleConfiguration;
import uk.le.ac.fts.FtsUtils;

public class ClassifyProduct2 {

	private static final String HELP = "h";
	public static final String DIR0 = "dir0";
	public static final String DIR1 = "dir1";
	public static final String DIR2 = "dir2";

	// This class avoids copying repeated configurations to dir2

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			File all_products_dir = new File(line.getOptionValue(DIR0));
			File sample_products_dir = new File(line.getOptionValue(DIR1));
			File new_products_dir = new File(line.getOptionValue(DIR2));

			File[] all_products_files = all_products_dir.listFiles();
			File[] sample_products_files = sample_products_dir.listFiles();

			Set<Set<Feature>> known_product_configurations = new HashSet<>();

			int max_products_count = 1000;
			int count = 0;

			for (File s : sample_products_files) {
				if (s.getName().endsWith("config")) {
					SimpleConfiguration s_config = FtsUtils.getInstance().loadConfiguration(s.getPath());
					Feature[] s_features = s_config.getFeatures();
					Set<Feature> s_features_set = Set.of(s_features);
					known_product_configurations.add(s_features_set); // Add to known features
				}
			}

//			for (Feature[] features : sample_products_features_list) {
//				System.out.println(Arrays.asList(features));
//			}

			for (File c : all_products_files) {
				if (c.getName().endsWith("config") && count < max_products_count) {
					SimpleConfiguration config = FtsUtils.getInstance().loadConfiguration(c.getPath());
					Feature[] features = config.getFeatures();
					Set<Feature> features_set = Set.of(features);

					if (!known_product_configurations.contains(features_set)) {
						File new_file = new File(new_products_dir.getAbsolutePath() + File.separator + c.getName());
						Files.copy(c, new_file);
						known_product_configurations.add(features_set);
						count++;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished!");

	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(HELP, false, "Help menu");
		options.addOption(DIR0, true, "Directory of all products (all config files)");
		options.addOption(DIR1, true, "Directory of sample products (config files)");
		options.addOption(DIR2, true, "Directory of new products (config files)");
		return options;
	}
}

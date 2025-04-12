package ir.ac.ut.fml;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class EvaluateMultipleGeneralizedFFSMs {
	private static final String FM = "fm";
	public static final String DIR1 = "dir1";
	public static final String DIR2 = "dir2";
	private static final String ALPHABET = "alphabet";
	private static final String NO_LOOP = "no_loop";

	public static void main(String[] args) {
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			String fm_string = line.getOptionValue(FM);
			String dir1_string = line.getOptionValue(DIR1);
			String dir2_string = line.getOptionValue(DIR2);
			String alphabet_string = line.getOptionValue(ALPHABET);
			String no_loop_string = line.getOptionValue(NO_LOOP);

			File fm_file = new File(fm_string);
			String out_string = fm_file.getParent() + File.separator + "output_dir";

//			String[] similarity_metrics = { "global_metric", "w2_metric", "bfs_metric" };
			String[] similarity_metrics = { "w2_metric", "bfs_metric" };
			String[] generalization_methods = { "1", "3" };
			for (String similarity_metric : similarity_metrics) {
				for (String generalization_method : generalization_methods) {
					String ffsm_string = dir1_string + File.separator + "ffsm_" + similarity_metric + "_generalized_"
							+ generalization_method + "_LoopsRemoved.txt";

					String[] args_2 = { "-fm", fm_string, "-ffsm", ffsm_string, "-dir", dir2_string, "-out", out_string,
							"-alphabet", alphabet_string, "-no_loop", no_loop_string };

					System.out.println(ffsm_string);

					EvaluateGeneralizedFFSM test = new EvaluateGeneralizedFFSM();
					test.main(args_2);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FM, true, "Feature model");
		options.addOption(DIR1, true, "Directory for FFSMs");
		options.addOption(DIR2, true, "Directory of config files and FSMs");
		options.addOption(ALPHABET, true, "Alphabet of features");
		options.addOption(NO_LOOP, true, "If this option is set to true, self loops will be removed.");
		return options;
	}
}

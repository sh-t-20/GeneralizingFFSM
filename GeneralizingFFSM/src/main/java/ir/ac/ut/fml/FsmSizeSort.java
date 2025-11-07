package ir.ac.ut.fml;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.IConfigurableFSM;

public class FsmSizeSort {
	private static final String FM = "fm";
	public static final String DIR = "dir";

	// Calculates the number of states of the FSMs in a directory, and sorts them according to their sizes in ascending order
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			File feature_model_file = new File(line.getOptionValue(FM));
			IFeatureModel feature_model = FeatureModelManager.load(feature_model_file.toPath()).getObject();

			File dir_1 = new File(line.getOptionValue(DIR));
			System.out.println(dir_1.toString());

			File[] fsm_files = dir_1.listFiles();

			List<Entry<String, Integer>> fsmSizes = new ArrayList<>();

			for (File a : fsm_files) {
				String file_name = a.getName();
				if (file_name.endsWith("txt")) {
					System.out.println("\n" + file_name);
					File fsm_1_file = new File(dir_1, file_name);
					IConfigurableFSM<String, Word<String>> fsm_1 = FeaturedMealyUtils.getInstance()
							.loadProductMachine(fsm_1_file, feature_model);
					int states_count = fsm_1.getStateIDs().size();
					fsmSizes.add(new SimpleEntry<>(file_name, states_count));
					System.out.println("Number of states = " + states_count);
				}
			}

			fsmSizes.sort(Comparator.comparingInt(Entry::getValue));

			System.out.print("{");
			for (int i = 0; i < fsmSizes.size(); i++) {
				String filename = fsmSizes.get(i).getKey();
				String nameWithoutExt = filename.replaceFirst("[.][^.]+$", "");
				String numericPart = nameWithoutExt.replaceFirst("_text$", "");
				int num = Integer.parseInt(numericPart);
				System.out.print(num);

				if (i != fsmSizes.size() - 1) {
					System.out.print(",");
				}
			}
			System.out.println("}");

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FM, true, "Feature model");
		options.addOption(DIR, true, "Directory for FSMs");
		return options;
	}
}

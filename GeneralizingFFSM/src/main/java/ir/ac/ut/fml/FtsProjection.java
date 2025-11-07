package ir.ac.ut.fml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import be.vibes.fexpression.configuration.SimpleConfiguration;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import ir.ac.ut.fml2.FSMExractor;
import ir.ac.ut.fml5.DeterminativeNfsm;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.fts.FtsUtils;

public class FtsProjection {

	private static final String FM = "fm";
	public static final String FFSM = "ffsm";
	public static final String DIR = "dir";
	public static final String OUT = "out";
	private static final String ALPHABET = "alphabet";
	private static final String NO_LOOP = "no_loop";
	private static final String CHECK_SCC = "check_scc";

	private static final String ALL_FEATURES = "All Features";
	
	// An FTS is projected onto the product configurations in the directory specified by the "dir" argument

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

			File fm_file = new File(line.getOptionValue(FM));
			IFeatureModel fm = FeatureModelManager.load(fm_file.toPath()).getObject();

			File fts_file = new File(line.getOptionValue(FFSM));
			FeaturedMealy<String, Word<String>> ffsm = FeaturedMealyUtils.getInstance().loadFeaturedMealy(fts_file, fm);

			List<String> lines = read_file_lines(line.getOptionValue(ALPHABET));
			Map<String, List<String>> features_alphabet = create_and_get_alphabet(lines);

			boolean remove_loops = Boolean.parseBoolean(line.getOptionValue(NO_LOOP));

			File dir_1 = new File(line.getOptionValue(DIR));
			File out_1 = new File(line.getOptionValue(OUT));

			File[] files = dir_1.listFiles();
			for (File a : files) {
				String file_name = a.getName();
				if (file_name.endsWith("config")) {
					int length = args.length;

					// Setting arguments
					String[] args_1 = new String[length];
					for (int i = 0; i < length; i++) {
						args_1[i] = args[i];
					}

					// Projecting FFSM onto the product configuration
					System.out.println("\n" + file_name);
					int file_version = 1;
					DataManagerFactory3 data_manager_factory_3 = new DataManagerFactory3(a.toString());
					data_manager_factory_3.set_arguments(args_1, file_version);

					Map<Object, Boolean> configs = data_manager_factory_3
							.read_and_get_config(features_alphabet.keySet());

					FSMExractor fsm_extractor_1 = new FSMExractor();
					FastNFA<String> nfsm_1 = fsm_extractor_1.exract_fsm_from_ffsm(ffsm, configs, remove_loops);
					
					SimpleConfiguration product_config = FtsUtils.getInstance().loadConfiguration(a.getPath());

					FSMVisualizer3 fsm_visualizer_3 = new FSMVisualizer3();
					fsm_visualizer_3.visualize(nfsm_1, data_manager_factory_3, product_config);
					
					

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished!");
	}

	private static Map<String, List<String>> create_and_get_alphabet(List<String> lines) {
		Map<String, List<String>> alphabet = new HashMap<>();
		for (String line_ : lines) {

			String[] splitted_line = line_.split(": ");
			String feature = splitted_line[0].trim();
			List<String> input_signals = Arrays.asList(splitted_line[1].split(", "));

			if (feature.equals(ALL_FEATURES))
				for (String inp : input_signals)
					alphabet.put(inp, null);

			alphabet.put(feature, input_signals);
		}

		return alphabet;
	}

	private static List<String> read_file_lines(String file_path) {
		List<String> lines = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file_path))) {
			String line_;
			while ((line_ = br.readLine()) != null) {
				lines.add(line_);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FM, true, "Feature model");
		options.addOption(FFSM, true, "FTS or FFSM file");
		options.addOption(DIR, true, "Directory of config files");
		options.addOption(OUT, true, "Output directory");
		options.addOption(ALPHABET, true, "Alphabet of features");
		options.addOption(NO_LOOP, true, "If this option is set to true, self loops will be removed.");
		options.addOption(CHECK_SCC, true, "Consider one scc that root is there");
		return options;
	}

}

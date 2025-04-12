package ir.ac.ut.fml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.prop4j.Node;

import be.vibes.fexpression.Feature;
import be.vibes.fexpression.configuration.SimpleConfiguration;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.SimplifiedTransition;
import uk.le.ac.fts.FtsUtils;

public class ProjectionFFSM1 {
	private static final String HELP = "h";
	private static final String FM = "fm";
	public static final String FFSM = "ffsm";
	public static final String CONFIG = "config";
	public static final String OUT = "out";
	public static final String ALPHABET = "alphabet";

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

			if (line.hasOption(HELP) || !line.hasOption(FM) || !line.hasOption(FFSM) || !line.hasOption(CONFIG)
					|| !line.hasOption(OUT)) {
				formatter.printHelp("Generalize FFSM", options);
				System.exit(0);
			}

			File fm_file = new File(line.getOptionValue(FM));
			IFeatureModel fm = FeatureModelManager.load(fm_file.toPath()).getObject();

			File ffsm_file_1 = new File(line.getOptionValue(FFSM));
			FeaturedMealy<String, Word<String>> ffsm_1 = FeaturedMealyUtils.getInstance().loadFeaturedMealy(ffsm_file_1,
					fm);

			String config_1 = line.getOptionValue(CONFIG);
			SimpleConfiguration product_config = FtsUtils.getInstance().loadConfiguration(config_1);
			Feature[] features = product_config.getFeatures();
			System.out.println(Arrays.toString(features));

			File output_dir = new File(line.getOptionValue(OUT));

			List<Integer> state_list = new ArrayList<>(ffsm_1.getStateIDs());
			int n = state_list.size();

			Set<String> fsm_alphabet_set = new HashSet<>();
			File features_alphabet_file = new File(line.getOptionValue(ALPHABET));
			String features_alphabet_string = ConvertToString(features_alphabet_file);
			Map<String, List<String>> features_alphabet = GetFeaturesAlphabet(features_alphabet_string);

			fsm_alphabet_set.addAll(features_alphabet.get("Common Alphabet"));
			for (Feature e : features) {
				if (features_alphabet.get(e.toString()) != null) {
					fsm_alphabet_set.addAll(features_alphabet.get(e.toString()));
				}
			}
			System.out.println(fsm_alphabet_set);

			Alphabet<String> ffsm_alphabet = ffsm_1.getInputAlphabet();
			System.out.println(ffsm_alphabet);

			CompactMealy<String, Word<String>> fsm_1 = new CompactMealy<String, Word<String>>(ffsm_1.getInputAlphabet(),
					n);
			fsm_1.setInitialState(ffsm_1.getInitialStateIndex());

			List<SimplifiedTransition<String, Word<String>>> t_list = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = ffsm_1
						.getSimplifiedTransitions(state_list.get(i));
				for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : transitions.entrySet()) {
					List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
					for (SimplifiedTransition<String, Word<String>> transition : list_1) {
						t_list.add(transition);
						System.out.println(transition.getSi() + " ," + transition.getIn() + " ," + transition.getOut()
								+ " ," + transition.getSj());

						@SuppressWarnings("unchecked")
						ConditionalTransition<String, Word<String>> c_transition = (ConditionalTransition<String, Word<String>>) transition
								.getTransition();

						Node condition_1 = c_transition.getCondition();

						fsm_1.setTransition(transition.getSi(), transition.getIn(), transition.getSj(),
								transition.getOut());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
	}

	private static Map<String, List<String>> GetFeaturesAlphabet(String f_alphabet_string) {
		// TODO Auto-generated method stub
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(f_alphabet_string);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
//			System.out.println("line:\n" + line);
			List<String> list_1 = Arrays.asList(line.split(":"));
//			System.out.println(list_1);
			String key_1 = list_1.get(0);
			list_1.set(1, list_1.get(1).replaceAll("\\s", ""));
			List<String> Value_1 = Arrays.asList(list_1.get(1).split(","));
			result.put(key_1, Value_1);
		}
		return result;
	}

	private static String ConvertToString(File string_file) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		try (BufferedReader buffer = new BufferedReader(new FileReader(string_file.getAbsolutePath()))) {
			String result = "";
			while ((result = buffer.readLine()) != null) {
//				System.out.println(result);
				builder.append(result).append("\n");
			}
			return builder.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(HELP, false, "Help menu");
		options.addOption(FM, true, "Feature model");
		options.addOption(FFSM, true, "FFSM");
		options.addOption(CONFIG, true, "Product configuration");
		options.addOption(OUT, true, "Output directory");
		options.addOption(ALPHABET, true, "Alphabet of features");
		return options;
	}
}

package ir.ac.ut.fml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

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
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import uk.le.ac.ffsm.ConditionalState;
import uk.le.ac.ffsm.ConditionalTransition;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;
import uk.le.ac.ffsm.SimplifiedTransition;
import uk.le.ac.fts.FtsUtils;

import net.automatalib.automata.fsa.impl.FastNFA;

public class ProjectionFFSM2 {
	private static final String HELP = "h";
	private static final String FM = "fm";
	public static final String FFSM = "ffsm";
	public static final String CONFIG = "config";
	public static final String OUT = "out";
	public static final String ALPHABET = "alphabet";

	public static Map<String, List<String>> read_and_get_features_alphabet(String alphabet) {
		List<String> lines = read_file_lines(alphabet);
		return create_and_get_alphabet(lines);
	}

	private static List<String> read_file_lines(String file_path) {
		List<String> lines = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file_path))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	private static Map<String, List<String>> create_and_get_alphabet(List<String> lines) {
		Map<String, List<String>> alphabet = new HashMap<>();
		for (String line : lines) {
			String[] splitted_line = line.split(": ");
			String feature = splitted_line[0].trim();
			List<String> input_signals = Arrays.asList(splitted_line[1].split(", "));

			alphabet.put(feature, input_signals);
		}
		return alphabet;
	}

	public static Map<Object, Boolean> read_and_get_config(Set<String> features, String config) {
		List<String> config_lines = read_file_lines(config);
		return create_and_get_config(config_lines, features);
	}

	private static Map<Object, Boolean> create_and_get_config(List<String> config_lines, Set<String> features) {
		Map<Object, Boolean> config = new HashMap<>();
		for (String feature : features)
			config.put(feature.toString(), config_lines.contains(feature));
		return config;
	}

	private static Alphabet<String> makeAlphabetIO(FeaturedMealy<String, Word<String>> ffsm_1) {
		Set<String> set_inputAlphabet = new LinkedHashSet<>();
		for (String in : ffsm_1.getInputAlphabet()) {
			set_inputAlphabet.add(in.toString() + "/0");
			set_inputAlphabet.add(in.toString() + "/1");
		}

		for (ConditionalState<ConditionalTransition<String, Word<String>>> cur_state_id : ffsm_1.getStates()) {
			for (String inputIdx : ffsm_1.getInputAlphabet()) {
				for (ConditionalTransition<String, Word<String>> tr : ffsm_1.getTransitions(cur_state_id, inputIdx)) {
					set_inputAlphabet.add(tr.getOutput().toString());
				}
			}
		}
		Alphabet<String> inpAlphabet = Alphabets.fromCollection(set_inputAlphabet);
		return inpAlphabet;
	}

	public static int extractInteger(String input) {
		// Define a regular expression pattern to match integers
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			// Extract the first integer found in the string
			String integerStr = matcher.group();
			// Parse and return the integer
			return Integer.parseInt(integerStr);
		} else {
			// Handle the case when no integer is found in the string
			throw new NumberFormatException("No integer found in the input string.");
		}
	}

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

			String alphabet_1 = line.getOptionValue(ALPHABET);

			List<Integer> state_list = new ArrayList<>(ffsm_1.getStateIDs());
			int n = state_list.size();

			Map<String, List<String>> features_alphabet_ = read_and_get_features_alphabet(alphabet_1);
			Map<Object, Boolean> configs = read_and_get_config(features_alphabet_.keySet(), config_1);
			configs.put("MinePumpSys", true);

			FastNFA<String> nfa = new FastNFA<String>(makeAlphabetIO(ffsm_1));
			Map<FastNFAState, Integer> nfa2model = new LinkedHashMap<>();
			Map<Integer, FastNFAState> model2nfa = new LinkedHashMap<>();

			List<SimplifiedTransition<String, Word<String>>> t_list = new ArrayList<>();
			Integer i = 0;

			for (ConditionalState<ConditionalTransition<String, Word<String>>> state : ffsm_1.getStates()) {
				Integer cur_state_id = ffsm_1.getStateId(state);
				if (!model2nfa.containsKey(cur_state_id)) {
					model2nfa.putIfAbsent(cur_state_id, nfa.addState());
					nfa2model.putIfAbsent(model2nfa.get(cur_state_id), cur_state_id);
				}
				FastNFAState s1 = model2nfa.get(cur_state_id);
				if (i == 0)
					nfa.setInitial(s1, true);

				Map<String, List<SimplifiedTransition<String, Word<String>>>> transitions = ffsm_1
						.getSimplifiedTransitions(state_list.get(i));
				for (Entry<String, List<SimplifiedTransition<String, Word<String>>>> t : transitions.entrySet()) {
					List<SimplifiedTransition<String, Word<String>>> list_1 = t.getValue();
					for (SimplifiedTransition<String, Word<String>> transition : list_1) {
						t_list.add(transition);

						@SuppressWarnings("unchecked")
						ConditionalTransition<String, Word<String>> c_transition = (ConditionalTransition<String, Word<String>>) transition
								.getTransition();

						Integer dst_state_id = transition.getSj();
						if (!model2nfa.containsKey(dst_state_id)) {
							model2nfa.putIfAbsent(dst_state_id, nfa.addState());
							nfa2model.putIfAbsent(model2nfa.get(dst_state_id), dst_state_id);
						}
						FastNFAState s2 = model2nfa.get(dst_state_id);

						Node condition_1 = c_transition.getCondition();
						if (condition_1 == null || condition_1.getValue(configs)) {
							String input_and_output = transition.getIn().toString() + "/"
									+ transition.getOut().toString();
							nfa.addTransition(s1, input_and_output, s2);
						}
					}
				}
				i++;
			}

			Map<FastNFAState, Boolean> visited = nfa.getStates().stream().collect(Collectors.toMap(s -> s, s -> false));

			Queue<FastNFAState> queue = new LinkedList<>();

			FastNFAState s0 = (new ArrayList<>(nfa.getInitialStates())).get(0);
			visited.put(s0, true);
			queue.offer(s0);
			StringBuilder buffer = new StringBuilder();

			BufferedWriter bw = new BufferedWriter(new FileWriter(line.getOptionValue(OUT)));
			while (!queue.isEmpty()) {
				FastNFAState currentVertex = queue.poll();
				for (String in : nfa.getLocalInputs(currentVertex)) {
					Collection<FastNFAState> neighbors = nfa.getTransitions(currentVertex, in);
					for (FastNFAState neighbors_it : neighbors) {

						String si = currentVertex.toString();
						String sj = neighbors_it.toString();
						String input = in.split("/")[0], output = in.split("/")[1];
						buffer.append(String.format("%s -- %s / %s -> %s\n", si, input, output, sj));

						if (!visited.get(neighbors_it)) {
							visited.put(neighbors_it, true);
							queue.offer(neighbors_it);
						}
					}
				}
			}

			String[] lines = buffer.toString().split("\n");

			Comparator<String> customComparator = new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					// Extract the numeric part of each string
					int num1 = extractInteger(s1.split(" ")[0]);
					int num2 = extractInteger(s2.split(" ")[0]);

					// Compare the numeric parts
					return Integer.compare(num1, num2);
				}
			};

			Arrays.sort(lines, customComparator);
			bw.append(String.join("\n", lines));
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
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

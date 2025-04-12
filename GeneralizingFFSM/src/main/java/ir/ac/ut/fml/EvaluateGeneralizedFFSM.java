package ir.ac.ut.fml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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

import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import ir.ac.ut.fml2.FSMExractor;
import ir.ac.ut.fml4.FSMComparator;
import ir.ac.ut.fml5.DeterminativeNfsm;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Word;
import uk.le.ac.ffsm.FeaturedMealy;
import uk.le.ac.ffsm.FeaturedMealyUtils;

public class EvaluateGeneralizedFFSM {

	private static final String FM = "fm";
	public static final String FFSM = "ffsm";
	public static final String DIR = "dir";
	public static final String OUT = "out";
	private static final String ALPHABET = "alphabet";
	private static final String NO_LOOP = "no_loop";
	private static final String CHECK_SCC = "check_scc";

	private static final String ALL_FEATURES = "All Features";

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

			File fm_file = new File(line.getOptionValue(FM));
			IFeatureModel fm = FeatureModelManager.load(fm_file.toPath()).getObject();

			File ffsm_file = new File(line.getOptionValue(FFSM));
			FeaturedMealy<String, Word<String>> ffsm = FeaturedMealyUtils.getInstance().loadFeaturedMealy(ffsm_file,
					fm);

			List<String> lines = read_file_lines(line.getOptionValue(ALPHABET));
			Map<String, List<String>> features_alphabet = create_and_get_alphabet(lines);

			boolean remove_loops = Boolean.parseBoolean(line.getOptionValue(NO_LOOP));

			File dir_1 = new File(line.getOptionValue(DIR));
			File out_1 = new File(line.getOptionValue(OUT));
			File result_dir = new File(out_1.getParent() + File.separator + "results");

			String results = "";
			results += "Configuration,IsEqual,Precision,Recall,F-measure";

			File[] files = dir_1.listFiles();
			for (File a : files) {
				String file_name = a.getName();
				if (file_name.endsWith("config")) {
					int length = args.length;

					results += "\n" + file_name.replaceFirst("[.][^.]+$", "");

					// Setting arguments with check_scc
					String[] args_check_scc = new String[length + 2];
					for (int i = 0; i < length; i++) {
						args_check_scc[i] = args[i];
					}
					args_check_scc[length] = "-check_scc";
					args_check_scc[length + 1] = "true";

					// Projecting FFSM onto the product configuration
					System.out.println("\n" + file_name);
					int file_version = 1;
					DataManagerFactory2 data_manager_factory_1 = new DataManagerFactory2(a.toString());
					data_manager_factory_1.set_arguments(args_check_scc, file_version);

					Map<Object, Boolean> configs = data_manager_factory_1
							.read_and_get_config(features_alphabet.keySet());

					FSMExractor fsm_extractor_1 = new FSMExractor();
					FastNFA<String> nfsm_1 = fsm_extractor_1.exract_fsm_from_ffsm(ffsm, configs, remove_loops);

					FSMVisualizer2 fsm_visualizer_1 = new FSMVisualizer2();
					fsm_visualizer_1.visualize(nfsm_1, data_manager_factory_1);

					String nfsm_file_string_1 = data_manager_factory_1.getPathString();
					System.out.println(nfsm_file_string_1);

					// Setting arguments without check_scc
					String[] args_without_check_scc = new String[length + 2];
					for (int i = 0; i < length; i++) {
						args_without_check_scc[i] = args[i];
					}
					args_without_check_scc[length] = "-check_scc";
					args_without_check_scc[length + 1] = "false";

					// Projecting FFSM onto the product configuration + converting to deterministic
					System.out.println("\ndeterministic version:");
					file_version = 2;
					DataManagerFactory2 data_manager_factory_2 = new DataManagerFactory2(a.toString());
					data_manager_factory_2.set_arguments(args_without_check_scc, file_version);

					FSMExractor fsm_extractor_2 = new FSMExractor();
					FastNFA<String> nfsm_2 = fsm_extractor_2.exract_fsm_from_ffsm(ffsm, configs, remove_loops);

					FSMVisualizer2 fsm_visualizer_2 = new FSMVisualizer2();
					fsm_visualizer_2.visualize(nfsm_2, data_manager_factory_2);

					String nfsm_file_string_2 = data_manager_factory_2.getPathString();
					System.out.println(nfsm_file_string_2);
					String connstructed_fsm_file_string = out_1.getPath() + File.separator
							+ a.getName().replaceFirst("[.][^.]+$", "_projected_v2_deterministic");

					String[] args_determination = { "-nfsm", nfsm_file_string_2, "-out", connstructed_fsm_file_string };
					DeterminativeNfsm determinative_nfsm = new DeterminativeNfsm();
					determinative_nfsm.determinate_nfsm(args_determination);

					connstructed_fsm_file_string = connstructed_fsm_file_string + ".txt";
					System.out.println(connstructed_fsm_file_string);

					// Removing self loops from the available FSM
					String fsm_file_string = a.getParent() + File.separator + a.getName().replaceFirst("[.][^.]+$", "")
							+ "_text.txt";
					File fsm_file_1 = new File(fsm_file_string);
					CompactMealy<String, Word<String>> fsm_1 = Utils.getInstance().loadMealyMachine(fsm_file_1);
					CompactMealy<String, Word<String>> fsm_2 = StateMachineUtils.removeSelfLoopsFSM(fsm_1);
					String header = "";

					String fsm_file_name = fsm_file_1.getName().replaceFirst("[.][^.]+$", "");
					String dot_file_path = out_1.getPath() + File.separator + fsm_file_name + "_LoopsRemoved.dot";
					BufferedWriter bw = new BufferedWriter(new FileWriter(dot_file_path));
					GraphDOT.write(fsm_2, bw);
					bw.close();

					Scanner scanner = new Scanner(new File(fsm_file_1.getPath()));
					scanner.useDelimiter("s0");
					int s0_visited = 0;
					while (scanner.hasNext() && s0_visited == 0) {
						header += scanner.next();
						s0_visited = 1;
					}
					FeaturedMealyUtils.getInstance().saveFSM_kiss(fsm_2,
							new File(out_1.getPath() + File.separator + fsm_file_name + "_LoopsRemoved.txt"), header);

					// Loading the FSM
					String fsm_file_string_2 = out_1.getPath() + File.separator + fsm_file_name + "_LoopsRemoved.txt";
					System.out.println(fsm_file_string_2);

					// Evaluates if the FSMs are equivalent
					String[] args_2 = { "-fsm", fsm_file_string, "-nfsm", connstructed_fsm_file_string };
					int result = 0;
					try {
						ir.ac.ut.fml3.FSMComparator fsm_comparator = new ir.ac.ut.fml3.FSMComparator();
						result = fsm_comparator.start_comparing(args_2);
						results += "," + result;

					} catch (Exception e) {
//						e.printStackTrace();
					}

					// Evaluates the number of added or removed transitions
					if (result != 1) {
						String fm_string = line.getOptionValue(FM);
						String no_loop_string = line.getOptionValue(NO_LOOP);
						String[] args_3 = { "-fsm", fsm_file_string, "-nfsm", connstructed_fsm_file_string, "-fm",
								fm_string, "-no_loop", no_loop_string };

						try {
							ir.ac.ut.fml4.FSMComparator fsm_comparator = new ir.ac.ut.fml4.FSMComparator();
							double[] result_2 = fsm_comparator.start_comparing(args_3);
							for (int i = 0; i < result_2.length; i++) {
								results += "," + result_2[i];
							}
						} catch (Exception e) {
//						e.printStackTrace();
						}
					}
					else {
						results += ",1.0,1.0,1.0";
					}
				}
			}
			System.out.println("\nresults:\n" + results);

			String SPL_dir_name = dir_1.getParentFile().getName();
			int index = SPL_dir_name.indexOf("_");
			String SPL_name = "";
			if (index != -1) {
				SPL_name = SPL_dir_name.substring(0, index);
			}

			String results_file_name = SPL_name + "_" + dir_1.getName() + "_"
					+ ffsm_file.getName().replaceFirst("[.][^.]+$", "");
			File result_file = new File(result_dir, results_file_name + ".csv");
			FileWriter file_stream = new FileWriter(result_file);
			BufferedWriter out = new BufferedWriter(file_stream);
			out.write(results);
			out.close();

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
		options.addOption(FFSM, true, "FFSM");
		options.addOption(DIR, true, "Directory of config files and FSMs");
		options.addOption(OUT, true, "Output directory");
		options.addOption(ALPHABET, true, "Alphabet of features");
		options.addOption(NO_LOOP, true, "If this option is set to true, self loops will be removed.");
		options.addOption(CHECK_SCC, true, "Consider one scc that root is there");
		return options;
	}

}

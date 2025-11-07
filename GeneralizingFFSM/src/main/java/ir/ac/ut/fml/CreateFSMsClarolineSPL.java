package ir.ac.ut.fml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import uk.le.ac.fts.FtsUtils;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.SimpleProjection;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.io.dot.TransitionSystemDotPrinter;
import be.vibes.ts.io.xml.XmlLoaders;
import be.vibes.fexpression.configuration.SimpleConfiguration;

public class CreateFSMsClarolineSPL {

	// Creates FSMs for all configurations available in a folder using the FTS file
	private static final String FTS = "fts";
	private static final String HELP = "h";
	public static final String DIR = "dir";

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

			if (line.hasOption(HELP) || !line.hasOption(FTS) || !line.hasOption(DIR)) {
				formatter.printHelp("CreateFSMs", options);
				System.exit(0);
			}

			String s_fts =
					// "Benchmark_SPL/minepump/fts/minepump.fts" ;
					line.getOptionValue(FTS);

			// load the fts
			File f_fts = new File(s_fts);
			FeaturedTransitionSystem fts = XmlLoaders.loadFeaturedTransitionSystem(f_fts);
			System.out.println(fts.getStatesCount());

			File configs_dir = new File(line.getOptionValue(DIR));

			File[] filesList = configs_dir.listFiles();
			for (int i = 0; i < filesList.length; i++) {
				File configFile = filesList[i];
				String config = configFile.getPath();

				String fileExtension = "";
				String fileName = configFile.getName();
				int j = fileName.lastIndexOf('.');
				if (j >= 0) {
					fileExtension = fileName.substring(j + 1);
				}

				if (fileExtension.equals("config")) {
//					System.out.println(config);
					SimpleConfiguration product = FtsUtils.getInstance().loadConfiguration(config);
					TransitionSystem lts = SimpleProjection.getInstance().project(fts, product);

					String r = "." + fileExtension + "$";

					// save lts.dot
					String s_lts = config.replaceFirst(r, "_lts.dot");
					new TransitionSystemDotPrinter(lts, new PrintStream(new File(s_lts))).printDot();

					String outputTxt = config.replaceFirst(r, "_text.txt");
					convertDotToCustomFormat(s_lts, outputTxt, product);
					
					

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished!");
	}

	private static void convertDotToCustomFormat(String dotFilePath, String outputFilePath,
			SimpleConfiguration product_1) throws Exception {
		String header = FtsUtils.getInstance().simpleConfigurationToString(product_1);
		List<String> lines = Files.readAllLines(Paths.get(dotFilePath));

		// Collect all unique states
		Set<String> states = new LinkedHashSet<>();
		// Store transitions: (source, label, target)
		List<Transition> transitions = new ArrayList<>();

		for (String line : lines) {
			line = line.trim();

			// Parse lines like: state1 -> state2 [ label=" event " ];
			if (line.contains("->") && line.contains("[") && line.contains("label=")) {
				int arrowIdx = line.indexOf("->");
				int bracketIdx = line.indexOf("[");
				int labelStart = line.indexOf("label=\"");
				int labelEnd = line.indexOf("\"", labelStart + 7);

				if (arrowIdx > 0 && bracketIdx > arrowIdx && labelStart > bracketIdx && labelEnd > labelStart) {
					String source = line.substring(0, arrowIdx).trim();
					String target = line.substring(arrowIdx + 2, bracketIdx).trim();
					String label = line.substring(labelStart + 7, labelEnd).trim();

					states.add(source);
					states.add(target);
					transitions.add(new Transition(source, label, target));
				}
			}
		}

		// Map original states to s0, s1, s2, ...
		Map<String, String> stateMap = new HashMap<>();
		int idx = 0;
		for (String state : states) {
			stateMap.put(state, "s" + idx++);
		}

		// Write transitions in custom format to output file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
			writer.write(header);
			writer.newLine();
			for (Transition t : transitions) {
				String src = stateMap.get(t.source);
				String dst = stateMap.get(t.target);
				String label = t.label;

				String outputSymbol = t.source.equals(t.target) ? "/1" : "/0";
				writer.write(src + " -- " + label + outputSymbol + " -> " + dst);
				writer.newLine();
			}
		}
	}

	private static class Transition {
		String source;
		String label;
		String target;

		Transition(String s, String l, String t) {
			source = s;
			label = l;
			target = t;
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FTS, true, "Featured transition system");
		options.addOption(HELP, false, "Help menu");
		options.addOption(DIR, true, "Directory of the config files");
		return options;
	}
}

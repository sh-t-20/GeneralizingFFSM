package ir.ac.ut.fml;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FeatureAlphabetsFromFts {
	public static final String FTS = "fts";
	public static final String DIR = "dir";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// create the command line parser
			CommandLineParser parser = new BasicParser();

			// create the Options
			Options options = createOptions();

			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			File FTS_file = new File(line.getOptionValue(FTS));
			File out_dir = new File(line.getOptionValue(DIR));

			Map<String, Set<String>> featureToInputs = new HashMap<>();
			List<String> lines = Files.readAllLines(Path.of(FTS_file.getAbsolutePath()), StandardCharsets.UTF_8);

			for (String line_1 : lines) {
				if (!line_1.contains("label="))
					continue;

				int labelStart = line_1.indexOf("label=\"") + 7;
				int labelEnd = line_1.indexOf("\"", labelStart);
				String labelContent = line_1.substring(labelStart, labelEnd).trim();

				int lastSlash = labelContent.lastIndexOf("/");
				if (lastSlash == -1)
					continue;

				String input = labelContent.substring(0, lastSlash).trim();
				String featureExpression = labelContent.substring(lastSlash + 1).trim();

				if (featureExpression.equalsIgnoreCase("true")) {
					featureExpression = "Common Alphabet";
				}

				String cleaned = featureExpression.replaceAll("[()]", "");
				String[] features = cleaned.split("\\s*\\|\\||&&\\s*|\\|\\s*|&&\\s*");

				for (String f : features) {
					String feature = f.trim();
					if (!feature.isEmpty()) {
						featureToInputs.computeIfAbsent(feature, k -> new HashSet<>()).add(input);
					}
				}

			}

			Set<String> commonInputs = featureToInputs.get("Common Alphabet");
			if (commonInputs != null) {
				for (Map.Entry<String, Set<String>> entry : featureToInputs.entrySet()) {
					if (!entry.getKey().equals("Common Alphabet")) {
						entry.getValue().removeAll(commonInputs);
					}
				}
			}

//			for (Map.Entry<String, Set<String>> entry : featureToInputs.entrySet()) {
//				System.out.println(entry.getKey() + " ->");
//				for (String input : entry.getValue()) {
//					System.out.println("   " + input);
//				}
//			}

			List<String> features = new ArrayList<>(featureToInputs.keySet());
			features.remove("Common Alphabet");

			String allFeatures = "All Features: " + String.join(", ", features) + "\n";

			String commonAlphabet = "";
			Set<String> commonAlphabetSet = featureToInputs.get("Common Alphabet");
			if (commonAlphabetSet != null) {
				commonAlphabet = "Common Alphabet: " + String.join(", ", commonAlphabetSet) + "\n";
			}

			StringBuilder featureLines = new StringBuilder();
			for (String feature : features) {
				Set<String> inputs = featureToInputs.get(feature);
				if (inputs != null) {
					featureLines.append(feature).append(": ").append(String.join(", ", inputs)).append("\n");
				}
			}

			String result = allFeatures + commonAlphabet + featureLines.toString();
			System.out.println(result);

			Files.writeString(Path.of(out_dir.getAbsolutePath(), "features_alphabet_Claroline.txt"), result,
					StandardCharsets.UTF_8);

			System.out.println("Finished.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FTS, true, "The FTS file");
		options.addOption(DIR, true, "Set output directory");
		return options;
	}
}

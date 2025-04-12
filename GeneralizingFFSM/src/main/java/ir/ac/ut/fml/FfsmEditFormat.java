package ir.ac.ut.fml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class FfsmEditFormat {

	public static final String FFSM = "ffsm";

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

			File ffsm_file = new File(line.getOptionValue(FFSM));
			BufferedReader r = new BufferedReader(new FileReader(ffsm_file));
			String L = r.readLine();
			String ffsm_string_1 = "";
			while (L != null) {
				ffsm_string_1 = ffsm_string_1 + L + System.lineSeparator();
				L = r.readLine();
			}

//			System.out.println(ffsm_string_1);
			String ffsm_string_2 = ffsm_string_1.replaceAll("(?<!-)\\-(?!(-|>))", "not ");
			ffsm_string_2 = ffsm_string_2.replaceAll("&", "and");
			ffsm_string_2 = ffsm_string_2.replaceAll("\\|", "or");
			System.out.println(ffsm_string_2);

			File ffsm_file_2 = new File(ffsm_file.getParent() + File.separator
					+ ffsm_file.getName().replaceFirst("[.][^.]+$", "") + "_FormatEdited.txt");
			FileWriter w = new FileWriter(ffsm_file_2);
			w.write(ffsm_string_2);
			w.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished!");
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(FFSM, true, "FFSM");
		return options;
	}
}

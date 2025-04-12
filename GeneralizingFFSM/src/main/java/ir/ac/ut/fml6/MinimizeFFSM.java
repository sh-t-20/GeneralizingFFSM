package ir.ac.ut.fml6;

public class MinimizeFFSM {
	public static void main(String[] args) {
		FFSMConvertor ffsm_convertor = new FFSMConvertor();
		ffsm_convertor.start_loading_ffsm_and_convert_to_fsm(args);
		System.out.println("Finished.");
	}
}

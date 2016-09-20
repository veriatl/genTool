package incremental;

import datastructure.IncrementalResult;

public class evaluation {

	
	public static void setting1() throws Exception{
		incrementalDriver.init();
		IncrementalResult hsm2fsmRes = incrementalDriver.verify_initial("HSM2FSM");
		
		
		incrementalDriver.inc_verify_post_cache("HSM2FSM", "DB3", "IS2IS", hsm2fsmRes);
		incrementalDriver.inc_verify_post_cache("HSM2FSM", "MB6", "T2TB", hsm2fsmRes);
		incrementalDriver.inc_verify_post_cache("HSM2FSM", "AF2", "RS2RS", hsm2fsmRes);
		incrementalDriver.inc_verify_post_cache("HSM2FSM", "MF6", "T2TB", hsm2fsmRes);
		incrementalDriver.inc_verify_post_cache("HSM2FSM", "DR1", "SM2SM", hsm2fsmRes);
		incrementalDriver.inc_verify_post_cache("HSM2FSM", "AR", "CS2RS", hsm2fsmRes);
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		setting1();

	}

}

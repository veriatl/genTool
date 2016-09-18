package runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;


public class executioner {
	static String proj;
	static String preludePath;
	static String auxuPath;
	static String subgoalPath;
	
	public static void init(String p){
		proj = p;
		auxuPath = String.format("%s/Auxu/", proj);
		subgoalPath = String.format("%s/Sub-goals/", proj);
		preludePath = "Prelude/";
	}
	
	public static List<String> getFiles(String path, String[] ext){
		File directory = new File(path);
		List<String> res = new ArrayList<String>();
		
		for(File f : FileUtils.listFiles(directory, ext, false)){
			res.add(f.getPath());
		}
		
	    return res;
	}
	
	

	
	
	public static VerificationResult verify(String postName, String nodeName) throws Exception {
		String id = String.format("%s-%s-%s", proj,postName,nodeName);
		
		List<String> params = new ArrayList<String>();
		params.add("Boogie");
		params.add("/nologo");
		params.addAll(getFiles(preludePath,new String[]{"bpl"}));
		params.addAll(getFiles(auxuPath,new String[]{"bpl"}));
		
		String postPath = String.format("%s/%s/%s", subgoalPath, postName, nodeName);
		params.add(postPath);
		
		if(!nodeName.equals("original.bpl")){
			String genbyPath = String.format("%s/genBy.bpl", subgoalPath);
			params.add(genbyPath);
		}
		
		
		String[] args = params.toArray(new String[0]);
		
		long start = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec(args);
        p.waitFor();
        long end = System.currentTimeMillis();
        
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        boolean res = false;
        
        String line;
        while ((line = input.readLine()) != null) {
           if(line.indexOf(", 0 errors")!=-1){
        	 res = true;
        	 break;
           }
        }

        input.close();
        return new VerificationResult(id, res, end-start);
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		init("HSM2FSM");
		System.out.print(verify("fsm_transition_src_multi_lower", "original.bpl"));
		
	}
}

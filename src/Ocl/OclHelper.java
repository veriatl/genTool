package Ocl;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;

public class OclHelper {

	
	public static boolean Equal(EObject e1, EObject e2){
		
		return Printer.print(e1).equals(Printer.print(e2));
		
	}
	
}

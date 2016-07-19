package metamodel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.m2m.atl.emftvm.EmftvmFactory;
import org.eclipse.m2m.atl.emftvm.Metamodel;

public class EMFLoader {

	public static EPackage loadEcore(String metamodelPath) throws Exception{
		// Load metamodels
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
	
		Metamodel metaModel = EmftvmFactory.eINSTANCE.createMetamodel();
		metaModel.setResource(rs.getResource(URI.createURI(metamodelPath), true));
		
		Resource r = rs.getResource(URI.createURI(metamodelPath), true);
		EObject eObject = r.getContents().get(0);
		if (eObject instanceof EPackage) {
		    EPackage p = (EPackage)eObject;
		    return p;
		}
		
		throw new Exception("reading metamodel fails hard! abort...");
		
	}
	
	public static Map<String, String> readParantInfo(EPackage mm) throws Exception{
		
		Map<String, String> info = new HashMap<String, String>();
		
		
		for(EClassifier cl : mm.getEClassifiers()){
			
			if(cl instanceof EClass){
				EClass clazz = (EClass)cl;
				
				for(EClass sp : clazz.getESuperTypes()){
					
					info.put(cl.getName(), sp.getName());
				}			
			}
			
		}
	
		return info;
	}
	
	
	public static boolean isSubtype(String sub, String sup, EPackage tarmm) throws Exception{
		Map<String, String> inheirtence = readParantInfo(tarmm);
		
		if(!inheirtence.containsKey(sub)){
			return false;
		}else{
			return inheirtence.get(sub).equals(sup);
		}
		
		
	}
	
	
}

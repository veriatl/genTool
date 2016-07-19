package transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.m2m.atl.emftvm.EmftvmFactory;
import org.eclipse.m2m.atl.emftvm.ExecEnv;
import org.eclipse.m2m.atl.emftvm.Metamodel;
import org.eclipse.m2m.atl.emftvm.OutputRuleElement;
import org.eclipse.m2m.atl.emftvm.Rule;
import org.eclipse.m2m.atl.emftvm.impl.resource.EMFTVMResourceFactoryImpl;
import org.eclipse.m2m.atl.emftvm.util.DefaultModuleResolver;

import metamodel.EMFLoader;

public class Trace {

	
	
	public static ExecEnv moduleLoader(String ATLFilePath, String ATLModuleName, 
			String sourceMMPath, String targetMMPath, String sourceId, String targetId)
	{
		ExecEnv env = EmftvmFactory.eINSTANCE.createExecEnv();
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emftvm", new EMFTVMResourceFactoryImpl());
		
		
		
		
		if(sourceId!=""){
			// Load metamodels
			Metamodel metaModel = EmftvmFactory.eINSTANCE.createMetamodel();
			metaModel.setResource(rs.getResource(URI.createURI(sourceMMPath), true));
			env.registerMetaModel(sourceId, metaModel);

			Resource r = rs.getResource(URI.createURI(sourceMMPath), true);
			EObject eObject = r.getContents().get(0);
			if (eObject instanceof EPackage) {
			    EPackage p = (EPackage)eObject;
			    rs.getPackageRegistry().put(p.getNsURI(), p);
			}
		}
	
		if(targetId!=""){
			// Load metamodels
			Metamodel metaModel = EmftvmFactory.eINSTANCE.createMetamodel();
			metaModel.setResource(rs.getResource(URI.createURI(targetMMPath), true));
			env.registerMetaModel(targetId, metaModel);

			Resource r = rs.getResource(URI.createURI(targetMMPath), true);
			EObject eObject = r.getContents().get(0);
			if (eObject instanceof EPackage) {
			    EPackage p = (EPackage)eObject;
			    rs.getPackageRegistry().put(p.getNsURI(), p);
			}
		}
		
		// load emftvm module
		DefaultModuleResolver mr = new DefaultModuleResolver(URI.createFileURI(ATLFilePath).toString(), rs);
		env.loadModule(mr, ATLModuleName);
		
		return env;
		
	}
	
	
	
	//TODO metamodel prefix
	//TODO notgenby
	public static Map<String, ArrayList<String>> getTrace(EPackage tarmm, ExecEnv env) throws Exception{
		
		Map<String, ArrayList<String>> rtn = new HashMap<String, ArrayList<String>>();
		
		for(EClassifier cl : tarmm.getEClassifiers()){
			for(Rule r : env.getRules()){
				
				for(OutputRuleElement output : r.getOutputElements()){
					if(cl.getName().equals(output.getType()) || EMFLoader.isSubtype(output.getType(), cl.getName(), tarmm)){
						if(rtn.containsKey(cl.getName())){
							rtn.get(cl.getName()).add(r.getName());
						}else{
							ArrayList<String> trace = new ArrayList<String>();
							trace.add(r.getName());
							rtn.put(cl.getName(), trace);
						}
					}
				}
			}
		}
		
		return rtn;
	}
	
	
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		
		
		ExecEnv env = moduleLoader(args[0], args[1],args[2], args[3],args[4], args[5]);
		EPackage tarmm = EMFLoader.loadEcore(args[3]);
		System.out.println(getTrace(tarmm, env));

	}

}

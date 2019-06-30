package org.gpsmaster.gpsloader;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author rfu
 * TODO get all available loader classes via reflection
 */
public class GpsLoaderFactory {
	
	// list of supported extensions
	List<String> extensions = new ArrayList<String>(); 
	static List<GpsLoader> loaders = new ArrayList<GpsLoader>();
	
	public GpsLoaderFactory() {
		loaders.clear();
		// TODO build list of available loader classes dynamically
		loaders.add(new GpxLoader());
		loaders.add(new KmlLoader());
		loaders.add(new IgcLoader());
		loaders.add(new ExifLoader());
		loaders.add(new NmeaLoader());
		loaders.add(new CpoLoader());
		// loaders.add(new JaxbLoader());
		// loaders.add("org.gpsmaster.gpsloader.XmlLoader");		
	}
	
	/**
	 * 
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public List<String> getExtensions() {
		getExtensionList();
		return extensions;
	}
	
	/**
	 * returns the loader class which supports the requested extension
	 * @param extension
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public static GpsLoader getLoader(String extension) throws ClassNotFoundException {
		for (GpsLoader loader : loaders) {
			if (loader.getSupportedExtensions().contains(extension.toLowerCase())) {
				return loader;
			}
			if (loader.getSupportedExtensions().contains(extension.toUpperCase())) {
				return loader;
			}

		}
		/*
		List<String> classNames = new ArrayList<String>();
		try {
			classNames = ClassUtils.getResources("org/gpsmaster/fileloader/");
			for (String className : classNames) {
				Class loaderClass = Class.forName("org.gpsmaster.gpsloader.".concat(className));
				GpsLoader loader;
				try {
					loader = (GpsLoader) loaderClass.newInstance();
					if (loader.getSupportedExtensions().contains(extension)) {
						return loader;
					}
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		throw new ClassNotFoundException(extension);			
	}
	
	/**
	 * 
	 * @throws ClassNotFoundException
	 */
	private void getExtensionList() {
		extensions.clear();
		for (GpsLoader loader : loaders) {
			extensions.addAll(loader.getSupportedExtensions());
		}
		// if a ClassNotFoundException is thrown: ignore it.
		// extensions of classes not loadable will not be added.  
	}
	
	
	
}

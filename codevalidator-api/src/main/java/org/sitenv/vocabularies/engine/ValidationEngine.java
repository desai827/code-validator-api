package org.sitenv.vocabularies.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sitenv.vocabularies.constants.VocabularyConstants;
import org.sitenv.vocabularies.data.DisplayNameValidationResult;
import org.sitenv.vocabularies.loader.code.CodeLoader;
import org.sitenv.vocabularies.loader.code.CodeLoaderManager;
import org.sitenv.vocabularies.loader.valueset.ValueSetLoader;
import org.sitenv.vocabularies.loader.valueset.ValueSetLoaderManager;
import org.sitenv.vocabularies.model.CodeModel;
import org.sitenv.vocabularies.model.ValueSetModel;
import org.sitenv.vocabularies.model.VocabularyModelDefinition;
import org.sitenv.vocabularies.repository.VocabularyRepository;
import org.sitenv.vocabularies.watchdog.RepositoryWatchdog;

public abstract class ValidationEngine {
	
	private static Logger logger = Logger.getLogger(ValidationEngine.class);
	private static RepositoryWatchdog codeWatchdog = null;
	private static RepositoryWatchdog valueSetWatchdog = null;
	
	public static RepositoryWatchdog getCodeWatchdogThread()
	{
		return codeWatchdog;
	}
	
	public static RepositoryWatchdog getValueSetWatchdogThread()
	{
		return valueSetWatchdog;
	}
	
	public static boolean isCodeSystemLoaded(String codeSystem) {
		VocabularyRepository ds = VocabularyRepository.getInstance();
		VocabularyModelDefinition vocabulary = null;
		if (codeSystem != null) {
			Map<String, VocabularyModelDefinition> vocabMap = ds.getVocabularyMap();
			
			if (vocabMap != null) {
				vocabulary = vocabMap.get(codeSystem);
			}
		}
		
		return (vocabulary != null);
	}
	
	public static boolean validateValueSetLoaded(String valueSet)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		
		if (valueSet != null  &&  ds != null && ds.getValueSetModelClassList() != null) {
			
			
			for (Class<? extends ValueSetModel> clazz : ds.getValueSetModelClassList())
			{
				Boolean model = ds.valueSetExists(clazz, valueSet);
				
				if (model != null && model)
				{
					return true;
				}
			}
					
		}
		
		return false;
	}
	
	public static DisplayNameValidationResult validateCodeSystem(String codeSystemName, String displayName, String code) {
		String codeSystem = VocabularyConstants.CODE_SYSTEM_MAP.get(codeSystemName);
		DisplayNameValidationResult result = null;
		
		if (codeSystem != null)
		{
			result = validateDisplayNameForCode(codeSystem, displayName, code);
		}
		
		return result;
	}
	
	public static DisplayNameValidationResult validateDisplayNameForCodeByCodeSystemName(String codeSystemName, String displayName, String code) {
		String codeSystem = VocabularyConstants.CODE_SYSTEM_MAP.get(codeSystemName);
		DisplayNameValidationResult result = null;
		
		if (codeSystem != null)
		{
			result = validateDisplayNameForCode(codeSystem, displayName, code);
		}
		
		return result;
	}
	
	public static DisplayNameValidationResult validateDisplayNameForCode(String codeSystem, String displayName, String code) {
		VocabularyRepository ds = VocabularyRepository.getInstance();
		DisplayNameValidationResult result = null;
		
		if (codeSystem != null && code != null &&  ds != null && ds.getVocabularyMap() != null) {
			Map<String, VocabularyModelDefinition> vocabMap = ds.getVocabularyMap();
			
			VocabularyModelDefinition vocab = vocabMap.get(codeSystem);
			
			result = new DisplayNameValidationResult();
			result.setCode(code);
			result.setAnticipatedDisplayName(displayName);
			result.setActualDisplayName(new ArrayList<String>());
			List<? extends CodeModel> results = ds.fetchByCode(vocab.getClazz(), code);
			
			result.setResult(false);
			
			for(CodeModel instance : results)
			{
				
				result.getActualDisplayName().add(instance.getDisplayName());
				if (instance.getDisplayName() != null && instance.getDisplayName().equalsIgnoreCase(displayName))
				{
					// we found a match for the code where the display name matches
					result.setResult(true);
				}
				
			}
			
		}
		
		return result;
	}
	
	public static boolean validateCodeByCodeSystemName(String codeSystemName, String code)
	{
		String codeSystem = VocabularyConstants.CODE_SYSTEM_MAP.get(codeSystemName);
		
		if (codeSystem != null)
		{
			return validateCode(codeSystem, code);
		}
		
		return false;
	}

	public static boolean validateCode(String codeSystem, String code)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		
		if (codeSystem != null && code != null &&  ds != null && ds.getVocabularyMap() != null) {
			Map<String, VocabularyModelDefinition> vocabMap = ds.getVocabularyMap();
			
			VocabularyModelDefinition vocab = vocabMap.get(codeSystem);
			
			List<? extends CodeModel> results = ds.fetchByCode(vocab.getClazz(), code);
			
			if (results != null && results.size() > 0)
			{
				return true; // instance of code found
			}
			
		}
		
		return false;
	}
	
	public static boolean validateDisplayNameByCodeSystemName(String codeSystemName, String displayName)
	{
		String codeSystem = VocabularyConstants.CODE_SYSTEM_MAP.get(codeSystemName);
		
		if (codeSystem != null)
		{
			return validateDisplayName(codeSystem, displayName);
		}
		
		return false;
	}
	
	public static boolean validateDisplayName(String codeSystem, String displayName)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		
		if (codeSystem != null && displayName != null &&  ds != null && ds.getVocabularyMap() != null) {
			Map<String, VocabularyModelDefinition> vocabMap = ds.getVocabularyMap();
			
			VocabularyModelDefinition vocab = vocabMap.get(codeSystem);
			
			List<? extends CodeModel> results = ds.fetchByDisplayName(vocab.getClazz(), displayName);
			
			if (results != null && results.size() > 0)
			{
				return true; // instance of code found
			}
			
		}
		
		return false;
	}
	
	public static List<ValueSetModel> getValueSetCode(String valueSet, String code, String codeSystem)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		List<ValueSetModel> result = null;
		
		if (valueSet != null && code != null &&  ds != null && ds.getValueSetModelClassList() != null) {
			
			
			for (Class<? extends ValueSetModel> clazz : ds.getValueSetModelClassList())
			{
				List<? extends ValueSetModel> modelList = ds.fetchByValueSetAndCode(clazz, valueSet, code);
				
				if (modelList != null)
				{
					if (result == null)
					{
						result = new ArrayList<ValueSetModel>();
					}
					
					result.addAll(modelList);
				}
			}
					
		}
		
		return result;
	}
	
	public static List<? extends CodeModel> getCode(String codeSystem, String code)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		List<? extends CodeModel> results = null;
		
		if (codeSystem != null && code != null &&  ds != null && ds.getVocabularyMap() != null) {
			Map<String, VocabularyModelDefinition> vocabMap = ds.getVocabularyMap();
			
			VocabularyModelDefinition vocab = vocabMap.get(codeSystem);
			
			results = ds.fetchByCode(vocab.getClazz(), code);
			
			
		}
		
		return results;
	}
		
	
	public static boolean validateValueSetCode(String valueSet, String code)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		
		if (valueSet != null && code != null &&  ds != null && ds.getValueSetModelClassList() != null) {
			
			
			for (Class<? extends ValueSetModel> clazz : ds.getValueSetModelClassList())
			{
				List<? extends ValueSetModel> modelList = ds.fetchByValueSetAndCode(clazz, valueSet, code);
				
				if (modelList != null && modelList.size() > 0)
				{
					return true;
				}
			}
					
		}
		
		return false;
	}
	
	public static boolean validateValueSetCodeForCodeSystem(String valueSet, String code, String codeSystem)
	{
		VocabularyRepository ds = VocabularyRepository.getInstance();
		
		if (valueSet != null && code != null &&  ds != null && ds.getValueSetModelClassList() != null) {
			
			
			for (Class<? extends ValueSetModel> clazz : ds.getValueSetModelClassList())
			{
				List<? extends ValueSetModel> modelList = ds.fetchByValueSetCodeSystemAndCode(clazz, valueSet, codeSystem, code);
				
				if (modelList != null && modelList.size() > 0)
				{
					return true;
				}
			}
					
		}
		
		return false;
	}
	
	public static synchronized void initialize(String codeDirectory, String valueSetDirectory, boolean loadAtStartup) throws IOException {
		boolean recursive = true;

		logger.info("Registering Loaders...");
		// register Loaders
		registerLoaders();
		logger.info("Loaders Registered...");
		
		// Validation Engine should load using the primary database (existing). This will kick off the loading of the secondary database and swap configs
		// Once the secondary dB is loaded, the watchdog thread will be initialized to monitor future changes.
		// Putting this initialization code in a separate thread will dramatically speed up the tomcat launch time
		InitializerThread initializer = new InitializerThread();
		
		initializer.setCodeDirectory(codeDirectory);
		initializer.setValueSetDirectory(valueSetDirectory);
		initializer.setRecursive(recursive);
		initializer.setLoadAtStartup(loadAtStartup);
		
		initializer.start();
	}
	
	public static void registerLoaders() {
		try {
			Class.forName("org.sitenv.vocabularies.loader.code.snomed.SnomedLoader");
			Class.forName("org.sitenv.vocabularies.loader.code.loinc.LoincLoader");
			Class.forName("org.sitenv.vocabularies.loader.code.rxnorm.RxNormLoader");
			Class.forName("org.sitenv.vocabularies.loader.code.icd9.Icd9CmDxLoader");
			Class.forName("org.sitenv.vocabularies.loader.code.icd9.Icd9CmSgLoader");
			Class.forName("org.sitenv.vocabularies.loader.code.icd10.Icd10CmLoader");
			Class.forName("org.sitenv.vocabularies.loader.code.icd10.Icd10PcsLoader");
			Class.forName("org.sitenv.vocabularies.loader.valueset.vsac.VsacLoader");
		} catch (ClassNotFoundException e) {
			// TODO: log4j
			logger.error("Error Initializing Loaders", e);
		}
	}
	
	public static void loadValueSetDirectory(String directory) throws IOException
	{
		File dir = new File(directory);
		
		if (dir.isFile())
		{
			logger.debug("Directory to Load is a file and not a directory");
			throw new IOException("Directory to Load is a file and not a directory");
		}
		else
		{
			
			File[] list = dir.listFiles();
			
			for (File file : list)
			{
				loadValueSetFiles(file);
			}
		}
	}
	
	private static void loadCodeFiles(File directory) throws IOException
	{
		if (directory.isDirectory() && !directory.isHidden()) 
		{
			File[] filesToLoad = directory.listFiles();
			String codeSystem = null;
			
			logger.debug("Building Loader for directory: " + directory.getName() + "...");
			CodeLoader loader = CodeLoaderManager.getInstance().buildLoader(directory.getName());
			if (loader != null && filesToLoad != null) {
				logger.debug("Loader built...");
			
				codeSystem = loader.getCodeSystem();
			
				//logger.debug("Loading file: " + loadFile.getAbsolutePath() + "...");
				loader.load(Arrays.asList(filesToLoad));
				
				
				logger.debug("File loaded...");
			}
			else 
			{
				logger.debug("Building of Loader Failed.");
			}
			
			
			
		}
		
		

	}
	
	
	public static void loadCodeDirectory(String directory) throws IOException
	{
		File dir = new File(directory);
		
		if (dir.isFile())
		{
			logger.debug("Directory to Load is a file and not a directory");
			throw new IOException("Directory to Load is a file and not a directory");
		}
		else
		{
			
			File[] list = dir.listFiles();
			
			for (File file : list)
			{
				loadCodeFiles(file);
			}
		}
	}
	
	private static void loadValueSetFiles(File directory) throws IOException
	{
		if (directory.isDirectory() && !directory.isHidden()) 
		{
			File[] filesToLoad = directory.listFiles();
			String valueSet = null;
			
			logger.debug("Building Loader for directory: " + directory.getName() + "...");
			ValueSetLoader loader = ValueSetLoaderManager.getInstance().buildLoader(directory.getName());
			if (loader != null && filesToLoad != null) {
				logger.debug("Loader built...");
			
				valueSet = loader.getValueSetAuthorName();
			
				//logger.debug("Loading file: " + loadFile.getAbsolutePath() + "...");
				loader.load(Arrays.asList(filesToLoad));
				
				
				logger.debug("File loaded...");
			}
			else 
			{
				logger.debug("Building of Loader Failed.");
			}
			
			
			
		}
		
		

	}
	
	private static class InitializerThread extends Thread {
		
		private String codeDirectory = null;
		private String valueSetDirectory = null;
		private boolean recursive = true;
		private boolean loadAtStartup = false;
		
		
		



		public String getCodeDirectory() {
			return codeDirectory;
		}



		public void setCodeDirectory(String codeDirectory) {
			this.codeDirectory = codeDirectory;
		}



		public String getValueSetDirectory() {
			return valueSetDirectory;
		}



		public void setValueSetDirectory(String valueSetDirectory) {
			this.valueSetDirectory = valueSetDirectory;
		}



		public boolean isRecursive() {
			return recursive;
		}



		public void setRecursive(boolean recursive) {
			this.recursive = recursive;
		}
		
		



		public boolean isLoadAtStartup() {
			return loadAtStartup;
		}



		public void setLoadAtStartup(boolean loadAtStartup) {
			this.loadAtStartup = loadAtStartup;
		}



		public void run() {
			
			
			try 
			{
				if (loadAtStartup)
				{
					if (codeDirectory != null && !codeDirectory.trim().equals(""))
					{
						logger.info("Loading vocabularies at: " + codeDirectory + "...");
						loadCodeDirectory(codeDirectory);
						logger.info("Vocabularies loaded...");
					}
					
					if (valueSetDirectory != null && !valueSetDirectory.trim().equals(""))
					{
						logger.info("Loading value sets at: " + valueSetDirectory + "...");
						loadValueSetDirectory(valueSetDirectory);
						logger.info("Value Sets loaded...");
					}	
						
					logger.info("Activating new Vocabularies Map...");
					VocabularyRepository.getInstance().toggleActiveDatabase();
					logger.info("New vocabulary Map Activated...");
					
					if (codeDirectory != null && !codeDirectory.trim().equals(""))
					{
						logger.info("Loading vocabularies to new inactive repository at: " + codeDirectory + "...");
						loadCodeDirectory(codeDirectory);
						logger.info("Vocabularies loaded...");
					}
					
					if (valueSetDirectory != null && !valueSetDirectory.trim().equals(""))
					{
						logger.info("Loading value sets to new inactive repository at: " + valueSetDirectory + "...");
						loadValueSetDirectory(valueSetDirectory);
						logger.info("Value Sets loaded...");
					}
				}
				// recommendation from cwatson: load files back in the primary so both db's are 
				
				logger.info("Starting Vocabulary Watchdog...");
				ValidationEngine.codeWatchdog = new RepositoryWatchdog(this.getCodeDirectory(), this.isRecursive(), false);
				ValidationEngine.codeWatchdog.start();
				logger.info("Vocabulary Watchdog started...");
				
				logger.info("Starting Value Set Watchdog...");
				ValidationEngine.valueSetWatchdog = new RepositoryWatchdog(this.getValueSetDirectory(), this.isRecursive(), false);
				ValidationEngine.valueSetWatchdog.start();
				logger.info("Vocabulary ValueSet started...");
			}
			catch (Exception e)
			{
				logger.error("Failed to load configured vocabulary directory.", e);
			}
			
			// TODO: Perform Validation/Verification, if needed
			Runtime.getRuntime().gc();
			
		}
		
	}

}

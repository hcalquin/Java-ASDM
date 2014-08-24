import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import alma.SubscanIntentMod.SubscanIntent;
import alma.asdm.ASDM;
import alma.asdm.ScanRow;
import alma.asdm.ScanTable;
import alma.asdm.SourceRow;
import alma.asdm.ExecBlockRow;
import alma.asdm.SourceTable;
import alma.asdm.SubscanRow;
import alma.asdm.SubscanTable;
import alma.hla.runtime.asdm.ex.ConversionException;
import alma.hla.runtime.asdm.types.Interval;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;




/**
 * Clase encargada de hacer la conversion ASDM a CSV, obteniendo los datos desde las tablas ASDM
 * 
 * @author aguila
 *
 */

public class ASDMCsvConverter {		
		
		/**
		 * 
		 * @param args Ruta de la carpeta que contiene los datos ASDM (arg[0]) y 
		 * Ruta de la carpeta en la cual se desea almacenar los archivos CSV con los datos Obscore (args[1])
		 * @throws ConversionException
		 * @throws IOException
		 * @throws IllegalAccessException
		 */
        public static void main (String[] args) throws ConversionException, IOException, IllegalAccessException {
        	
        	// validas para eclipse
        	String ASDM_DATA_PATH = args[0];
        	String ASDM_DATA_POR_PROCESAR_PATH = ASDM_DATA_PATH + "PorProcesar/";
        	String ASDM_DATA_PROCESADOS_PATH = ASDM_DATA_PATH + "Procesados/";
    	 	String ASDM_OUTPUT_DATA_PATH = args[1];
          	
    	 	        	
        	String[] listaCarpetas =  FileHandler.listFolder(ASDM_DATA_POR_PROCESAR_PATH);
        	
        	// Caso que existan datos a procesar        	
        	if (listaCarpetas != null ) 
	        {
	        	File asdmDataFolder;
	        
	        	
	        	
	    		// verifica y crea carpeta CSV de output
	    		File folder = new File(ASDM_OUTPUT_DATA_PATH);
	    		if (!folder.exists()) {
	    			folder.mkdirs();    			
	    		}
	    		
	
				
	    		
	    		for (String Folder: listaCarpetas) {
	    			asdmDataFolder = new File (Folder);
	    			
	    			// Nombre del archivo csv de salida
	                String output_csv_file = ASDM_OUTPUT_DATA_PATH + asdmDataFolder.getName() + ".csv";
	                
	                
	                
	                ASDM asdm = ASDM.getFromXML(ASDM_DATA_PATH + ASDM_DATA_POR_PROCESAR_PATH + asdmDataFolder.getName());
	                
	                
	                CSVReader reader = new CSVReader(new FileReader(ASDM_DATA_POR_PROCESAR_PATH + "input.csv"), ' ');
	                CSVWriter writer = new CSVWriter(new FileWriter(output_csv_file), ' ', CSVWriter.NO_QUOTE_CHARACTER);
	               
	    			
	                // lee los datos del archivo input.csv (contiene la seleccion de los datos fov desde los archivos generados por Harvester)
                	List<String[]> csv_input = reader.readAll();
	                
	
	                Double sumExptime;
	                Double vSpeedLight = 300000.0;
	                Double lambda;
	     
	                
	                // Obtencion de SourceTable
	                SourceTable sourceTable = asdm.getSource();
	
	                // Obtencion de ScanTable
	                 ScanTable scanTable = asdm.getScan();                 
	
	                // Obtencion de SubscanTable
	                  SubscanTable subScanTable = asdm.getSubscan();
	                  
	                  
	                ObscoreRow obscoreRow = new ObscoreRow ();
	                String sourceName;  
	                ExecBlockRow execBlockRow; 
	                
	                
	                
	                        
	                for (ScanRow scanRow: scanTable.get()) {
	                	execBlockRow = scanRow.getExecBlockUsingExecBlockId();
	
	               	 	// obs_id 
	                	obscoreRow.setObs_id(execBlockRow.getExecBlockUID().getEntityId().toString());
	                	
	                	
	                	
	                	
	                	
	                	                	
	                	if ( scanRow.isSourceNameExists() ) {																// comprobacion sourceName exista (es opcional en scanTable)
	                		
	                		// cambio de espacios vacios
	                		sourceName = scanRow.getSourceName().replace(" ", "_");
	                		                		
	                    	// target_name 																					// es opcional  en scantable
	                    	obscoreRow.setTarget_name( sourceName );
	                                        	
	                        // s_ra
	                    	// s_dec
	                    	for (SourceRow sourceRow: sourceTable.get()) {													// busca en la tabla source 
	                    		if (sourceRow.getSourceName().equals(sourceName)) {											// si alguno coincide con el sourceName
	                    			
	                    			obscoreRow.setS_ra(sourceRow.getDirection()[0].toString());								// s_ra
	                    			obscoreRow.setS_dec(sourceRow.getDirection()[1].toString());							// s_dec
	                    			
	                    		    break;																					// basta obtener solo uno
	                    			
	                    		}
	                    	}                		
	                	}
	                	                	
	                	
	                	
	                	// s_fov
	                    // em_res_power
	                	
	                	obscoreRow.setS_fov("NULL");				// por defecto
	                    obscoreRow.setEm_res_power("0.0");
	                	
	                	int num_linea = 1;				// primera fila de input.csv tiene los nombres de las columnas
	                	for (String[] linea_csv: csv_input) {
		                	
	                		if (linea_csv[0].equals(obscoreRow.getObs_id())) {
	    	                	
	                			
	    	                	obscoreRow.setS_fov(linea_csv[1]);
	    	                    obscoreRow.setEm_res_power(linea_csv[2]);
	    	                	break;
	                		}

    	                	num_linea++;
	                	}

	                	
                		

                		
	                	
	                	
	                	
	                	
	                		                	
	                    // s_resolution 
	                	lambda = vSpeedLight / execBlockRow.getSBSummaryUsingSBSummaryId().getFrequency();
	                	obscoreRow.setS_resolution( Double.toString(  (1.2 * lambda) /execBlockRow.getBaseRangeMax().get() ));
	                    
	                    
	                	// t_min                	
	                	obscoreRow.setT_min( Double.toString( execBlockRow.getStartTime().getAsDouble(Interval.SECOND) ) );
	
	
	                    // t_max
	                	obscoreRow.setT_max( Double.toString( execBlockRow.getEndTime().getAsDouble(Interval.SECOND) ) );
	
	
	                    // t_exptime
	                	sumExptime = 0.0;                	
	                	
	                		// busqueda de los subscan tal que SubscanIntent = ON_SOURCE
	                	for (SubscanRow subScanRow: subScanTable.get()) {                    	
	                    	
	                    	// si fila subcan pertenece a scan y al execblock
	                    	if ( (subScanRow.getScanNumber() == scanRow.getScanNumber() )  &&  (subScanRow.getExecBlockId().getTagValue() == scanRow.getExecBlockId().getTagValue() )   ){
	                    		
	                    		if (subScanRow.getSubscanIntent() == SubscanIntent.ON_SOURCE  ) {                    			
	                    			sumExptime = sumExptime + (subScanRow.getEndTime().getAsDouble(Interval.SECOND) - subScanRow.getStartTime().getAsDouble(Interval.SECOND) );                    		
	                    		}
	                    	}
	                    }                    
	                    obscoreRow.setT_exptime( Double.toString(sumExptime) );
	
	
	                    
// Por mientras	        // t_resolution														// por mientras igual a t_exptime
	                    obscoreRow.setT_resolution(Double.toString(sumExptime));

	                    
	                    
	                    // em_min
	                    obscoreRow.setEm_min( execBlockRow.getBaseRangeMin().toString());
	
	
	                    // em_max
	                    obscoreRow.setEm_max(execBlockRow.getBaseRangeMax().toString()); 
	                    
	                    
	                    
	                    
	                    
// Por mientras         // pol_states
	                    obscoreRow.setPol_states("NULL");

	                	
// Por mientras         // noise
	                    obscoreRow.setNoise("0.0");

	                	
// Por mientras         // red_shift
	                    obscoreRow.setRedshift("0.0");
	                    
	                    
	                    // fits_name ( campo extra )                    
	                    obscoreRow.setFits_name( asdmDataFolder.getName() + ".fits");
	
	                
	                    // escritura de datos en archivo csv
	                    writer.writeNext(obscoreRow.getObscoreRow());
	                    
	                }
	                
	
	                writer.close();
	                
	                
	
	                // mover a carpeta procesados
	    			FileHandler.renameFolder(asdmDataFolder.getCanonicalPath(), ASDM_DATA_PROCESADOS_PATH);
	    			
	    			
	    			
	    			System.out.println("OK");
	                                
	    		}
	        } 
        	
        	else {
    			System.out.println("No existen datos a procesar");
        	}
	        
        }
}




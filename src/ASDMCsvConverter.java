import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
import au.com.bytecode.opencsv.CSVWriter;

public class ASDMCsvConverter {
	
	 	public static final String ASDM_INPUT_DATA_PATH = "./ASDMData/";
	 	public static final String ASDM_OUTPUT_DATA_PATH = "./CSV/";
	
			
        public static void main (String[] args) throws ConversionException, IllegalAccessException, IOException {
        	
           
    		String[] asdmFoldersPath = listFolderASDMData(ASDM_INPUT_DATA_PATH);
        
    		// verifica y crea carpeta csv de output
    		File folder = new File(ASDM_OUTPUT_DATA_PATH);
    		if (!folder.exists()) {
    			System.out.println("NO EXISTE");
    			folder.mkdirs();
    			
    		}
    		
    		
    		
    		for (String asdmDataFolder: asdmFoldersPath) {
    			
                String csv = ASDM_OUTPUT_DATA_PATH + asdmDataFolder + ".csv";
                
                ASDM asdm = ASDM.getFromXML(ASDM_INPUT_DATA_PATH + asdmDataFolder);
                
                
                CSVWriter writer = new CSVWriter(new FileWriter(csv), ' ', CSVWriter.NO_QUOTE_CHARACTER);
               

                Long sumExptime;
                Double vSpeedLight = 300000.0;
                Double lambda;
     
                
                
                       
                ExecBlockRow execBlockRow;


                // Obtencion de SourceTable
                SourceTable sourceTable = asdm.getSource();



                // Obtencion de ScanTable
                 ScanTable scanTable = asdm.getScan();
                 

                // Obtencion de SubscanTable
                  SubscanTable subScanTable = asdm.getSubscan();
                  
                ObscoreRow obscoreRow = new ObscoreRow ();

                        
                        
                for (ScanRow scanRow: scanTable.get()) {
                	execBlockRow = scanRow.getExecBlockUsingExecBlockId();

               	 	// obs_id 
                	obscoreRow.setObs_id(execBlockRow.getExecBlockUID().getEntityId().toString());
                	
                	
                	lambda = vSpeedLight / execBlockRow.getSBSummaryUsingSBSummaryId().getFrequency();
                	
                	                	

                	String sourceName = scanRow.getSourceName();
                	// target_name 													// es opcional  en scantable
                	obscoreRow.setTarget_name(sourceName);

                                    	
                    // s_ra
                	// s_dec
                	for (SourceRow sourceRow: sourceTable.get()) {													// busca en la tabla source 
                		if (sourceRow.getSourceName().equals(sourceName)) {											// si alguno coincide con el sourceName
                			
                			obscoreRow.setS_ra(sourceRow.getDirection()[0].toString());								// s_ra
                			obscoreRow.setS_dec(sourceRow.getDirection()[1].toString());							// s_dec
                			

                		    break;																					// basta solo encontrar uno
                			
                		}
                	}
                	                	
                	
                    // s_resolution 
                	obscoreRow.setS_resolution( Double.toString(  (1.2 * lambda) /execBlockRow.getBaseRangeMax().get() ));
                    
                    
                	// t_ms_fovin
                	obscoreRow.setT_min(execBlockRow.getStartTime().toString());


                    // t_max
                	obscoreRow.setT_max(execBlockRow.getEndTime().toString() );


                    // t_exptime
                	sumExptime = 0l;
                	
                    for (SubscanRow subScanRow: subScanTable.get()) {
                    	
                    	
                    	// si fila subcan pertenece a scan y al execblock
                    	if ( (subScanRow.getScanNumber() == scanRow.getScanNumber() )  &&  (subScanRow.getExecBlockId().getTagValue() == scanRow.getExecBlockId().getTagValue() )   ){
                    		
                    		if (subScanRow.getSubscanIntent() == SubscanIntent.ON_SOURCE  ) {
                    			
                    			sumExptime = sumExptime + (subScanRow.getEndTime().get() - subScanRow.getStartTime().get() );
                    			
                    		}
                    	}
                    }                    
                    obscoreRow.setT_exptime(Long.toString(sumExptime) );


                    // em_min
                    obscoreRow.setEm_min( execBlockRow.getBaseRangeMin().toString());


                    // em_max
                    obscoreRow.setEm_max(execBlockRow.getBaseRangeMax().toString()); 

                

                    
                    
                    writer.writeNext(obscoreRow.getObscoreRow());
                    
                   
                    
                }
                

                writer.close();
                
                System.out.println("OK");
                 
              
    			
    		}
                
        }   

        
        
        
        
        
        
        
        /*
         * Lee el directorio dado, para encontrar todas las carpetas que existan
         * Carpeta debe contener los datos ASDM
         */
        
        
        private static String [] listFolderASDMData (String Path) throws IOException{
        	
        	ArrayList<String> arrayListFolder = new ArrayList<String>();
        	
        	File Dir = new File(Path);	
            File[] listaArchivos = Dir.listFiles();
            
            for (int i = 0; i < listaArchivos.length; i++) {

                if (listaArchivos[i].isDirectory()) {
                	arrayListFolder.add(listaArchivos[i].getName());
                }
            }
            
            String [] Folders = arrayListFolder.toArray(new String[arrayListFolder.size()]);
            
			return Folders;
        }
}



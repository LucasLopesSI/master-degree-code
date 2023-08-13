/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import main.Main;

/**
 *
 * @author Lucas
 */
public class Patent {
    public String patentID;
    public String patentAbstract;
    public String patentTitle;
    public String assignes;
    public String assigneEntity;
    public String inventors;
    public String application_authority;
    public LinkedList<String> ipcs = new LinkedList<String>();
    public LinkedList<String> references = new LinkedList<String>();
    public LinkedList<String> NPRscientificFields = new LinkedList<String>();
    public LinkedList<String> citedJournals = new LinkedList<String>();
    public static HashMap<String,String> referencesAndRecognizedFields = new HashMap<String,String>();
    public static HashMap<String,String> referencesAndRecognizedJournals = new HashMap<String,String>();
    public String firstBayesianField;
    public String secondBayesianField;
    
    public static void writePatentsInFile(LinkedList<Patent> patents, String filePath) {
      BufferedWriter bw = null;
      try {
            //Specify the file name and path here
            File file = new File(filePath);

            /* This logic will make sure that the file 
             * gets created if it is not present at the
             * specified location*/
             if (!file.exists()) {
                file.createNewFile();
             }

             FileWriter fw = new FileWriter(file);
             bw = new BufferedWriter(fw);
             bw.write("patentID"+"\t"+"patentAbstract"+"\t"+"patentTitle"+"\t"+"ipcs"+"\t"+"references"+"\t"+"NPRscientificFields"+"\t"+"citedJournals"+"\t"+"inventors"+"\t"+"assignes"+"\t"+"firstBayesianField"+"\t"+"secondBayesianField"+"\n");
             for(Patent patent : patents){
                 if(patent.references.size()>0){
                   for(int i =0 ; i < patent.references.size(); i ++){
                       String reference = patent.references.get(i);
                       bw.write(patent.patentID+"\t"+patent.patentAbstract+"\t"+patent.patentTitle+"\t"+patent.ipcs+"\t"+reference+"\t"+patent.referencesAndRecognizedFields.get(reference)+"\t"+patent.referencesAndRecognizedJournals.get(reference)+"\t"+patent.inventors+"\t"+patent.assignes+"\t"+patent.firstBayesianField+"\t"+patent.secondBayesianField+"\n");
                   }
                 }else{
                     bw.write(patent.patentID+"\t"+patent.patentAbstract+"\t"+patent.patentTitle+"\t"+patent.ipcs+"\t"+patent.references+"\t"+patent.NPRscientificFields+"\t"+patent.citedJournals+"\t"+patent.inventors+"\t"+patent.assignes+"\t"+patent.firstBayesianField+"\t"+patent.secondBayesianField+"\n");
                 }
             }
        }catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
   }
   
    
   public static LinkedList<Patent> ReadPatentsInFile2(String filePath, String folderPath) {
      LinkedList<Patent> patents = new LinkedList<Patent>();
      HashMap<String,Patent> idPatents = new HashMap<String,Patent>();
      
      BufferedReader bw = null;
      try {
         File[]files;
         if(folderPath!= null){
            files = new File(folderPath).listFiles();
         }else{
             files = new File[1];
             files[0] = new File(filePath);
         }
         for(File file : files){
            /* This logic will make sure that the file 
             * gets created if it is not present at the
             * specified location*/
             if (!file.exists()) {
                file.createNewFile();
             }

             FileReader fw = new FileReader(file);
             bw = new BufferedReader(fw);
             bw.readLine();
             while(bw.ready()){
                 try{
                   String line = bw.readLine();
                   String [] parameter;
                   if(line.contains("\"")){
                         parameter = line.split("\"");
                         parameter[0] = parameter[0].substring(0,parameter[0].length()-1);
                         parameter[2] = parameter[2].substring(1,parameter[2].length());
                   }else{
                         parameter = line.split(",");
                   }
                   String patentID = parameter[0];
                   if(!idPatents.containsKey(patentID)){

                       Patent patent = new Patent();
                       patent.patentID = parameter[0];
                       patent.patentAbstract = parameter[1];
                       String[] ipcs_aux = parameter[2].replace("[","").replace("]","").split(",  ");
                       for(String ipc : ipcs_aux){
                           if(ipc.length()>4){
                             patent.ipcs.add(ipc);
                           }
                       }

                       idPatents.put(patent.patentID,patent);
                       patents.add(patent);
                   }else{
                       Patent patent = idPatents.get(patentID);
                       String[] ipcs_aux = parameter[2].replace("[","").replace("]","").split(",  ");
                       for(String ipc : ipcs_aux){
                           if(ipc.length()>4){
                             patent.ipcs.add(ipc);
                           }
                       }
                   }
                 }catch(Exception e1){}
             }
         }
      } catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
      return patents;
   }
   
   public static LinkedList<Patent> ReadPatentsInFile(String filePath) {
      LinkedList<Patent> patents = new LinkedList<Patent>();
      HashMap<String,Patent> idPatents = new HashMap<String,Patent>();
      
      BufferedReader bw = null;
      try {
         //Specify the file name and path here
	 File file = new File(filePath);

	 /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
	  if (!file.exists()) {
	     file.createNewFile();
	  }

	  FileReader fw = new FileReader(file);
	  bw = new BufferedReader(fw);
          bw.readLine();
          while(bw.ready()){
              String line = bw.readLine();
              String parameter [] = line.split("\t");
              
              String patentID = parameter[0];
              if(!idPatents.containsKey(patentID)){
                  Patent patent = new Patent();
                  patent.patentID = parameter[0];
                  patent.patentAbstract = parameter[1];
                  patent.patentTitle = parameter[2];
                  String[] ipcs_aux = parameter[3].replace("[","").replace("]","").split(",  ");
                  for(String ipc : ipcs_aux){
                      if(ipc.length()>4){
                        patent.ipcs.add(ipc);
                      }
                  }
                  patent.references.add(parameter[4]);
                  
                  if(!parameter[5].contentEquals("null") && !Main.scientific_areas_exception.contains(parameter[5])){
                    referencesAndRecognizedFields.put(parameter[4], parameter[5]);
                    patent.NPRscientificFields.add(parameter[5]);
                  }
                  if(!parameter[6].contentEquals("null")){
                    referencesAndRecognizedJournals.put(parameter[4], parameter[6]);
                    patent.citedJournals.add(parameter[6]);
                  }
                  
                  if(parameter.length > 8){
                      patent.inventors = parameter[7];
                      patent.assignes = parameter[8];
                      patent.firstBayesianField = parameter[9];
                      patent.secondBayesianField = parameter[10];
                  }
                  idPatents.put(patent.patentID,patent);
                  patents.add(patent);
              }else{
                  Patent patent = idPatents.get(patentID);
                  patent.references.add(parameter[4]);
                  if(!parameter[5].contentEquals("null") && !Main.scientific_areas_exception.contains(parameter[5])){
                    patent.NPRscientificFields.add(parameter[5]);
                  }
                  if(!parameter[6].contentEquals("null") && !Main.scientific_areas_exception.contains(parameter[5])){
                    patent.citedJournals.add(parameter[6]);
                  }
              }
          }
      } catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
      return patents;
   }
   
   public static LinkedList<String> getAbstractsInBERT(String filePath) {
      LinkedList<String> abstracts = new LinkedList<String>();
      
      BufferedReader bw = null;
      try {
         //Specify the file name and path here
	 File file = new File(filePath);

	 /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
	  if (!file.exists()) {
	     file.createNewFile();
	  }

	  FileReader fw = new FileReader(file);
	  bw = new BufferedReader(fw);
          bw.readLine();
          while(bw.ready()){
              String line = bw.readLine();
              String parameter [] = line.split("\t");
              abstracts.add(parameter[1]);
              
          }
      } catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
      return abstracts;
   }
   
   public static LinkedList<String> getAbstractsInBERT2(String filePath, int cont) {
      LinkedList<String> abstracts = new LinkedList<String>();
      
      BufferedReader bw = null;
      try {
         //Specify the file name and path here
	 File file = new File(filePath);

	 /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
	  if (!file.exists()) {
	     file.createNewFile();
	  }

	  FileReader fw = new FileReader(file);
	  bw = new BufferedReader(fw);
          bw.readLine();
          while(bw.ready()){
              String line = bw.readLine();
              String parameter [] = line.split("\t");
              abstracts.add(parameter[cont]);
          }
      } catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
      return abstracts;
   }
   
   public static LinkedList<String> deleteThisFunciton(String filePath, LinkedList<Patent> test) {
      LinkedList<String> abstracts = new LinkedList<String>();
      HashSet<String> abstracts_test = new HashSet<String>();
      for(Patent pat : test){
          abstracts_test.add(pat.patentAbstract);
      }
      
      BufferedReader bw = null;
      try {
         //Specify the file name and path here
	 File file = new File(filePath);

	 /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
	  if (!file.exists()) {
	     file.createNewFile();
	  }

	  FileReader fw = new FileReader(file);
	  bw = new BufferedReader(fw);
          bw.readLine();
          while(bw.ready()){
              String line = bw.readLine();
              String parameter [] = line.split("\t");
              if(abstracts_test.contains(parameter[1])){
                  System.out.println(line);
              }
              
          }
      } catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
      return abstracts;
   }
   
   public static HashMap<String,Map<String,Double>> deleteThisFunciton2(String filePath, LinkedList<Patent> test) {
      LinkedList<String> abstracts = new LinkedList<String>();
      HashMap<String,Patent> patents = new HashMap<String,Patent>();
      for(Patent pat : test){
          patents.put(pat.patentAbstract,pat);
      }
      HashMap<String,Map<String,Double>> predictions = new HashMap<String,Map<String,Double>>();
      
      BufferedReader bw = null;
      try {
         //Specify the file name and path here
	 File file = new File(filePath);

	 /* This logic will make sure that the file 
	  * gets created if it is not present at the
	  * specified location*/
	  if (!file.exists()) {
	     file.createNewFile();
	  }

	  FileReader fw = new FileReader(file);
	  bw = new BufferedReader(fw);
          bw.readLine();
          while(bw.ready()){
              String line = bw.readLine();
              String parameter [] = line.split("\t");
              if(patents.containsKey(parameter[0])){
                String id = patents.get(parameter[0]).patentID;
                predictions.put(id,new HashMap<String,Double>());
                predictions.get(id).put(parameter[1], 1.0);
              }
          }
      } catch (IOException ioe) {
	   ioe.printStackTrace();
	}
	finally
	{ 
	   try{
	      if(bw!=null)
		 bw.close();
	   }catch(Exception ex){
	       System.out.println("Error in closing the BufferedWriter"+ex);
	    }
	}
      return predictions;
   }
   
   public static HashMap<String,LinkedList<Patent>> train_test_split(LinkedList<Patent> patents, double train_proportion){
       LinkedList<Patent> train = new LinkedList<Patent>();
       LinkedList<Patent> test = new LinkedList<Patent>();
       
       Random rd = new Random();
       for(Patent pat : patents){
            if(rd.nextDouble()<train_proportion){
                train.add(pat);
            }
            else{
                test.add(pat);
            }
       }
       HashMap<String,LinkedList<Patent>> train_test = new HashMap<String,LinkedList<Patent>>();
       train_test.put("train", train);
       train_test.put("test",test);
       return train_test;
   }
   
   public void printPatent(){
        Patent patent = this;
        if(patent.references.size()>0){
            for(int i =0 ; i < patent.references.size(); i ++){
                String reference = patent.references.get(i);
                System.out.println(patent.patentID+"\t"+patent.patentAbstract+"\t"+patent.patentTitle+"\t"+patent.ipcs+"\t"+reference+"\t"+patent.referencesAndRecognizedFields.get(reference)+"\t"+patent.referencesAndRecognizedJournals.get(reference)+"\t"+patent.inventors+"\t"+patent.assignes+"\t"+patent.firstBayesianField+"\t"+patent.secondBayesianField);
            }
        }else{
            System.out.println(patent.patentID+"\t"+patent.patentAbstract+"\t"+patent.patentTitle+"\t"+patent.ipcs+"\t"+patent.references+"\t"+patent.NPRscientificFields+"\t"+patent.citedJournals+"\t"+patent.inventors+"\t"+patent.assignes+"\t"+patent.firstBayesianField+"\t"+patent.secondBayesianField);
        }
   }
   
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OntologyTagging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import main.Main;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class AssigneTagger {
    
    public HashMap<String,LinkedList<String>> dbpediaEntities = new HashMap<String,LinkedList<String>>();
    public HashMap<String,String> organizationDict = new HashMap<String,String>();
    public HashSet<String> personNames = new HashSet<String>();

    public AssigneTagger(String dbpediaEntitiesPath) {
        dbpediaEntities = readDBpediaEntities(dbpediaEntitiesPath);
    }
    
    
    
    private double calculateDice(String s, String t)
    {
    	// Verifying the input:
    	if (s == null || t == null)
    		return 0;
    	// Quick check to catch identical objects:
    	if (s == t)
    		return 1;
            // avoid exception for single character searches
            if (s.length() < 2 || t.length() < 2)
                return 0;

    	// Create the bigrams for string s:
    	final int n = s.length()-1;
    	final int[] sPairs = new int[n];
    	for (int i = 0; i <= n; i++)
    		if (i == 0)
    			sPairs[i] = s.charAt(i) << 16;
    		else if (i == n)
    			sPairs[i-1] |= s.charAt(i);
    		else
    			sPairs[i] = (sPairs[i-1] |= s.charAt(i)) << 16;

    	// Create the bigrams for string t:
    	final int m = t.length()-1;
    	final int[] tPairs = new int[m];
    	for (int i = 0; i <= m; i++)
    		if (i == 0)
    			tPairs[i] = t.charAt(i) << 16;
    		else if (i == m)
    			tPairs[i-1] |= t.charAt(i);
    		else
    			tPairs[i] = (tPairs[i-1] |= t.charAt(i)) << 16;

    	// Sort the bigram lists:
    	Arrays.sort(sPairs);
    	Arrays.sort(tPairs);

    	// Count the matches:
    	int matches = 0, i = 0, j = 0;
    	while (i < n && j < m)
    	{
    		if (sPairs[i] == tPairs[j])
    		{
    			matches += 2;
    			i++;
    			j++;
    		}
    		else if (sPairs[i] < tPairs[j])
    			i++;
    		else
    			j++;
    	}
    	return (double)matches/(n+m);
    }
            
    public static String removerAcentos(String str) {
        return str.replace("é","e").replace("á","a").replace("ã","a").replace("í","i").replace("ê","e");
    }
    
    private String treatLabel(String raw){
        raw = raw.replace(", sa","").replace("s.a.","").replace(" inc", "").replace(" llc","").replace(" limited","").replace(" corp","").replace(" corporation"," ").replace(" company"," ").replace(" ltd", "").replace(",", "").replace(".","").replace("international","").replace("technologies","").replace("/","").replace("-"," ").replace("gmbh","");
        if(raw.endsWith(" sa")){
            raw = raw.replace(" sa","");
        }
        return removerAcentos(raw);
    }
    
    public void loadPeopleNames(){
        
            BufferedReader bw = null;
            try {
                bw = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Carlos\\Documents\\usp\\cnpq_bolsas\\treat\\cnpq-beneficiarios-modalidade-instituição.csv"),StandardCharsets.UTF_8));
                bw.readLine();
                while(bw.ready()){
                    String [] params = bw.readLine().split(",");
                    personNames.add((params[1].split(" ")[0]).toLowerCase());
                }
                bw.close();
            }catch (IOException ioe) {
                ioe.printStackTrace();
            }
            finally{ 
                
            }
    }
    
    public HashMap<String,LinkedList<String>> readDBpediaEntities(String path){
        HashMap<String,LinkedList<String>> orgs = new HashMap<String,LinkedList<String>>();
        File[]files = new File(path).listFiles();
        try{
            for(File file : files){
                BufferedReader bw = null;
                try {
                   //Specify the file name and path here
                   /* This logic will make sure that the file 
                    * gets created if it is not present at the
                    * specified location*/
                    if (!file.exists()) {
                       file.createNewFile();
                    }

                    bw = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()),StandardCharsets.UTF_8));
                    bw.readLine();
                    while(bw.ready()){
                        try{
                            String org_line = bw.readLine();
                            String[] org_entity_label = org_line.replace("\"","").split("\t");

//                            String label = treatLabel(org_entity_label[1].toLowerCase());
                            if(!orgs.containsKey(org_entity_label[0])){
                                orgs.put(org_entity_label[0], new LinkedList<String>());
                            }
                            orgs.get(org_entity_label[0]).add(org_entity_label[3]);
                        }catch(Exception e2){}
                    }
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                finally{ 
                    bw.close();
                }
            }
        
        }catch(Exception e){
            e.printStackTrace();
        }
        return orgs;
    }
    
    public LinkedList<String> getColumn(String path,int index){
        LinkedList<String> column = new LinkedList<String>();
        
        File[]files = new File[1];
        files[0] = new File(path);
        try{
            for(File file : files){
                BufferedReader bw = null;
                try {
                   //Specify the file name and path here
                   /* This logic will make sure that the file 
                    * gets created if it is not present at the
                    * specified location*/
                    if (!file.exists()) {
                       file.createNewFile();
                    }

                    bw = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()),StandardCharsets.UTF_8));
                    bw.readLine();
                    while(bw.ready()){
                        String line = bw.readLine();
                        if(!line.contains("\"")){
                            column.add(line.split(",")[index]);
                        }
                    }
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                finally{ 
                    bw.close();
                }
            }
        
        }catch(Exception e){
            e.printStackTrace();
        }
        return column;
    }
    
    public void mergeAlternativeNamesWithOriginalLabels(String path, HashMap<String,LinkedList<String>> alternatives){
        HashMap<String,LinkedList<String>> orgs = new HashMap<String,LinkedList<String>>();
        File[]files = new File(path).listFiles();
        try{
            for(File file : files){
                BufferedReader bw = null;
                try {
                   //Specify the file name and path here
                   /* This logic will make sure that the file 
                    * gets created if it is not present at the
                    * specified location*/
                    if (!file.exists()) {
                       file.createNewFile();
                    }

                    bw = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()),StandardCharsets.UTF_8));
                    bw.readLine();
                    while(bw.ready()){
                      String org_line = bw.readLine();
                      String[] org_entity_label = org_line.split("\t");
                      String best_entity_uri = findOrganizationEntity(org_entity_label[1],alternatives);
                      if(!best_entity_uri.contentEquals("")){
                          for(String label : alternatives.get(best_entity_uri)){
                              if(org_entity_label.length == 4)
                                System.out.println(org_entity_label[0]+"\t\""+label+"\"\t"+org_entity_label[2]+"\t"+org_entity_label[3]);
                              if(org_entity_label.length == 3)
                                System.out.println(org_entity_label[0]+"\t\""+label+"\"\t"+org_entity_label[2]);
                          }
                      }
                      
                    }
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                finally{ 
                    bw.close();
                }
            }
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public String findOrganizationEntity(String name, HashMap<String,LinkedList<String>> entities){
        double best_match = 0;
        String best_entity = "";
        
        String name_non_stop_w = treatLabel(name.toLowerCase());
        
        if(organizationDict.containsKey(name_non_stop_w)){
            best_entity = organizationDict.get(name_non_stop_w);
            return best_entity;
        }
        
        if(name.contains(",")){
            String[] names_splitted = name.toLowerCase().split(" ");
            for(String name_splitted : names_splitted){
//                System.out.println(name_splitted.replace(",","")+" "+personNames.contains(name_splitted.replace(",","")));
                if(personNames.contains(name_splitted.replace(",",""))){
//                    System.out.println("Nome de pessoa");
                    return "";
                }
            }
        }
        
        for(String uri : entities.keySet()){
            
            LinkedList<String> labels2 = entities.get(uri);
            LinkedList<String> labels = new LinkedList<String>();
            
            for(String label : labels2){
                if(label.contains("- ")){
                    String labels_aux [] = label.split("- ");
                    for(String label_aux : labels_aux){
                        labels.add(label_aux);
                    }
                }else{
                    labels.add(label);
                }
            }
            
            for(String label : labels){
                
                double match_value = calculateDice(name_non_stop_w,label);

                if(match_value > 0.87){
                    if(match_value > best_match){
                        best_match = match_value;
                        best_entity = uri;
                    }
                }
            }
        }
        if(!organizationDict.containsKey(name_non_stop_w)){
            organizationDict.put(name_non_stop_w,best_entity);
        }
        
        if(!best_entity.equals("")){
            return best_entity;
        }
        return "";
    }
}

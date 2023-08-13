/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import static main.Main.patstatPatents;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class SVMOneClass extends Classifier{
    double amplification;
    double amplificationF1;
    boolean useF1Amplification;
    HashMap<String, TreeMap<String, Double>> classificationCache = new HashMap<String, TreeMap<String, Double>>();
    
    public SVMOneClass() {}
    
    public void readDictionaryOfClassifications(String csv_path, LinkedList<Patent> refPatents){
        
        HashMap<String,Patent> patents = new HashMap<String,Patent>();
        for(Patent patent : refPatents){
            patents.put(patent.patentAbstract,patent);
        }
        BufferedReader bw = null;
        try {
            //Specify the file name and path here
            File file = new File(csv_path);

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
                String abstract_ = parameter[1];
                if(patents.containsKey(abstract_)){
                    HashMap<String, Double> probabilities = new HashMap<String, Double>();
                    if(parameter[3].contentEquals("1")){
                        probabilities.put("biomedical research",1.0);
                    }else{
                         probabilities.put("biomedical research",0.0);
                    }

                    TreeMap<String,Double>probabilities2=new TreeMap<String,Double>(new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                            return probabilities.get(s2).compareTo(probabilities.get(s1));
                        }
                    });
                    probabilities2.putAll(probabilities);
                    classificationCache.put(patents.get(abstract_).patentID,probabilities2);
                }
             }
        } catch (IOException ioe) {
	   ioe.printStackTrace();
	}

    }
    
    @Override
    public TreeMap<String, Double> classifyPatent(Patent patent) {
        if(classificationCache.containsKey(patent.patentID)){
            TreeMap<String, Double> results = classificationCache.get(patent.patentID);
            return results;
        }
        return null;
    }

    @Override
    public void trainPatents(LinkedList<Patent> patents) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void amplify(Map<String,Double> map){
        double total=0;
        for(String field:map.keySet()){
//            System.out.println(field+" amplification: "+amplification+" before: "+map.get(field)+" after: "+Math.pow(map.get(field), this.amplification));
            map.put(field,Math.pow(map.get(field), this.amplification));
        }
    }
}

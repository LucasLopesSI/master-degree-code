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
public class BERTLikelihood extends Classifier{
    double amplification;
    double amplificationF1;
    boolean useF1Amplification;
    HashMap<String, TreeMap<String, Double>> classificationCache = new HashMap<String, TreeMap<String, Double>>();
    
    public BERTLikelihood(double amplification, double amplificationF1, boolean useF1Amplification) {
        this.useF1Amplification = useF1Amplification;
        this.amplification = amplification;
        this.amplificationF1 = amplificationF1;
    }
    
    private TreeMap<String,Double> getF1Bert(double amplification){
        TreeMap<String,Double> f1Bert = new TreeMap<String,Double>();
        f1Bert.put("chemistry",Math.pow(0.736872812135355,amplification));
        f1Bert.put("mathematics",Math.pow(0.0,amplification));
        f1Bert.put("physics",Math.pow(0.677532013969732,amplification));
        f1Bert.put("biology (organismic & supraorganismic level)",Math.pow(0.710295291300877,amplification));
        f1Bert.put("clinical and experimental medicine",Math.pow(0.658500371195248,amplification));
        f1Bert.put("agriculture & environment",Math.pow(0.689361702127659,amplification));
        f1Bert.put("engineering",Math.pow(0.900404448938321,amplification));
        f1Bert.put("geociences & space sciences",Math.pow(0.583333333333333,amplification));
        f1Bert.put("biomedical research",Math.pow(0.335616438356164,amplification));
        return f1Bert;
    }
    
    public void readDictionaryOfClassifications(String csv_path, LinkedList<Patent> supplyPatents){
        
        HashMap<String,Patent> patents = new HashMap<String,Patent>();
        if(supplyPatents == null){
            for(Patent patent : patstatPatents){
                patents.put(patent.patentAbstract,patent);
            }
        }else{
            for(Patent patent : supplyPatents){
                patents.put(patent.patentAbstract,patent);
            }
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
                String abstract_ = parameter[24];
                if(patents.containsKey(abstract_)){
                    HashMap<String, Double> probabilities = new HashMap<String, Double>();
                    for(int i =0; i < 22;i+=2){
                        try{
                            probabilities.put(parameter[i],Double.parseDouble(parameter[i+1]));
                        }catch(Exception e){}
                    }

                    TreeMap<String,Double>probabilities2=new TreeMap<String,Double>(new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                            return probabilities.get(s2).compareTo(probabilities.get(s1));
                        }
                    });
                    probabilities2.putAll(probabilities);
//                    System.out.println(patents.get(abstract_).patentID);
                    classificationCache.put(patents.get(abstract_).patentID,probabilities2);
                }
             }
        } catch (IOException ioe) {
	   ioe.printStackTrace();
	}

    }
    
    @Override
    public TreeMap<String, Double> classifyPatent(Patent patent) {
//        System.out.println(classificationCache.keySet());
        if(classificationCache.containsKey(patent.patentID)){
            TreeMap<String, Double> results = classificationCache.get(patent.patentID);
            amplify(results);
            if(useF1Amplification){
                Map<String,Double> weightedResults = NaiveBayesClassifier.multiplyTwoMaps(results, getF1Bert(amplificationF1));
                Prior.convertAbsoluteToPercentual2(weightedResults);

                TreeMap<String,Double>posteriorOrdered=new TreeMap<String,Double>(new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return weightedResults.get(s2).compareTo(weightedResults.get(s1));
                }
                });
                posteriorOrdered.putAll(weightedResults);
                return posteriorOrdered;
            }else{
                Prior.convertAbsoluteToPercentual2(results);
                return results;
            }
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

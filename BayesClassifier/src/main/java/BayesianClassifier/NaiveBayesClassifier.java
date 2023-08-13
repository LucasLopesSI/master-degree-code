/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import main.Main;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class NaiveBayesClassifier extends Classifier{
    
    Classifier likelihood;
    Classifier prior;
    String classifier;

    public NaiveBayesClassifier(Classifier likelihood, Classifier prior, String classifier) {
        this.classifier = classifier;
        this.likelihood = likelihood;
        this.prior = prior;
    }
    
//    public TreeMap<String,Double> getLikelihoodWithoutAmplification(Patent patent){
//        return Main.likelihood_dummy.classifyPatent(patent);
//    }
    
    
    public TreeMap<String,Double> classifyPatent(Patent patent){
        LinkedList<String> ipcs = patent.ipcs;
        
        TreeMap<String,Double> likelihoodProbabilities = likelihood.classifyPatent(patent);
        if(classifier == "likelihood"){
            return likelihoodProbabilities;
        }
        TreeMap<String,Double> priorProbabilities = prior.classifyPatent(patent);
        if(classifier == "prior"){
            return priorProbabilities;
        }
        
        Random rand = new Random();
        if(priorProbabilities.keySet().size()<=1){
            return likelihoodProbabilities;
        }
        
        for(String field : likelihoodProbabilities.keySet()){
            try{
                if(!priorProbabilities.containsKey(field)){
                    priorProbabilities.put(field,0.009+0.00001 * rand.nextDouble());
                }
            }catch(Exception e){
            }
        }
        
        for(String field : priorProbabilities.keySet()){
            try{
                if(!likelihoodProbabilities.containsKey(field)){
                    likelihoodProbabilities.put(field, 0.009+ 0.00001 * rand.nextDouble());
                }
            }catch(Exception e){
            }
        }
        
        Map<String,Double> posterior = multiplyTwoMaps(priorProbabilities,likelihoodProbabilities);
        Prior.convertAbsoluteToPercentual2(posterior);
        
        TreeMap<String,Double>posteriorOrdered=new TreeMap<String,Double>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return posterior.get(s2).compareTo(posterior.get(s1));
            }
        });
        posteriorOrdered.putAll(posterior);
        HashSet<String> recognizedFields = new HashSet<String>();
        for(String field: likelihoodProbabilities.keySet()){
            recognizedFields.add(field);
        }
//        System.out.println("Posterior: "+posteriorOrdered);;;
//        System.out.print(patent.patentID+";");
//        for(String scientificField: Main.allScientificAreas){
//            if(scientificField!=null){
//                if(recognizedFields.contains(scientificField)){
//                    System.out.print(new DecimalFormat("#.####").format(likelihoodProbabilities.get(scientificField))+";");
//                }else{
//                    System.out.print(0.0+";");
//                }
//            }
//        }
//        System.out.println("");
        return posteriorOrdered;
    }
    
    public static Map<String,Double> multiplyTwoMaps(Map<String,Double>map_one,Map<String,Double>map_two){
        Map<String,Double> multiplied = new HashMap<String,Double>();
        TreeMap<String,Double>multiplied_sorted = new TreeMap<String,Double>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return multiplied.get(s2).compareTo(multiplied.get(s1));
            }
        });
        for(String key : map_one.keySet()){
            double val1 = map_one.get(key);
            double val2 = 0;
            try{
            if(map_two.get(key)!=null){
                 val2 = map_two.get(key);
            }
            }catch(Exception e){}
            multiplied.put(key, val1*val2);
        }
        multiplied_sorted.putAll(multiplied);
        return multiplied_sorted;
    }
    
    public static Map<String,Double> sumTwoMaps(Map<String,Double>map_one,Map<String,Double>map_two){
        Map<String,Double> multiplied = new HashMap<String,Double>();
        TreeMap<String,Double>multiplied_sorted = new TreeMap<String,Double>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return multiplied.get(s2).compareTo(multiplied.get(s1));
            }
        });
        for(String key : map_one.keySet()){
            double val1 = map_one.get(key);
            double val2 = 0;
            try{
            if(map_two.get(key)!=null){
                 val2 = map_two.get(key);
            }
            }catch(Exception e){}
            multiplied.put(key, val1+val2);
        }
        multiplied_sorted.putAll(multiplied);
        return multiplied_sorted;
    }
    
    public static double totalNumberInHashObject(Map<Object,Double>map){
        double a = 0;
        for(Object key: map.keySet()){
            a+=map.get(key);
        }
        return a;
    }
    
     public static void incrementMapObject(HashMap<String,Integer>map, String key){
        if(!map.containsKey(key)){
            map.put(key,1);
        }else{
            map.put(key, map.get(key)+1);
        }
    }

    @Override
    public void trainPatents(LinkedList<Patent> patents){
        int ipcDigits = 3;
        boolean smoothed = true;
        Prior prior = new Prior(patents,ipcDigits,smoothed);
        
        boolean countCitationDuplicates = false;
        double cut = 0;
        Likelihood likelihood = new Likelihood(countCitationDuplicates,cut,2);
        likelihood.trainPatents(patents);
    }
}

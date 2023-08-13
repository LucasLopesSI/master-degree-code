package Metrics;


import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import main.Main;
import patent.Patent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Lucas
 */
public class Metrics {
    
    
    public static String getElementByIndexInMap(Map<String,Double> map, int index){
        int i=1;
        for(String element : map.keySet()){
            if(i==index){
                return element;
            }
            i++;
        }
        return null;
    }
    
    public static LinkedList<String> getTwoMostFrequentElementInList(Collection<String> fields){
        LinkedList<String> best_fields= new LinkedList<>();
        HashMap<String,Integer>field_frequency=new HashMap<String,Integer>();
        
        for(String field: fields){
            
            if(field_frequency.containsKey(field)){
                field_frequency.put(field,field_frequency.get(field)+1);
            }else{
                field_frequency.put(field,1);
            }
        }
        TreeMap<String,Integer>field_frequency_sorted = new TreeMap<String,Integer>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return field_frequency.get(s2).compareTo(field_frequency.get(s1));
            }
        });
        field_frequency_sorted.putAll(field_frequency);
        int i=0;
        for(String field1: field_frequency_sorted.keySet()){
            if(i>1){
                break;
            }
            if(field_frequency_sorted.get(field1)>0){
                best_fields.add(field1);
            }
            i++;
        }
        return best_fields;
    }
    
    public static double getAccuracy(LinkedList<Patent> patents, HashMap<String,Map<String,Double>> predictions, int criteria){
        double accuracy = 0;
        int hits = 0;
        int total = 0;
        
        for(Patent patent : patents){
            if(patent.NPRscientificFields.size()>0){
                Map<String,Double> classifiedFields = predictions.get(patent.patentID);
                if(classifiedFields!= null){
                    String firstPrediction = getElementByIndexInMap(classifiedFields,1);
                    String secondPrediction = getElementByIndexInMap(classifiedFields,2);
                    if(criteria == 1){
                        if(patent.NPRscientificFields.contains(firstPrediction)){
                            hits++;
                        }else{
                            if(Main.log)
                                System.out.println(patent.ipcs+" actual: "+patent.NPRscientificFields+" predicted "+firstPrediction+" "+secondPrediction);
                        }
                    }
                    if(criteria == 2){
                        if(patent.NPRscientificFields.contains(firstPrediction) || patent.NPRscientificFields.contains(secondPrediction)){
                            hits++;
                        }else{
                            if(Main.log)
                                System.out.println(patent.ipcs+" actual: "+patent.NPRscientificFields+" predicted "+firstPrediction+" "+secondPrediction);
                        }
                    }
                    if(criteria == 3){
                        if(patent.NPRscientificFields.contains(secondPrediction)){
                            hits++;
                        }else{
                            if(Main.log)
                                System.out.println(patent.ipcs+" actual: "+patent.NPRscientificFields+" predicted "+firstPrediction+" "+secondPrediction);
                        }
                    }
                    total++;
                }
            }
        }
        accuracy = (double)hits/total;
        return accuracy;
    }
    
    public static HashMap<String,Double> getRecallByClass(LinkedList<Patent> patents, HashMap<String,Map<String,Double>> predictions){
        double recall = 0;
        
        HashMap<String,Double> recallForClasses = new HashMap<String,Double>();
        HashMap<String,Integer> true_positives = new HashMap<String,Integer>();
        HashMap<String,Integer> false_negatives = new HashMap<String,Integer>();
        int true_positives_global = 0;
        int false_negatives_global = 0;
        
        for(Patent patent : patents){
            if(patent.NPRscientificFields.size()>0){
                Map<String,Double> classifiedFields = predictions.get(patent.patentID);
                String firstPrediction = getElementByIndexInMap(classifiedFields,1);
                String secondPrediction = getElementByIndexInMap(classifiedFields,2);
                
                if(patent.NPRscientificFields.contains(firstPrediction)){
                    if(!true_positives.containsKey(firstPrediction)){
                        true_positives.put(firstPrediction, 0);
                    }
                    true_positives.put(firstPrediction, true_positives.get(firstPrediction)+1);
                }
//                if(patent.NPRscientificFields.contains(secondPrediction)){
//                    if(!true_positives.containsKey(secondPrediction)){
//                        true_positives.put(secondPrediction, 0);
//                    }
//                    true_positives.put(secondPrediction, true_positives.get(secondPrediction)+1);
//                }
                
                if(!patent.NPRscientificFields.contains(firstPrediction) && !patent.NPRscientificFields.contains(firstPrediction)){
                    LinkedList<String> twoMostCitedFieldsInNPR = getTwoMostFrequentElementInList(patent.NPRscientificFields);
                    String real = twoMostCitedFieldsInNPR.get(0);
                    if(!false_negatives.containsKey(real)){
                        false_negatives.put(real, 0);
                    }
                    false_negatives.put(real, false_negatives.get(real)+1);
                }
            }
        }
        LinkedList<String> observedClasses = new LinkedList<String>(true_positives.keySet());
        observedClasses.addAll(false_negatives.keySet());
        for(String targetClass : observedClasses){
            int true_positive_number = 0;
            if(true_positives.containsKey(targetClass)){ 
                   true_positive_number = true_positives.get(targetClass);
            }
            int false_negative_number = 0;
            if(false_negatives.containsKey(targetClass)){
                false_negative_number = false_negatives.get(targetClass);
            }
            double recallValueForClass = -1;
            if((true_positive_number + false_negative_number) != 0){
                recallValueForClass = (double) true_positive_number / (true_positive_number + false_negative_number);
            }
            recallForClasses.put(targetClass, recallValueForClass);
        }
        return recallForClasses;
    }
    
    public static double getRecall(LinkedList<Patent> patents, HashMap<String,Map<String,Double>> predictions){
        double recall = 0;
        
        HashMap<String,Double> recallForClasses = new HashMap<String,Double>();
        HashMap<String,Integer> true_positives = new HashMap<String,Integer>();
        HashMap<String,Integer> false_negatives = new HashMap<String,Integer>();
        int true_positives_global = 0;
        int false_negatives_global = 0;
        
        for(Patent patent : patents){
            if(patent.NPRscientificFields.size()>0){
                Map<String,Double> classifiedFields = predictions.get(patent.patentID);
                String firstPrediction = getElementByIndexInMap(classifiedFields,1);
                String secondPrediction = getElementByIndexInMap(classifiedFields,2);
                
                if(patent.NPRscientificFields.contains(firstPrediction)){
                    if(!true_positives.containsKey(firstPrediction)){
                        true_positives.put(firstPrediction, 0);
                    }
                    true_positives.put(firstPrediction, true_positives.get(firstPrediction)+1);
                }
//                if(patent.NPRscientificFields.contains(secondPrediction)){
//                    if(!true_positives.containsKey(secondPrediction)){
//                        true_positives.put(secondPrediction, 0);
//                    }
//                    true_positives.put(secondPrediction, true_positives.get(secondPrediction)+1);
//                }
                
                if(!patent.NPRscientificFields.contains(firstPrediction)){
                    LinkedList<String> twoMostCitedFieldsInNPR = getTwoMostFrequentElementInList(patent.NPRscientificFields);
                    String real = twoMostCitedFieldsInNPR.get(0);
                    if(!false_negatives.containsKey(real)){
                        false_negatives.put(real, 0);
                    }
                    false_negatives.put(real, false_negatives.get(real)+1);
                }
            }
        }
        LinkedList<String> observedClasses = new LinkedList<String>(true_positives.keySet());
        observedClasses.addAll(false_negatives.keySet());
        for(String targetClass : observedClasses){
            int true_positive_number = 0;
            if(true_positives.containsKey(targetClass)){ 
                   true_positive_number = true_positives.get(targetClass);
            }
            int false_negative_number = 0;
            if(false_negatives.containsKey(targetClass)){
                false_negative_number = false_negatives.get(targetClass);
            }
            
            false_negatives_global += false_negative_number;
            true_positives_global += true_positive_number;
        }
        return ((double) true_positives_global / (true_positives_global + false_negatives_global));
    }
    
    public static HashMap<String,Double> getPrecisionByClass(LinkedList<Patent> patents, HashMap<String,Map<String,Double>> predictions){
        
        HashMap<String,Double> precisionForClasses = new HashMap<String,Double>();
        HashMap<String,Integer> true_positives = new HashMap<String,Integer>();
        HashMap<String,Integer> false_positives = new HashMap<String,Integer>();
        int true_positives_global = 0;
        int false_negatives_global = 0;
        
        for(Patent patent : patents){
            if(patent.NPRscientificFields.size()>0){
                Map<String,Double> classifiedFields = predictions.get(patent.patentID);
                String firstPrediction = getElementByIndexInMap(classifiedFields,1);
                String secondPrediction = getElementByIndexInMap(classifiedFields,2);
                
                if(patent.NPRscientificFields.contains(firstPrediction)){
                    if(!true_positives.containsKey(firstPrediction)){
                        true_positives.put(firstPrediction, 0);
                    }
                    true_positives.put(firstPrediction, true_positives.get(firstPrediction)+1);
                }
//                if(patent.NPRscientificFields.contains(secondPrediction)){
//                    if(!true_positives.containsKey(secondPrediction)){
//                        true_positives.put(secondPrediction, 0);
//                    }
//                    true_positives.put(secondPrediction, true_positives.get(secondPrediction)+1);
//                }
                
                if(!patent.NPRscientificFields.contains(firstPrediction)){
                    LinkedList<String> twoMostCitedFieldsInNPR = getTwoMostFrequentElementInList(patent.NPRscientificFields);
                    String real = twoMostCitedFieldsInNPR.get(0);
                    if(!false_positives.containsKey(firstPrediction)){
                        false_positives.put(firstPrediction, 0);
                    }
                    false_positives.put(firstPrediction, false_positives.get(firstPrediction)+1);
                }
            }
        }
        for(String targetClass : true_positives.keySet()){
            int true_positive_number = true_positives.get(targetClass);
            int false_positives_number = 0;
            if(false_positives.containsKey(targetClass)){
                false_positives_number = false_positives.get(targetClass);
            }
            double precisionValueForClass = -1;
            if((true_positive_number + false_positives_number) != 0){
                precisionValueForClass = (double) true_positive_number / (true_positive_number + false_positives_number);
            }
            precisionForClasses.put(targetClass, precisionValueForClass);
        }
        return precisionForClasses;
    }
    
    public static double getPrecision(LinkedList<Patent> patents, HashMap<String,Map<String,Double>> predictions){
        
        HashMap<String,Double> precisionForClasses = new HashMap<String,Double>();
        HashMap<String,Integer> true_positives = new HashMap<String,Integer>();
        HashMap<String,Integer> false_positives = new HashMap<String,Integer>();
        int true_positives_global = 0;
        int false_negatives_global = 0;
        
        for(Patent patent : patents){
            if(patent.NPRscientificFields.size()>0){
                Map<String,Double> classifiedFields = predictions.get(patent.patentID);
                String firstPrediction = getElementByIndexInMap(classifiedFields,1);
                String secondPrediction = getElementByIndexInMap(classifiedFields,2);
                
                if(patent.NPRscientificFields.contains(firstPrediction)){
                    if(!true_positives.containsKey(firstPrediction)){
                        true_positives.put(firstPrediction, 0);
                    }
                    true_positives.put(firstPrediction, true_positives.get(firstPrediction)+1);
                }
//                if(patent.NPRscientificFields.contains(secondPrediction)){
//                    if(!true_positives.containsKey(secondPrediction)){
//                        true_positives.put(secondPrediction, 0);
//                    }
//                    true_positives.put(secondPrediction, true_positives.get(secondPrediction)+1);
//                }
                
                if(!patent.NPRscientificFields.contains(firstPrediction)){
                    LinkedList<String> twoMostCitedFieldsInNPR = getTwoMostFrequentElementInList(patent.NPRscientificFields);
                    String real = twoMostCitedFieldsInNPR.get(0);
                    if(!false_positives.containsKey(firstPrediction)){
                        false_positives.put(firstPrediction, 0);
                    }
                    false_positives.put(firstPrediction, false_positives.get(firstPrediction)+1);
                }
            }
        }
        
        for(String targetClass : true_positives.keySet()){
            int true_positive_number = true_positives.get(targetClass);
            int false_positives_number = 0;
            if(false_positives.containsKey(targetClass)){
                false_positives_number = false_positives.get(targetClass);
            }
            true_positives_global+=true_positive_number;
            false_negatives_global+=false_positives_number;
            
        }
        return ((double) true_positives_global / (true_positives_global + false_negatives_global));
    }
    
    public static double getF1Score(double recall, double precision){
        return( 2 * precision * recall / (precision + recall));
    }
    
    public static String[][] confusionMatrix(Collection<String> actual_dictionary, Collection<String> predicted_dictionary, Collection<String> actual, Collection<String> predicted){
        if(actual.size() != predicted.size()){
            Exception exp = new Exception("The size of actual and predicted array must be the same");
            exp.printStackTrace();
        }
        
        String[][] confusion = new String[actual_dictionary.size()+1][predicted_dictionary.size()+1];
        
        HashMap<String,Integer> lineDictionary = new HashMap<String,Integer>();
        HashMap<String,Integer> columnDictionary = new HashMap<String,Integer>();
        
        for(int i=0; i < confusion.length; i++){
            for(int j=0; j < confusion[i].length; j++){
                confusion[i][j] = "0";
            }
        }
        
        int i=1;
        for(String class_ : actual_dictionary){
            confusion[i][0] = class_;
            lineDictionary.put(class_, i);
            i++;
        }
        
        int j=1;
        for(String class_ : predicted_dictionary){
            confusion[0][j] = class_;
            columnDictionary.put(class_, j);
            j++;
        }
        LinkedList<String>actual_list = new LinkedList<String>(actual);
        LinkedList<String>predicted_list = new LinkedList<String>(predicted);
        
        for(i =0; i<actual_list.size(); i++){
            int i_ind = lineDictionary.get(actual_list.get(i));
            int j_ind = columnDictionary.get(predicted_list.get(i));
            confusion[i_ind][j_ind] = ""+(Integer.valueOf(confusion[i_ind][j_ind])+1);
        }
        
        return confusion;
    }
    
    public static String[][] confusionMatrix(LinkedList<Patent> patents, HashMap<String,Map<String,Double>> predictions){
        Collection<String> actual_dictionary = new TreeSet<String>();
        Collection<String> predicted_dictionary = new TreeSet<String>();
        
        Collection<String> actual = new LinkedList<String>();
        Collection<String> predicted = new LinkedList<String>();
        
        for(Patent pat: patents){
            for(String scientific_field : pat.NPRscientificFields){
                if(!actual_dictionary.contains(scientific_field)){
                    if(!scientific_field.contentEquals("art & humanities") && !scientific_field.contentEquals("biosciences (general, cellular&subcellular biology; genetics)") && !scientific_field.contentEquals("social sciences") && !scientific_field.contentEquals("neuroscience & behavior")){
                        actual_dictionary.add(scientific_field);
                    }
                }
                if(!predicted_dictionary.contains(scientific_field)){
                    if(!scientific_field.contentEquals("art & humanities") && !scientific_field.contentEquals("biosciences (general, cellular&subcellular biology; genetics)") && !scientific_field.contentEquals("social sciences") && !scientific_field.contentEquals("neuroscience & behavior")){
                        predicted_dictionary.add(scientific_field);
                    }
                }
            }
        }
        
        for(Patent pat: patents){
            if(pat.NPRscientificFields.size()>0){
                Map<String,Double> classifiedFields = predictions.get(pat.patentID);
                String firstPrediction = getElementByIndexInMap(classifiedFields,1);
                LinkedList<String> actual_NPRs = getTwoMostFrequentElementInList(pat.NPRscientificFields);
                if(actual_NPRs.size()>0){
                    String scientific_field = actual_NPRs.get(0);
                    if(!scientific_field.contentEquals("art & humanities") && !scientific_field.contentEquals("biosciences (general, cellular&subcellular biology; genetics)") && !scientific_field.contentEquals("social sciences") && !scientific_field.contentEquals("neuroscience & behavior")){
                        actual.add(actual_NPRs.get(0));
                        predicted.add(firstPrediction);
                    }
                }
            }
        }
        
        for(String pat: predictions.keySet()){
            for(String scientific_field : predictions.get(pat).keySet()){
                if(!predicted_dictionary.contains(scientific_field)){
                    if(!scientific_field.contentEquals("art & humanities") && !scientific_field.contentEquals("biosciences (general, cellular&subcellular biology; genetics)") && !scientific_field.contentEquals("social sciences") && !scientific_field.contentEquals("neuroscience & behavior")){
                        predicted_dictionary.add(scientific_field);
                    }
                }
                if(!actual_dictionary.contains(scientific_field)){
                    if(!scientific_field.contentEquals("art & humanities") && !scientific_field.contentEquals("biosciences (general, cellular&subcellular biology; genetics)") && !scientific_field.contentEquals("social sciences") && !scientific_field.contentEquals("neuroscience & behavior")){
                        actual_dictionary.add(scientific_field);
                    }
                }
            }
        }
        return confusionMatrix(actual_dictionary,predicted_dictionary,actual,predicted);
    }
    
    public static String[][] scientificFieldscoOccurrencesMatrix(LinkedList<Patent> patents){
        Collection<String> actual_dictionary = new TreeSet<String>();
        Collection<String> predicted_dictionary = new TreeSet<String>();
        
        Collection<String> fields = new LinkedList<String>();
        Collection<String> coocurrent_fields = new LinkedList<String>();
        
        for(Patent pat: patents){
            for(String scientific_field : pat.NPRscientificFields){
                if(!actual_dictionary.contains(scientific_field)){
                    actual_dictionary.add(scientific_field);
                }
                if(!predicted_dictionary.contains(scientific_field)){
                    predicted_dictionary.add(scientific_field);
                }
            }
        }
        
        for(Patent pat: patents){
            if(pat.NPRscientificFields.size()>0){
                LinkedList<String> bestFields = getTwoMostFrequentElementInList(pat.NPRscientificFields);
                if(bestFields.size()==2){
                    fields.add(bestFields.get(0));
                    coocurrent_fields.add(bestFields.get(1));
                }
            }
        }
        
        return confusionMatrix(actual_dictionary,predicted_dictionary,fields,coocurrent_fields);
    }
    
}

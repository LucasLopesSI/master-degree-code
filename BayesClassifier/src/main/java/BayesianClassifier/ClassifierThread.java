/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class ClassifierThread extends Thread{
    
    public Classifier classifier;
    public LinkedList<Patent> patents;
    public HashMap<String,Map<String,Double>> predictions;

    public ClassifierThread(LinkedList<Patent> patents, Classifier classifier) {
        this.classifier = classifier;
        this.patents = patents;
    }

    @Override
    public void run() {
        HashMap<String,Map<String,Double>> nbClassificationDictionary = new HashMap<String,Map<String,Double>>();
        int i =0;
        for(Patent patent : patents){
            System.out.println("classifying patent " +i+" \t id: "+ patent.patentID);
            Map<String,Double> classification = classifier.classifyPatent(patent);
            try{
                patent.firstBayesianField = Metrics.Metrics.getElementByIndexInMap(classification, 1);
                patent.secondBayesianField = Metrics.Metrics.getElementByIndexInMap(classification, 2);
                nbClassificationDictionary.put(patent.patentID, classification);
            }catch(Exception e1){
            }
            i++;
        }
        predictions = nbClassificationDictionary;
    }
    
    
    
    
}

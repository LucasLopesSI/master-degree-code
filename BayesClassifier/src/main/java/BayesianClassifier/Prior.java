/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import patent.Patent;

/**
 *
 * @author Lucas
 */
public class Prior extends Classifier{
    
    public TreeMap<String,TreeMap<String,Double>> priorProbabilities = new TreeMap<String,TreeMap<String,Double>>();
    public TreeMap<String,Double>field_probabilities = new TreeMap<String,Double>();
    public TreeMap<String,Double>ipc_code_probabilities = new TreeMap<String,Double>();
    public LinkedList<Patent> trainingPatents;
    public int ipcNDigits;

    public Prior(){}
    
    public Prior(LinkedList<Patent> trainingPatents, int ipcNDigits, boolean smoothed) {
        this.trainingPatents = trainingPatents;
        this.ipcNDigits = ipcNDigits;
        trainPatents();
        if(smoothed){
            initializeSmoothedIPCFields();
        }
        convertAbsoluteToPercentual(priorProbabilities);
        convertAbsoluteToPercentual2(field_probabilities);
        convertAbsoluteToPercentual2(ipc_code_probabilities);
    }
    
    public void initializeSmoothedIPCFields (){
        LinkedList<String> fields = new LinkedList<String>();
        for(Patent patent: trainingPatents){
            for(String field: patent.NPRscientificFields){
                if(!fields.contains(field)){
                    fields.add(field);
                }
            }
        }
        
        for(String ipc : priorProbabilities.keySet()){
            
            for(String scientific_field : fields){
                scientific_field = scientific_field.replace("\"","").replace(",","");
                if(priorProbabilities.get(ipc).containsKey(scientific_field)){
                    priorProbabilities.get(ipc).put(scientific_field.replace("\"",""), priorProbabilities.get(ipc).get(scientific_field)+1);
                }else{
                    priorProbabilities.get(ipc).put(scientific_field.replace("\"",""),1.0);
                }
            }
        }
    }
    
    public void printPrior(){
        System.out.println("###################### Probabilities of Scientific field by IPC "+ipcNDigits+" digits ######################");
        for(String ipc:priorProbabilities.keySet()){
            System.out.println(ipc);
            TreeMap<String,Double> a1 = priorProbabilities.get(ipc);
            for(String field:a1.keySet()){
                  System.out.println("\t"+field+"\t"+a1.get(field));
            }
        }
        System.out.println("########################## Probabilities of Scientific field ##########################");
        for(String field:field_probabilities.keySet()){
            System.out.println("\t"+field+"\t"+field_probabilities.get(field)*100+"%");
        }
        System.out.println("########################## Probabilities of IPCs codes ##########################");
        for(String field:ipc_code_probabilities.keySet()){
            System.out.println("\t"+field+"\t"+ipc_code_probabilities.get(field)*100+"%");
        }
        System.out.println("##############################################################################");
    }
    
    private void countPriorProbabilitites(LinkedList<String> IPC_code, LinkedList<String> scientific_fields){
        for(String ipc : IPC_code){
            try{
                ipc = ipc.replace("\"","").replace(" ","").substring(0,ipcNDigits);
            }catch(Exception e){
                continue;
            }
            if(!priorProbabilities.containsKey(ipc)){
                priorProbabilities.put(ipc,new TreeMap<String,Double>());
            }

            if(!ipc_code_probabilities.containsKey(ipc)){
                ipc_code_probabilities.put(ipc,1.0);
            }else{
                ipc_code_probabilities.put(ipc,ipc_code_probabilities.get(ipc)+1);
            }
            
            double i=1;
            
            for(String scientific_field : scientific_fields){
                scientific_field = scientific_field.replace("\"","");
                if(scientific_field.length()<2){
                    continue;
                }
                if(!field_probabilities.containsKey(scientific_field.replace("\"",""))){
                    field_probabilities.put(scientific_field.replace("\"",""), i);
                }else{
                    field_probabilities.put(scientific_field, field_probabilities.get(scientific_field)+i);
                }                                
                if(priorProbabilities.get(ipc).containsKey(scientific_field)){
                    priorProbabilities.get(ipc).put(scientific_field.replace("\"",""), priorProbabilities.get(ipc).get(scientific_field)+i);
                }else{
                    priorProbabilities.get(ipc).put(scientific_field.replace("\"",""),i);
                }
            }
        }
    }
    
    private void trainPatents(){
        for(Patent patent : trainingPatents){
            if(patent.NPRscientificFields.size()>0){
                countPriorProbabilitites(patent.ipcs,patent.NPRscientificFields);
            }
        }
    }
    
    public void trainPatents(LinkedList<Patent> patents){
        for(Patent patent : patents){
            if(patent.NPRscientificFields.size()>0){
                countPriorProbabilitites(patent.ipcs,patent.NPRscientificFields);
            }
        }
    }
    
    public TreeMap<String,Double> classifyPatent(Patent patent){
        HashMap<String,Double>prior = new HashMap<String,Double>();
        TreeMap<String,Double>prior_fields_sorted = new TreeMap<String,Double>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return prior.get(s2).compareTo(prior.get(s1));
            }
        });
        Random rand = new Random();
        for(String ipc_full: patent.ipcs){
            String ipc = ipc_full.substring(0,ipcNDigits);
            
            for(String field:field_probabilities.keySet()){
                field = field.replace("\"","").replace(",","");
                
                if(priorProbabilities.get(ipc)!=null && priorProbabilities.get(ipc).get(field)!=null){
                    double prior_probability = priorProbabilities.get(ipc).get(field);
//                    System.out.println(ipc+"\t"+field+"\t"+prior_probability);
                    if(!prior.containsKey(field)){
                        prior.put(field, prior_probability);
                    }else{
                        prior.put(field, prior.get(field)+prior_probability + 0.00001 * rand.nextDouble());
                    }
                }
            }
        }
        prior_fields_sorted.putAll(prior);
        convertAbsoluteToPercentual2(prior_fields_sorted);
        return prior_fields_sorted;
    }
    
    
    public static void convertAbsoluteToPercentual2(Map<String,Double> map){
        double total=0;
        for(String field:map.keySet()){
            total+=map.get(field);
        }
        for(String field:map.keySet()){
            map.put(field,(double)map.get(field)/total);
        }
    }
    
    public static void convertAbsoluteToPercentual(TreeMap<String,TreeMap<String,Double>> map){
        for(String ipc: map.keySet()){
            Map<String,Double> a = map.get(ipc);
            convertAbsoluteToPercentual2(a);
        }
    }
    
}

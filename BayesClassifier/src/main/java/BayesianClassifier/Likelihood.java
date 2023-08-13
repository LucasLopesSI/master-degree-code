/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BayesianClassifier;

import java.util.HashMap;
import java.util.LinkedList;
import patent.Patent;
import Ngrams.Ngram;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import main.Main;

/**
 *
 * @author Lucas
 */
public class Likelihood extends Classifier{
    HashMap<String,Ngram> fieldsNgrams = new HashMap<String,Ngram>();
    double cut;
    double amplification;
    boolean countCitationDuplicates;

    public Likelihood(boolean countCitationDuplicates, double cut, double amplification) {
        this.countCitationDuplicates = countCitationDuplicates;
        this.cut = cut;
        this.amplification = amplification;
    }
    
    public TreeMap<String,Double> classifyPatent(Patent patent){
        
        String patentAbstract = patent.patentAbstract;
        if(main.Main.log){
            System.out.println(patentAbstract);
        }
        Random sorteia = new Random();
        patentAbstract = patentAbstract.toLowerCase();
        patentAbstract = removePontuationCharacters(patentAbstract);
        
        double max=0;
        double sec_max=0;
        String best="";
        String second="";
        String[] palavra_citacao = patentAbstract.split(" ");
        //palavra_citacao = NLPparser.getLemaWord(palavra_citacao);
        
        LinkedList<String> palavra_citacao2 = new LinkedList<String>();
        for(String palavra:palavra_citacao){
            if(palavra.endsWith("s")){
                palavra = palavra.substring(0,palavra.length()-1);
            }
            if(!palavra_citacao2.contains(palavra))
                palavra_citacao2.add(palavra);
        }
        
        double local_count=0;
        LinkedList<Double>sum_weigths = new LinkedList<Double>();
        
        HashMap<String,Double>probability=new HashMap<>();
            //System.out.println(field);
            int max_grams=3;
            for(int i=1;i<max_grams+1;i++){
                
                for(String field : fieldsNgrams.keySet()){
                    //Somatório dos Pesos associado das palavras a uma área
                    local_count=0;
                    for(int count=0;count<palavra_citacao2.size()-(i-1);count++){
                        
                        String palavra="";
                        for(int j=count;j<count+i;j++){
                            palavra+=palavra_citacao2.get(j)+" ";
                        }
                        if(palavra.length()>0){
                            palavra=palavra.substring(0,palavra.length()-1);
                        }
                        
                        HashMap<String,Double> a = fieldsNgrams.get(field).getNgram(i);
                        
                        double peso_nas_outras_areas=0;
                        
                        for(String field1: fieldsNgrams.keySet()){
                            
                            HashMap<String,Double> a1 = fieldsNgrams.get(field1).getNgram(i);
                            
                            if(a1.get(palavra)!=null){
                                peso_nas_outras_areas+= a1.get(palavra);
                            }
                            
                        }
                        
                        if(a.get(palavra)!=null){
                            
                            if(a.get(palavra)>=peso_nas_outras_areas/cut){
                                
                                if(peso_nas_outras_areas!=0){
                                    if(main.Main.log){
                                        System.out.println(palavra+"\t"+field+"\t"+i+"\t"+(double)a.get(palavra)/Math.pow(peso_nas_outras_areas,1)*i);
                                    }
                                    local_count += (double)a.get(palavra)/Math.pow(peso_nas_outras_areas,1)*i+0.0001*sorteia.nextDouble();
                                }
                                else{
                                    local_count += (double)a.get(palavra)*i+0.0001*sorteia.nextDouble();
                                }
                            }
                        }
                    }
                    field=field.replace("\"","").replace(",","");

                    if(probability.containsKey(field)){
                        probability.put(field, Math.pow(probability.get(field), 1)+local_count);
                    }else{
                        probability.put(field, local_count);
                    }
                }
               
            }
            //System.out.println("");
            Prior.convertAbsoluteToPercentual2(probability);
            amplify(probability);
            Prior.convertAbsoluteToPercentual2(probability);
            TreeMap<String,Double>probability2=new TreeMap<String,Double>(new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return probability.get(s2).compareTo(probability.get(s1));
                }
            });
        
        probability2.putAll(probability);
//        System.out.println(probability2);
        return probability2;
    }
    
    public void amplify(Map<String,Double> map){
        double total=0;
        for(String field:map.keySet()){
//            System.out.println(field+" amplification: "+amplification+" before: "+map.get(field)+" after: "+Math.pow(map.get(field), this.amplification));
            map.put(field,Math.pow(map.get(field), this.amplification));
        }
    }
    
    public void trainPatents(LinkedList<Patent> patents){
        for(Patent patent : patents){
//            String[] tokens_title = removePontuationCharacters(patent.patentTitle).split(" ");
            String[] tokens_abstract = removePontuationCharacters(patent.patentAbstract).split(" ");
            if(countCitationDuplicates){
                for(String field: patent.NPRscientificFields){
                    for(int gram = 1; gram<4;gram++){
    //                    updateNGram(gram, field, tokens_title);
                        updateNGram(gram, field, tokens_abstract);
                        if(field!=null && field.length() < 50 && !Main.allScientificAreas.contains(field)){
                            Main.allScientificAreas.add(field);
                        }
                    }
                }
            }else{
                HashSet<String> fields = new HashSet<String>(patent.NPRscientificFields);
                for(String field: fields){
                    for(int gram = 1; gram<4;gram++){
    //                    updateNGram(gram, field, tokens_title);
                        updateNGram(gram, field, tokens_abstract);
                    }
                }
            }
//            for(int gram = 1; gram<4;gram++){
//                for(String class_:Main.allScientificAreas){
//                    if(fieldsNgrams.get(class_)!=null){
//                        HashMap<String,Double> ngram = fieldsNgrams.get(class_).getNgram(gram);
//                        Prior.convertAbsoluteToPercentual2(ngram);
//                    }
//                }
//            }
        }
    }
    
    public void printNGramas(int n_grams){
            for(String field: fieldsNgrams.keySet()){
              System.out.println(field);
              HashMap<String,Double> a = fieldsNgrams.get(field).getNgram(n_grams);
              for(String word:a.keySet()){
                    System.out.println(field+"\t"+word+"\t"+a.get(word));
                    
              }
        }
    }
    
    private void updateNGram(int gram, String field, String[]tokens){
        if(!fieldsNgrams.containsKey(field)){
             fieldsNgrams.put(field, new Ngram());
         }
        
         for(int i=0;i<tokens.length-gram-1;i++){
             LinkedList<String> grams = new LinkedList<String>();
             for(int j=i;j<(i+gram);j++){
                 grams.add(tokens[j]);
             }

             boolean non_stop_words = true;
             for(String word:grams){
                 if(StopWords.isStopword(word)){
                     non_stop_words=false;
                 }
                 if(word.length()<=2){
                     non_stop_words=false;
                 }
             }
             
             HashMap<String,Double> ngram = fieldsNgrams.get(field).getNgram(gram);
             
             //System.out.println(non_stop_words);
             if(non_stop_words){
                 String added_string="";
                 for(String word: grams){
                     added_string+=word+" ";
                 }
                 String added_string2 =added_string;
                 added_string = added_string.substring(0,added_string.length()-1);
                 if(ngram.containsKey(added_string)){
                     ngram.put(added_string,ngram.get(added_string)+1.0);
                 }else{
                     ngram.put(added_string,1.0);
                 }
             }
         }
    }
    
    private static String removePontuationCharacters(String base){
        base = base.replace(",", "").replace("\""," ").replace("/"," ").replace("'", "").replace(";","").replace("?","").replace("-"," ").replace("("," ").replace(")"," ").replace(".","");
        return base;
    }
}

class StopWords{

    static BufferedReader bufferedReader ;
    static HashSet<String> myStopWords;
    static String text;
    public static void MyStopWordsHandler() {
        String filename="/home/lucas/Documentos/backup/Desktop/github/ClassifierNLP/patents_extraction/_stopwords.txt";
        // TODO Auto-generated constructor stub
        myStopWords = new HashSet<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(filename)); 
                int cont=0;
                while(br.ready()){
                String linha = br.readLine();
                myStopWords.add(linha.replace("","").replace("\n", ""));
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isStopword(String word) {
        // TODO Auto-generated method stub
        
        if(myStopWords == null){
            MyStopWordsHandler();
        }
        
        word = word.toLowerCase();
        return myStopWords.contains(word);
    }
    
}

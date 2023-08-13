/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ngrams;

import java.util.HashMap;

/**
 *
 * @author Lucas
 */
public class Ngram {
    
    public HashMap<String,Double> unigram = new HashMap<String,Double>();
    public HashMap<String,Double> bigram = new HashMap<String,Double>();
    public HashMap<String,Double> trigram = new HashMap<String,Double>();
    
    public HashMap<String,Double> getNgram(int ngram){
        if(ngram == 1){
            return unigram;
        }
        if(ngram == 2){
            return bigram;
        }
        if(ngram == 3){
            return trigram;
        }
        return new HashMap<String,Double>();
    }
}

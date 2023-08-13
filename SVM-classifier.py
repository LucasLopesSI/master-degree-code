import pandas as pd
import numpy as np
import tensorflow as tf
import torch
from transformers import AutoTokenizer
from transformers import BertForMaskedLM
from transformers import AutoModel
import time
import random
from sklearn import svm
from sklearn.model_selection import ParameterGrid
from sklearn import metrics
from os import listdir
from os.path import isfile, join
import os
from os import path

def loadTensors(folder_path):
  list_of_tensors = []
  tensors = [str(folder_path+'/'+f) for f in listdir(folder_path) if isfile(join(folder_path, f))]

  for i in range(0,len(tensors)):
    fileName = folder_path.split('/')[1].replace("_tensors","_tensor")+"_"+str(i)+".pt"
    print('reading ',folder_path+"/"+fileName)
    tensor = torch.load(folder_path+"/"+fileName);
    list_of_tensors.append(tensor)
  return list_of_tensors

def loadVectorizedTensorsInLists():
  trainingEmbeddings = loadTensors('./svm_training_tensors/content/training_tensors/content/training_tensors')
  abstractValidationEmbeddings = loadTensors('./svm_validation_tensors/content/validation_tensors/content/validation_tensors/abstract_tensors')
  abstractTestEmbeddings = loadTensors('./svm_test_tensors/content/test_tensors/content/test_tensors/abstract_tensors')
  return [trainingEmbeddings,abstractValidationEmbeddings,abstractTestEmbeddings]

lists = loadVectorizedTensorsInLists()
trainingEmbeddings=lists[0]
labels = [1 for index in trainingEmbeddings]
trainingLabels = np.array(labels)
abstractValidationEmbeddings=lists[1]
abstractTestEmbeddings=lists[2]

########################################## Load BERT #################################################################

tokenizer = AutoTokenizer.from_pretrained('bert-large-uncased', do_lower_case=True)
mlm = BertForMaskedLM.from_pretrained('bert-large-uncased')
model = AutoModel.from_pretrained('bert-large-uncased')

############################## Functions to Convert Sentences in vectors #############################################

# store the metrics in seconds of the required time to convert a natural language sentence to vector in BERT space
tokenizations_time_metrics = [] 

## Aqui ele pega o texto original e gera os tokens
def convertSentenceToBERTEmbedding(sentence):
  ## tokenize sentence to get the id of the tokens
  input_ids = tokenizer.encode(sentence, return_tensors='pt')
  
  ## if number of tokens is bigger than 512, than cut in max 512 tokens
  if(input_ids.shape[1]>512):
    input_ids = input_ids[:1,:512]
  
  with torch.no_grad():
      start_time = time.time()
      #convert tokens_id to vector
      outs = model(input_ids)
      encoded = outs[0][0, 1:-1]  # Ignore [CLS] and [SEP] special tokens

      #calculate average coordinate for vectors
      out = tf.reduce_mean(encoded,0)
      tokenizations_time_metrics.append([len(encoded),(time.time() - start_time)])
  return out
  
def convertListOfSentencesToListOfTokensEmbeddings(list_of_sentences):
  listOfEmbeddings = []

  i = 0;
  for sentence in list_of_sentences:
    print('converting sentence ',(i+1),' of ',len(list_of_sentences),' to BERT vector')
    sentenceEmbedding = convertSentenceToBERTEmbedding(sentence)
    if( sentenceEmbedding != None):
      listOfEmbeddings.append(sentenceEmbedding)
    i+=1

  return listOfEmbeddings
 
indices = range(0,len(trainingEmbeddings))

trainingEmbeddingsFiltered = []
trainingLabelsFiltered = []

for i in indices:
  trainingEmbeddingsFiltered.append(trainingEmbeddings[i])
  trainingLabelsFiltered.append(trainingLabels[i])

classifySentences = pd.read_csv('./patents.csv', sep='\t')
#filteredSentences = classifySentences[(classifySentences['firstBayesianField'] == 'clinical and experimental medicine') | (classifySentences['firstBayesianField'] == 'chemistry')]
filteredSentences = classifySentences
print(len(filteredSentences['patentAbstract'].unique().tolist()))

bestParameters = {'gamma': 0.32, 'kernel': 'rbf', 'nu': 0.1}
print(bestParameters)
tuned_ocsvm = svm.OneClassSVM()
tuned_ocsvm.set_params(**bestParameters)
tuned_ocsvm.fit(trainingEmbeddingsFiltered, trainingLabelsFiltered)

already_classified = {}

for index,row in filteredSentences.iterrows():
	print(index)
	try:	
		if(row['patentAbstract'] in already_classified):
			continue
		classifyEmbeddings = convertListOfSentencesToListOfTokensEmbeddings([row['patentAbstract']])

		probs = tuned_ocsvm.decision_function(classifyEmbeddings)
		predicted_labels = tuned_ocsvm.predict(classifyEmbeddings)

		data_test = {'field':[row['patentID']],
			    'abstract':[row['patentAbstract']],
			    'confidence':probs.flatten(),
			    'prediction':predicted_labels.flatten()}

		results_test = pd.DataFrame(data_test)
		results_test.to_csv('predictions-svm.csv',index=False, sep= '\t', mode = 'a', header = False)
		already_classified[row['patentAbstract']] = True
	except:
		print('Exception while reading sentence')

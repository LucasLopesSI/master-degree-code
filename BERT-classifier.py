from transformers import TextClassificationPipeline
from transformers import BertTokenizer, BertForSequenceClassification
import pandas as pd

data = pd.read_csv("deletar2.csv", sep = "\t")

labels_dict = {
      '0':'agriculture & environment',
      '1':'biology (organismic & supraorganismic level)',
      '2':'chemistry',
      '3':'clinical and experimental medicine',
      '4':'biomedical research',
      '5':'engineering',
      '6':'art & humanities',
      '7':'physics',
      '8':'neuroscience & behavior',
      '9':'mathematics',
      '10':'social sciences',
      '11':'geociences & space sciences',
      '12':'biosciences (general, cellular&subcellular biology; genetics)'
}

data['patentAbstract'].name = 'Abstract'
data['Abstract'] = data['patentAbstract'].rename("Abstract", inplace=True)

model_name = "bert-base-uncased"
tokenizer = BertTokenizer.from_pretrained(model_name)

model_path = "./bert-base-uncased2"
model = BertForSequenceClassification.from_pretrained(model_path, num_labels=13, local_files_only=True)

pipe = TextClassificationPipeline(model=model, tokenizer=tokenizer, top_k=100)
print(len(data["Abstract"].unique()))

for abstract in data["Abstract"].unique().tolist():
	try:
		predictions = pipe([abstract], truncation=True, max_length=512)

		bertFirstPredictions = []
		bertFirstPredictionsProb = []

		bertPredictions = {
		}
		for i in range(0,len(predictions)):
		  for j in range(0,12):
		    if('bertPrediction ' + str(j) not in bertPredictions):
		      bertPredictions['bertPrediction ' + str(j)] = []
		      bertPredictions['bertPrediction ' + str(j)+ ' probability'] = []
		    try:
		      bertPredictions['bertPrediction ' + str(j)].append(labels_dict[str(predictions[i][j]["label"].replace('LABEL_',''))])
		      bertPredictions['bertPrediction ' + str(j)+ ' probability'].append(predictions[i][j]["score"])
		    except:
		      bertPredictions['bertPrediction ' + str(j)].append(None)
		      bertPredictions['bertPrediction ' + str(j)+ ' probability'].append(None)
		      
		bertPredictions['abstract'] = [abstract]

		# details = {
		#     'abstract' : data['Abstract'].unique().tolist(),
		#     'bertPredictions' : bertFirstPredictions
		# }
		  
		# creating a Dataframe object 
		df = pd.DataFrame(bertPredictions)

		df.to_csv('predictions-sulamerican.csv',index=False,sep='\t', header = False, mode = 'a')
	except:
		print('Exception')
	


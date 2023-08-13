# Master Degree Code

Este repositório contém o código e as instruções necessárias para se replicar os experimentos publicados na dissertação de título:
Combinação de modelos por método físico inspirado para a classificação científica de Patentes

# Configuração do ambiente

Requerida a instalação de python 3.
Software testado na versão 3.10.6

Dependências necessárias (python):
transformers (testado na versão 4.29.2)
torch (testado na versão 4.29.2)
scikit-learn (testado na versão 1.2.2)
tensorflow (testado na versão 2.12.0)
pandas (testado na versão 2.0.1)
numpy (testado na versão 1.23.5)

Requerida a instalação da Java Virtual Machine (JVM).
Software testado na versão build 19.0.1+10-21

Dependências necessárias:
Executar maven build all no pasta do projeto.

Recomendado a instalação de IDE Python e Java para desenvolvimento:
Eclipse com Maven suporte
Pycharm

# Instruções para execução de teste

# Execução do BERT

Faça o download do modelo tunado do BERT por meio do link:
https://drive.google.com/drive/folders/1py1dieHcpWiQeTAc07A96DsCrTkFjw6z?usp=sharing

Adicione a pasta baixada na raiz deste repositório.

Para classificar uma sentença por meio do BERT, é necessário adicionar as instâncias das patentes
no arquivo patentes.csv. O arquivo contém alguns exemplos de instâncias que serão classificadas.

Em seguida, execute o BERT por meio do comando:

``python3 BERT-classifier.py``

A execução do programa levará algum tempo e as instâncias ficarão disponíveis no
arquivo predictions-bert.csv

# Execução de SVM OneClass

Para classificar uma sentença por meio de SVMOneClass, use o mesmo arquivo patentes.csv.
O arquivo contém alguns exemplos de instâncias que serão classificadas.

Em seguida, execute o SVMOneClass por meio do comando:

``python3 SVM-classifier.py``

A execução do programa levará algum tempo e as instâncias ficarão disponíveis no
arquivo predictions-svm.csv

# Execução de Naive Bayes e Comitê de superposição

Para classificar uma sentença por meio de SVMOneClass, use o mesmo arquivo patentes.csv.
O arquivo contém alguns exemplos de instâncias que serão classificadas.

Em seguida, execute o SVMOneClass por meio do comando:

``python3 SVM-classifier.py``

A execução do programa levará algum tempo e as instâncias ficarão disponíveis no
arquivo predictions-svm.csv

# Suporte

Para dúvidas ou para relatar um problema:
lucaslopes.si@usp.br

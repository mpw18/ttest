---
layout: model
title: French Legal CamemBERT Embedding  Cased model
author: John Snow Labs
name: camembert_embeddings_legal_camembert
date: 2023-01-13
tags: [fr, open_source, embeddings, camembert]
task: Embeddings
language: fr
edition: Spark NLP 4.2.7
spark_version: 3.0
supported: true
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

Pretrained CamemBERT Embedding model, adapted from Hugging Face and curated to provide scalability and production-readiness using Spark NLP. `legal-camembert` is a French model originally trained by `maastrichtlawtech`.

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/camembert_embeddings_legal_camembert_fr_4.2.7_3.0_1673596318105.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/camembert_embeddings_legal_camembert_fr_4.2.7_3.0_1673596318105.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
documentAssembler = DocumentAssembler() \
    .setInputCol("text") \
    .setOutputCol("document")

tokenizer = Tokenizer() \
    .setInputCols("document") \
    .setOutputCol("token")
  
embeddings = CamemBertEmbeddings.pretrained("camembert_embeddings_legal_camembert","fr") \
    .setInputCols(["document", "token"]) \
    .setOutputCol("embeddings")
    
pipeline = Pipeline(stages=[documentAssembler, tokenizer, embeddings])

data = spark.createDataFrame([["J'adore Spark NLP"]]).toDF("text")

result = pipeline.fit(data).transform(data)
```

</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|camembert_embeddings_legal_camembert|
|Compatibility:|Spark NLP 4.2.7+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence]|
|Output Labels:|[bert_sentence]|
|Language:|fr|
|Size:|415.6 MB|
|Case sensitive:|true|
|Max sentence length:|128|

## References

- https://huggingface.co/maastrichtlawtech/legal-camembert
- https://antoinelouis.co
- https://www.maastrichtuniversity.nl/about-um/faculties/law/research/law-and-tech-lab
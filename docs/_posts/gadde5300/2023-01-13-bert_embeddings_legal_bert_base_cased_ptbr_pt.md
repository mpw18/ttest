---
layout: model
title: Portuguese Legal BERT Embedding Base Cased model
author: John Snow Labs
name: bert_embeddings_legal_bert_base_cased_ptbr
date: 2023-01-13
tags: [pt, open_source, embeddings, bert]
task: Embeddings
language: pt
edition: Spark NLP 4.2.7
spark_version: 3.0
supported: true
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

Pretrained BERT Embedding model, adapted from Hugging Face and curated to provide scalability and production-readiness using Spark NLP. `legal-bert-base-cased-ptbr` is a Portuguese model originally trained by `dominguesm`.

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/bert_embeddings_legal_bert_base_cased_ptbr_pt_4.2.7_3.0_1673598724252.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/bert_embeddings_legal_bert_base_cased_ptbr_pt_4.2.7_3.0_1673598724252.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

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
  
embeddings = BertEmbeddings.pretrained("bert_embeddings_legal_bert_base_cased_ptbr","pt") \
    .setInputCols(["document", "token"]) \
    .setOutputCol("embeddings")
    
pipeline = Pipeline(stages=[documentAssembler, tokenizer, embeddings])

data = spark.createDataFrame([["Eu amo Spark NLP"]]).toDF("text")

result = pipeline.fit(data).transform(data)
```

</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|bert_embeddings_legal_bert_base_cased_ptbr|
|Compatibility:|Spark NLP 4.2.7+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence]|
|Output Labels:|[bert_sentence]|
|Language:|pt|
|Size:|472.7 MB|
|Case sensitive:|true|
|Max sentence length:|128|

## References

- https://huggingface.co/dominguesm/legal-bert-base-cased-ptbr
- https://ailab.unb.br/victor/lrec2020
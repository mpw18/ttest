---
layout: model
title: XLNet Large CoNLL-03 NER Pipeline
author: John Snow Labs
name: xlnet_large_token_classifier_conll03_pipeline
date: 2023-05-25
tags: [open_source, ner, token_classifier, xlnet, conll03, large, en]
task: Named Entity Recognition
language: en
edition: Spark NLP 4.4.2
spark_version: 3.4
supported: true
annotator: PipelineModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

This pretrained pipeline is built on the top of [xlnet_large_token_classifier_conll03](https://nlp.johnsnowlabs.com/2021/09/28/xlnet_large_token_classifier_conll03_en.html) model.

## Predicted Entities



{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/xlnet_large_token_classifier_conll03_pipeline_en_4.4.2_3.4_1685010899121.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/xlnet_large_token_classifier_conll03_pipeline_en_4.4.2_3.4_1685010899121.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
pipeline = PretrainedPipeline("xlnet_large_token_classifier_conll03_pipeline", lang = "en")

pipeline.annotate("My name is John and I work at John Snow Labs.")
```
```scala
val pipeline = new PretrainedPipeline("xlnet_large_token_classifier_conll03_pipeline", lang = "en")

pipeline.annotate("My name is John and I work at John Snow Labs.")
```
</div>

## Results

```bash
Results




+--------------+---------+
|chunk         |ner_label|
+--------------+---------+
|John          |PERSON   |
|John Snow Labs|ORG      |
+--------------+---------+


{:.model-param}
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|xlnet_large_token_classifier_conll03_pipeline|
|Type:|pipeline|
|Compatibility:|Spark NLP 4.4.2+|
|License:|Open Source|
|Edition:|Official|
|Language:|en|
|Size:|19.0 KB|

## Included Models

- DocumentAssembler
- TokenizerModel
- NormalizerModel
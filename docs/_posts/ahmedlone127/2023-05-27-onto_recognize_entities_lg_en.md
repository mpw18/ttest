---
layout: model
title: Onto Recognize Entities Lg
author: John Snow Labs
name: onto_recognize_entities_lg
date: 2023-05-27
tags: [en, open_source]
task: Named Entity Recognition
language: en
edition: Spark NLP 4.4.2
spark_version: 3.2
supported: true
annotator: PipelineModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

The onto_recognize_entities_lg is a pretrained pipeline that we can use to process text with a simple pipeline that performs basic processing steps and recognizes entites.
It performs most of the common text processing tasks on your dataframe

## Predicted Entities



{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/onto_recognize_entities_lg_en_4.4.2_3.2_1685188232491.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/onto_recognize_entities_lg_en_4.4.2_3.2_1685188232491.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



{:.model-param}

<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
from sparknlp.pretrained import PretrainedPipeline
pipeline = PretrainedPipeline("onto_recognize_entities_lg", "en")

result = pipeline.annotate("""I love johnsnowlabs!  """)
```


{:.nlu-block}
```python
import nlu
nlu.load("en.ner.onto.lg").predict("""I love johnsnowlabs!  """)
```
</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|onto_recognize_entities_lg|
|Type:|pipeline|
|Compatibility:|Spark NLP 4.4.2+|
|License:|Open Source|
|Edition:|Official|
|Language:|en|
|Size:|2.5 GB|

## Included Models

- DocumentAssembler
- SentenceDetector
- TokenizerModel
- WordEmbeddingsModel
- NerDLModel
- NerConverter
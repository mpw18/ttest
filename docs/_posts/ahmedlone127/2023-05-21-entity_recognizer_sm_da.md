---
layout: model
title: Recognize Entities DL Pipeline for Danish - Small
author: John Snow Labs
name: entity_recognizer_sm
date: 2023-05-21
tags: [open_source, danish, entity_recognizer_sm, pipeline, da]
task: Named Entity Recognition
language: da
edition: Spark NLP 4.4.2
spark_version: 3.0
supported: true
annotator: PipelineModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

The entity_recognizer_sm is a pretrained pipeline that we can use to process text with a simple pipeline that performs basic processing steps.
It performs most of the common text processing tasks on your dataframe

## Predicted Entities



{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/entity_recognizer_sm_da_4.4.2_3.0_1684643832055.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/entity_recognizer_sm_da_4.4.2_3.0_1684643832055.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
from sparknlp.pretrained import PretrainedPipelinein
pipeline = PretrainedPipeline('entity_recognizer_sm', lang = 'da')
annotations =  pipeline.fullAnnotate(""Hej fra John Snow Labs! "")[0]
annotations.keys()
```
```scala
val pipeline = new PretrainedPipeline("entity_recognizer_sm", lang = "da")
val result = pipeline.fullAnnotate("Hej fra John Snow Labs! ")(0)
```

{:.nlu-block}
```python
import nlu
text = [""Hej fra John Snow Labs! ""]
result_df = nlu.load('da.ner').predict(text)
result_df
```
</div>

## Results

```bash
Results


|    | document                     | sentence                    | token                                   | embeddings                   | ner                                   | entities            |
|---:|:-----------------------------|:----------------------------|:----------------------------------------|:-----------------------------|:--------------------------------------|:--------------------|
|  0 | ['Hej fra John Snow Labs! '] | ['Hej fra John Snow Labs!'] | ['Hej', 'fra', 'John', 'Snow', 'Labs!'] | [[0.0306969992816448,.,...]] | ['O', 'O', 'B-PER', 'I-PER', 'I-PER'] | ['John Snow Labs!'] |


{:.model-param}
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|entity_recognizer_sm|
|Type:|pipeline|
|Compatibility:|Spark NLP 4.4.2+|
|License:|Open Source|
|Edition:|Official|
|Language:|da|
|Size:|166.7 MB|

## Included Models

- DocumentAssembler
- SentenceDetector
- TokenizerModel
- WordEmbeddingsModel
- NerDLModel
- NerConverter
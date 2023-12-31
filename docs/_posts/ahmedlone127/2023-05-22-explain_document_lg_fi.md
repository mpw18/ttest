---
layout: model
title: Explain Document pipeline for Finnish (explain_document_lg)
author: John Snow Labs
name: explain_document_lg
date: 2023-05-22
tags: [open_source, finnish, explain_document_lg, pipeline, fi]
task: Named Entity Recognition
language: fi
edition: Spark NLP 4.4.2
spark_version: 3.2
supported: true
annotator: PipelineModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

The explain_document_lg is a pretrained pipeline that we can use to process text with a simple pipeline that performs basic processing steps 
and recognizes entities .
It performs most of the common text processing tasks on your dataframe

## Predicted Entities



{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/explain_document_lg_fi_4.4.2_3.2_1684755070483.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/explain_document_lg_fi_4.4.2_3.2_1684755070483.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
from sparknlp.pretrained import PretrainedPipelinein
pipeline = PretrainedPipeline('explain_document_lg', lang = 'fi')
annotations =  pipeline.fullAnnotate(""Hei John Snow Labs! "")[0]
annotations.keys()
```
```scala
val pipeline = new PretrainedPipeline("explain_document_lg", lang = "fi")
val result = pipeline.fullAnnotate("Hei John Snow Labs! ")(0)
```

{:.nlu-block}
```python
import nlu
text = [""Hei John Snow Labs! ""]
result_df = nlu.load('fi.explain.lg').predict(text)
result_df
```
</div>

## Results

```bash
Results


|    | document                 | sentence                | token                            | lemma                            | pos                                 | embeddings                   | ner                              | entities            |
|---:|:-------------------------|:------------------------|:---------------------------------|:---------------------------------|:------------------------------------|:-----------------------------|:---------------------------------|:--------------------|
|  0 | ['Hei John Snow Labs! '] | ['Hei John Snow Labs!'] | ['Hei', 'John', 'Snow', 'Labs!'] | ['hei', 'John', 'Snow', 'Labs!'] | ['INTJ', 'PROPN', 'PROPN', 'PROPN'] | [[0.0639619976282119,.,...]] | ['O', 'B-PRO', 'I-PRO', 'I-PRO'] | ['John Snow Labs!'] |


{:.model-param}
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|explain_document_lg|
|Type:|pipeline|
|Compatibility:|Spark NLP 4.4.2+|
|License:|Open Source|
|Edition:|Official|
|Language:|fi|
|Size:|2.5 GB|

## Included Models

- DocumentAssembler
- SentenceDetector
- TokenizerModel
- LemmatizerModel
- PerceptronModel
- WordEmbeddingsModel
- NerDLModel
- NerConverter
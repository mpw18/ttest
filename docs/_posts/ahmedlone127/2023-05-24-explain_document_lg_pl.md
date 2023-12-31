---
layout: model
title: Explain Document pipeline for Polish (explain_document_lg)
author: John Snow Labs
name: explain_document_lg
date: 2023-05-24
tags: [open_source, polish, explain_document_lg, pipeline, pl]
task: Named Entity Recognition
language: pl
edition: Spark NLP 4.4.2
spark_version: 3.4
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
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/explain_document_lg_pl_4.4.2_3.4_1684949836920.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/explain_document_lg_pl_4.4.2_3.4_1684949836920.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use

<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python

from sparknlp.pretrained import PretrainedPipelinein
pipeline = PretrainedPipeline('explain_document_lg', lang = 'pl')
annotations =  pipeline.fullAnnotate(""Witaj z John Snow Labs! "")[0]
annotations.keys()

```
```scala

val pipeline = new PretrainedPipeline("explain_document_lg", lang = "pl")
val result = pipeline.fullAnnotate("Witaj z John Snow Labs! ")(0)


```

{:.nlu-block}
```python

import nlu
text = [""Witaj z John Snow Labs! ""]
result_df = nlu.load('pl.explain.lg').predict(text)
result_df
    
```
</div>

<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
from sparknlp.pretrained import PretrainedPipelinein
pipeline = PretrainedPipeline('explain_document_lg', lang = 'pl')
annotations =  pipeline.fullAnnotate(""Witaj z John Snow Labs! "")[0]
annotations.keys()
```
```scala
val pipeline = new PretrainedPipeline("explain_document_lg", lang = "pl")
val result = pipeline.fullAnnotate("Witaj z John Snow Labs! ")(0)
```

{:.nlu-block}
```python
import nlu
text = [""Witaj z John Snow Labs! ""]
result_df = nlu.load('pl.explain.lg').predict(text)
result_df
```
</div>

## Results

```bash
Results


|    | document                     | sentence                    | token                                   | lemma                                   | pos                                        | embeddings                   | ner                                   | entities            |
|---:|:-----------------------------|:----------------------------|:----------------------------------------|:----------------------------------------|:-------------------------------------------|:-----------------------------|:--------------------------------------|:--------------------|
|  0 | ['Witaj z John Snow Labs! '] | ['Witaj z John Snow Labs!'] | ['Witaj', 'z', 'John', 'Snow', 'Labs!'] | ['witać', 'z', 'John', 'Snow', 'Labs!'] | ['VERB', 'ADP', 'PROPN', 'PROPN', 'PROPN'] | [[0.4977500140666961,.,...]] | ['O', 'O', 'B-PER', 'I-PER', 'I-PER'] | ['John Snow Labs!'] |


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
|Language:|pl|
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
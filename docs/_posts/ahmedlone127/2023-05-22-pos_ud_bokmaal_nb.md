---
layout: model
title: Part of Speech for Norwegian
author: John Snow Labs
name: pos_ud_bokmaal
date: 2023-05-22
tags: [pos, norwegian, nb, open_source]
task: Part of Speech Tagging
language: nb
edition: Spark NLP 4.4.2
spark_version: 3.2
supported: true
annotator: PipelineModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

This model annotates the part of speech of tokens in a text. The parts of speech annotated include PRON (pronoun), CCONJ (coordinating conjunction), and 15 others. The part of speech model is useful for extracting the grammatical structure of a piece of text automatically.

This model was trained using the dataset available at https://universaldependencies.org

## Predicted Entities



{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/public/models/pos_ud_bokmaal_nb_4.4.2_3.2_1684759476489.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://auxdata.johnsnowlabs.com/public/models/pos_ud_bokmaal_nb_4.4.2_3.2_1684759476489.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
pos = PerceptronModel.pretrained("pos_ud_bokmaal", "nb") \
    .setInputCols(["document", "token"]) \
    .setOutputCol("pos")

nlp_pipeline = Pipeline(stages=[document_assembler, sentence_detector, tokenizer, pos])
light_pipeline = LightPipeline(nlp_pipeline.fit(spark.createDataFrame([['']]).toDF("text")))
results = light_pipeline.fullAnnotate("Annet enn å være kongen i nord, er John Snow en engelsk lege og en leder innen utvikling av anestesi og medisinsk hygiene.")
```
```scala
val pos = PerceptronModel.pretrained("pos_ud_bokmaal", "nb")
    .setInputCols(Array("document", "token"))
    .setOutputCol("pos")

val pipeline = new Pipeline().setStages(Array(document_assembler, sentence_detector, tokenizer, pos))
val data = Seq("Annet enn å være kongen i nord, er John Snow en engelsk lege og en leder innen utvikling av anestesi og medisinsk hygiene.").toDF("text")
val result = pipeline.fit(data).transform(data)
```

{:.nlu-block}
```python
import nlu

text = ["""Annet enn å være kongen i nord, er John Snow en engelsk lege og en leder innen utvikling av anestesi og medisinsk hygiene."""]
pos_df = nlu.load('nb.pos.ud_bokmaal').predict(text)
pos_df
```
</div>

## Results

```bash
Results



[Row(annotatorType='pos', begin=0, end=4, result='DET', metadata={'word': 'Annet'}),
Row(annotatorType='pos', begin=6, end=8, result='SCONJ', metadata={'word': 'enn'}),
Row(annotatorType='pos', begin=10, end=10, result='PART', metadata={'word': 'å'}),
Row(annotatorType='pos', begin=12, end=15, result='AUX', metadata={'word': 'være'}),
Row(annotatorType='pos', begin=17, end=22, result='NOUN', metadata={'word': 'kongen'}),
...]


{:.model-param}
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|pos_ud_bokmaal|
|Type:|pipeline|
|Compatibility:|Spark NLP 4.4.2+|
|License:|Open Source|
|Edition:|Official|
|Language:|nb|
|Size:|17.7 KB|

## Included Models

- DocumentAssembler
- SentenceDetector
- TokenizerModel
- PerceptronModel
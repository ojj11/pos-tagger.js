# pos-tagger.js

![Build and test the library](https://github.com/ojj11/pos-tagger.js/workflows/Build%20and%20test%20the%20library/badge.svg)

> A Part-Of-Speech Tagger (POS Tagger) is a piece of software that reads text in some language and assigns parts of speech to each word (and other token), such as noun, verb, adjective, etc., although generally computational applications use more fine-grained POS tags like 'noun-plural'.

This is a rewrite of the [Stanford Part-Of-Speech Log-Linear tagger](https://nlp.stanford.edu/software/tagger.shtml) in Kotlin, it is compiled to JavaScript and made available through npm. No background Java service is needed.

This module includes two models:
 - left3words-wsj-0-18
 - bidirectional-distsim-wsj-0-18

The total package size (including both models) is under 10mb. Basic benchmarks show that the JavaScript library has similar performance to that of the original Java code for the "left3words" model.

    Author: Olli Jones, Kristina Toutanova, Dan Klein, Christopher Manning, and Yoram Singer
    License: GPL v2 or above

## Usage

```javascript
const Tagger = require("pos-tagger.js");
const tagger = new Tagger(Tagger.readModelSync("left3words-wsj-0-18"));

// alternatively
// const tagger = new Tagger(Tagger.readModelSync("bidirectional-distsim-wsj-0-18"));

const output = tagger.tag("I am a happy part-of-speech tagger. How do you do?");

console.log(output);
console.log("First word is a " + output[0][0].tag);
```

`tag` takes a string representing multiple sentences (where there isn't a terminating ".", one is added). The output is a list with one element per input sentence. Each sentence element is itself a list with an element per input token, each element contains a `word` key with the original token, and a `tag` key which contains the [Penn Treebank part-of-speech tag](https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html).

###### Example output:

```json
[
  [
    { "word": "I", "tag": "PRP" },
    { "word": "am", "tag": "VBP" },
    { "word": "a", "tag": "DT" },
    { "word": "happy", "tag": "JJ" },
    { "word": "part-of-speech", "tag": "JJ" },
    { "word": "tagger", "tag": "NN" },
    { "word": ".", "tag": "." }
  ],
  [
    { "word": "How", "tag": "WRB" },
    { "word": "do", "tag": "VBP" },
    { "word": "you", "tag": "PRP" },
    { "word": "do", "tag": "VB" },
    { "word": "?", "tag": "."
    }
  ]
]
First word is a PRP
```

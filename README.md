# stanford-tagger.js

> A Part-Of-Speech Tagger (POS Tagger) is a piece of software that reads text in some language and assigns parts of speech to each word (and other token), such as noun, verb, adjective, etc., although generally computational applications use more fine-grained POS tags like 'noun-plural'.

This is a JavaScript port of the [Stanford Part-Of-Speech Log-Linear tagger](https://nlp.stanford.edu/software/tagger.shtml) ported to JavaScript. No background service is needed.

This module includes two models:
 - bidirectional-distsim-wsj-0-18
 - left3words-wsj-0-18

The total package size is around 11mb.

    Author: Olli Jones and Kristina Toutanova, Dan Klein, Christopher Manning, and Yoram Singer)
    License: GPL v2 or above

## Usage

```javascript
const Tagger = require("stanford-tagger.js");
const tagger = new Tagger(Tagger.left3words_wsj_v0_18());

// alternatively
// const tagger = new Tagger(Tagger.bidirectional_distsim_wsj_v0_18());

console.log(tagger.tag("I am the Stanford POS Tagger. How do you do?"));
```

`tag` takes a string representing multiple sentences (where there isn't a terminating ".", one is added). The output is a list with one element per input sentence. Each sentence element is itself a list with an element per input token, each element contains a `word` key with the original token, and a `tag` key which contains the [Penn Treebank part-of-speech tag](https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html).

###### Example output:

```json
[
  [
    { "word": "I", "tag": "CD" },
    { "word": "am", "tag": "VBP" },
    { "word": "the", "tag": "VBP" },
    { "word": "Stanford", "tag": "NNP" },
    { "word": "POS", "tag": "NNP" },
    { "word": "Tagger", "tag": "NNP" },
    { "word": ".", "tag": "."
    }
  ],
  [
    { "word": "How", "tag": "WRB" },
    { "word": "do", "tag": "VB" },
    { "word": "you", "tag": "PRP" },
    { "word": "do", "tag": "VB" },
    { "word": "?", "tag": "."
    }
  ]
]
```

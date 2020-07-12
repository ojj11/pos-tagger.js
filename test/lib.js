const assert = require("assert");
const Tagger = require("..");

const tagger = new Tagger(Tagger.readModelSync("left3words-wsj-0-18"));

describe("lib", function() {

  it("should be able to tag 'I am a happy part-of-speech tagger'", function() {
    const output = tagger.tag("I am a happy part-of-speech tagger");
    assert.equal(1, output.length);
    assert.equal(output[0].map(o => o.word).join(" "), "I am a happy part-of-speech tagger .");
    assert.equal(output[0].map(o => o.tag).join(" "), "PRP VBP DT JJ JJ NN .");
  });

  it("should be able to tag 'How do you do?'", function() {
    const output = tagger.tag("How do you do?");
    assert.equal(1, output.length);
    assert.equal(output[0].map(o => o.word).join(" "), "How do you do ?");
    assert.equal(output[0].map(o => o.tag).join(" "), "WRB VBP PRP VB .");
  });

});

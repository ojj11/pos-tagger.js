const assert = require("assert");
const Tagger = require("..");

describe("lib", function() {

  this.timeout(0);
  this.slow(10000);

  it("should be able to tag 'I am a happy part-of-speech tagger'", function() {
    const tagger = new Tagger(Tagger.readModelSync("left3words-wsj-0-18"));
    const output = tagger.tag("I am a happy part-of-speech tagger");
    assert.equal(1, output.length);
    assert.equal(output[0].map(o => o.word).join(" "), "I am a happy part-of-speech tagger");
    assert.equal(output[0].map(o => o.tag).join(" "), "PRP VBP DT JJ JJ NN");
  });

  it("should be able to tag 'How do you do?'", function() {
    const tagger = new Tagger(Tagger.readModelSync("left3words-wsj-0-18"));
    const output = tagger.tag("How do you do?");
    assert.equal(1, output.length);
    assert.equal(output[0].map(o => o.word).join(" "), "How do you do ?");
    assert.equal(output[0].map(o => o.tag).join(" "), "WRB VBP PRP VB .");
  });

  it("should be able to tag 'How do you do?' with bidirectional model", function() {
    const tagger = new Tagger(Tagger.readModelSync("bidirectional-distsim-wsj-0-18"));
    const output = tagger.tag("I am a happy part-of-speech tagger");
    assert.equal(1, output.length);
    assert.equal(output[0].map(o => o.word).join(" "), "I am a happy part-of-speech tagger");
    assert.equal(output[0].map(o => o.tag).join(" "), "PRP VBP DT JJ NN NN");
  });

});

describe("speed", function() {

  this.timeout(0);
  this.slow(4000);

  it("should be able to run quickly", function() {
    const tagger = new Tagger(Tagger.readModelSync("left3words-wsj-0-18"));
    const time = process.hrtime();
    const output = tagger.tag(`
    A passenger plane has crashed shortly after take-off from Kyrgyzstan's
    capital, Bishkek, killing a large number of those on board. The head of
    Kyrgyzstan's civil aviation authority said that out of about 90
    passengers and crew, only about 20 people have survived. The Itek Air
    Boeing 737 took off bound for Mashhad, in north-eastern Iran, but turned
    round some 10 minutes later.`);
    const diff = process.hrtime(time);
    const tokens = output.map(s => s.length).reduce((p, c) => p + c, 0);
    const tokens_per_second = tokens * (1 / (diff[0] + (diff[1] / 1000000000)));
    console.log("Tagged " + tokens + " words at " + tokens_per_second + " words per second");
    assert.equal(
      output.map(s => s.map(o => o.word+"_"+o.tag).join(" ")).join("\n"),
      [
        "A_DT passenger_NN plane_NN has_VBZ crashed_VBN shortly_RB after_IN take-off_NN from_IN " +
        "Kyrgyzstan_NNP 's_POS capital_NN ,_, Bishkek_NNP ,_, killing_VBG a_DT large_JJ " +
        "number_NN of_IN those_DT on_IN board_NN ._.",
        "The_DT head_NN of_IN Kyrgyzstan_NNP 's_POS civil_JJ aviation_NN authority_NN said_VBD " +
        "that_IN out_IN of_IN about_IN 90_CD passengers_NNS and_CC crew_NN ,_, only_RB about_IN " +
        "20_CD people_NNS have_VBP survived_VBN ._.",
        "The_DT Itek_NNP Air_NNP Boeing_NNP 737_CD took_VBD off_RP bound_VBN for_IN Mashhad_NNP " +
        ",_, in_IN north-eastern_JJ Iran_NNP ,_, but_CC turned_VBD round_NN some_DT 10_CD " +
        "minutes_NNS later_RB ._."
      ].join("\n"));
  });

});

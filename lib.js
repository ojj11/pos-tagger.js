const kotlin = require("./build/js/packages/posTagger/kotlin/posTagger.js");
const parser = require("./parser.js");
const zlib = require("zlib");
const fs = require("fs");

function Tagger(model) {
    this.tagger = new kotlin.tagger(model);
}

Tagger.readModelSync = function(path) {
  return zlib.gunzipSync(fs.readFileSync(__dirname + "/models/" + path + ".cbor.gz"));

}

Tagger.prototype.tag = function(string) {
    try {
      const out = parser.parse(string);
      return out.map(sentence => this.tagger.tag(sentence));
    } catch(e) {
      const out = parser.parse(string + ".");
      return out.map(sentence => this.tagger.tag(sentence));
    }
}

module.exports = Tagger;

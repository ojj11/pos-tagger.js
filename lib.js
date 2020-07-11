const kotlin = require("./build/js/packages/StanfordPOS/kotlin/StanfordPOS.js");
const parser = require("./parser.js");

function Tagger(model) {
    this.tagger = new kotlin.tagger(model);
}

Tagger.readModelSync = function(path) {
    return kotlin.readModelSync(path);
}

Tagger.prototype.tag = function(string) {
    try {
        return parser.parse(string).map(sentence => this.tagger.tag(sentence));
    } catch(e) {
        return parser.parse(string + ".").map(sentence => this.tagger.tag(sentence));
    }
}

module.exports = Tagger;
const kotlin = require("./build/js/packages/StanfordPOS/kotlin/StanfordPOS.js");
const parser = require("./parser.js");

function Tagger(model) {
    this.tagger = new kotlin.tagger(model);
}

Tagger.left3words_wsj_v0_18 = function() {
    return kotlin.left3words_wsj_v0_18();
}

Tagger.bidirectional_distsim_wsj_v0_18 = function() {
    return kotlin.bidirectional_distsim_wsj_v0_18();
}

Tagger.prototype.tag = function(string) {
    try {
        return parser.parse(string).map(sentence => this.tagger.tag(sentence));
    } catch(e) {
        return parser.parse(string + ".").map(sentence => this.tagger.tag(sentence));
    }
}

module.exports = Tagger;
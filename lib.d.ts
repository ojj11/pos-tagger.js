import { Output } from "./build/js/packages/posTagger/kotlin/posTagger"

/** a pos-tagger model */
export class Model {}

/** the part of speech tagger */
export class Tagger {

    /**
     * construct a part of speech tagger from a model, for example:
     * ```JavaScript
     * const Tagger = require("pos-tagger.js");
     * const tagger = new Tagger(Tagger.readModelSync("left3words-wsj-0-18"));
     * ```
     */
    constructor(bytes: Model);

    /**
     * tag a document of sentences
     * ```javascript
     * const output = tagger.tag("I am a happy part-of-speech tagger. How do you do?");
     * ```
     * @param input the document of sentences
     * @returns a list of [[Output]], an object of type `{ word: string, tag: string}`
     */
    tag(input: string): Array<Array<Output>>;

    /**
     * load a model synchronously
     * ```javascript
     * const model = Tagger.readModelSync("left3words-wsj-0-18");
     * ```
     * @param path the name of the included model, either "left3words-wsj-0-18" or "bidirectional-distsim-wsj-0-18"
     * @returns a [[Model]]
     */
    static readModelSync(path: string): Model;
}
export default Tagger

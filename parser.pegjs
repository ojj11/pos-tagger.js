Sentence "sentence"
  = sentences:((Characters / Specials / _)+ EOS?)+
  { return sentences.map(x => x[0].concat(x[1]).filter(x => x != undefined)) }

Specials "special"
  = Abbreviation / Ellipsis / Symbol / Brackets

Abbreviation "abbreviation"
  = "'ll" / "'LL"  / "'re" / "'RE" / "'ve" / "'VE" / "n't" / "N'T" / "'s" / "'S" / "'m" / "'M" / "'d" / "'D"

Symbol "symbol"
  = [^a-zA-Z0-9'".\-\_ \t\n\r]

EOS "eos"
  = punc:[\.!?] ([ \t\n\r] / !.)
  { return punc; }

LRB "lrb"
  = "("
  { return "-LRB-"; }
RRB "rrb"
  = ")"
  { return "-RRB-"; }
LSB "lsb"
  = "["
  { return "-LSB-"; }
RSB "rsb"
  = "]"
  { return "-RSB-"; }
LCB "lcb"
  = "{"
  { return "-LCB-"; }
RCB "rcb"
  = "}"
  { return "-RCB-"; }

Brackets "brackets"
  = LRB / RRB / LSB / RSB / LCB / RCB

Ellipsis "ellipsis"
  = "..."

Characters "characters"
  = (!Specials !EOS [a-zA-Z0-9'".\-\_])+
  { return text(); }

_ "whitespace"
  = [ \t\n\r]+
  { return; }

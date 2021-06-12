grammar Regress;
@header {
package edu.umich.rosie.tools.config;
}
corpus   : block+ COMMENT* ;
block    : COMMENT* sentence FAILED? expected? ;
sentence : sentenceWord (sentenceWord)* ('.' | TERMINATOR)? ;
sentenceWord : QUOTE | TIME | WORD | COMMA;
expected : (COMMENT* rhs)+ ;
rhs      : '(' '@'? SYMBOL (attr value COMMENT?)+  ')' ;
attr     : '^' (NUMBER | (WORD ('.' WORD)*) ) ;
value    : (variable | '@'? SYMBOL | WORD | NUMBER) ;
TIME     : NUMBER ':' NUMBER ;
variable : '<' WORD '>' ;
QUOTE    : '"' ~["]* '"';
SYMBOL   : [a-zA-Z][0-9]+ ;
WORD     : [a-zA-Z0-9!][a-zA-Z0-9\-'_]*;
NUMBER   : '-'? ('.' DIGIT+ | DIGIT+ ('.' DIGIT*)? ) ;
fragment DIGIT : [0-9] ;
PAREN    : [()] ;
TERMINATOR : [?!;];
COMMA    : ',' ;
FAILED   : '#   FAILED!' ;
COMMENT  : '#' ~[\r\n]* ;
BRACKETS : '[' .*? ']' -> skip ;
WS       : [ \t\r\n]+ -> skip ;

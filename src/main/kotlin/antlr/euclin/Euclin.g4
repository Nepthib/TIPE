grammar Euclin;

// Règles du parseur

// Règle de base
codeBlock
    : instructions*
    ;

functionCodeBlock
    : functionInstructions+
    ;

functionInstructions
    : instructions          #InstructionFuncInstruction
    | Return expression     #ReturnFuncInstruction
    ;

instructions
    : expression            #ExpressionInstruction
    | variableDeclaration   #DeclareVarInstruction
    | variableAssign        #AssignVarInstruction
    | memberAssign          #AssignMemberInstruction
    | transformBlock        #TransformBlockInstruction
    | functionDeclaration   #DeclareFuncInstruction
    ;

type
    : LeftBracket (type (Comma type)*)? RightBracket LightArrow type    #FunctionType
    | LeftBracket type Comma type RightBracket                          #CoupleType
    | LeftBracket type RightBracket                                     #WrappedType
    | Identifier                                                        #BasicType
    ;

parameter
    : Identifier Colon type
    ;

functionDeclaration
    : modifiers* FunctionDefStart Identifier LeftBracket (parameter (Comma parameter)*)? RightBracket Colon type
    LeftCurlyBracket functionCodeBlock RightCurlyBracket
    ;

modifiers
    : Pure
    | Impure
    | Memoized;

functionCall
    : Identifier LeftBracket (expression (Comma expression)*)? RightBracket
    ;

variableDeclaration
    : Var Identifier Equals expression
    ;

variableAssign
    : Identifier Equals expression
    ;

memberAssign
    : expression Period Identifier Equals expression
    ;

transformBlock
    : TransformBlockStart LeftCurlyBracket codeBlock RightCurlyBracket
    ;

// définies par ordre de précédence
expression
    : LambdaVariable                            #LambdaVarExpr
    | LeftSquareBracket expression RightSquareBracket   #LambdaFunctionExpr
    | expression Period Identifier              #AccessExpr
    | functionCall                              #CallExpr
    | Identifier                                #VarExpr
    | couple                                    #CoupleExpr
    | Integer                                   #IntExpr
    | FloatNumber                               #FloatExpr
    | LeftBracket expression RightBracket       #WrappedExpr
    | expression DivOperator expression         #DivExpr
    | expression MultOperator expression        #MultExpr
    | expression SubOperator expression         #SubExpr
    | expression AddOperator expression         #AddExpr
    | True                                      #BoolTrueExpr
    | False                                     #BoolFalseExpr
    | StringConstant                            #StringExpr
    | LeftBracket RightBracket                  #UnitExpr
    ;

couple
    : LeftBracket expression Comma expression RightBracket
    ;

// Mots clés
Return: 'return';
TransformBlockStart: 'transform';
FunctionDefStart: 'func';
Var: 'var';
True: 'true';
False: 'false';
Pure: 'pure';
Impure: 'impure';
Memoized: 'memoized';

// Ponctuation
LambdaVariable: '_';
LeftBracket: '(';
RightBracket: ')';
LeftCurlyBracket: '{';
RightCurlyBracket: '}';
LeftSquareBracket: '[';
RightSquareBracket: ']';
Comma: ',';
Period: '.';
Colon: ':';
SemiColon: ';';
Equals: '=';
LightArrow: '->';

Identifier: IdentifierStart IdentifierPart*;
Integer: Digits;
FloatNumber
    : Digits Period (Digits)?
    | Period Digits
    ;

MultOperator
    : '*' BinaryOperatorSuffix
    ;

DivOperator
    : '/' BinaryOperatorSuffix
    ;

AddOperator
    : '+' BinaryOperatorSuffix
    ;

SubOperator
    : '-' BinaryOperatorSuffix
    ;

StringConstant
    : '"' ~('"')* '"'
    ;
// Fragments: visible que par le lexer
fragment IdentifierStart
    : [a-z] | [A-Z]
    ;

fragment IdentifierPart
    : ([a-z] | [A-Z] | [0-9])
    ;

fragment Digits
    : Digit+
    ;

fragment Digit
    : [0-9]
    ;

fragment BinaryOperatorSuffix
    : ('.')?
    ;

// Commentaires et blancs
LineComment
  :  '//' ~( '\r' | '\n' )* -> skip
  ;

MultilineComment
// '/*': début du commentaire
// '*/': fin
// '.*?': n'importe ('.') quel**s** ('*') caractères, ou rien ('?')
  : '/*' .*? '*/' -> skip
  ;

Whitespace:
    [ \t\n\r]+ -> skip;

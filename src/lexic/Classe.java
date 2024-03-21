package lexic;

public enum Classe {
    identifier,
    reservedWord,
    String,
    integerNumber,
    sumOperator,
    subtractOperator,
    multiplyOperator,
    divisionOperator,
    greaterOperator,
    lessesOperator,

    greaterEqualOperator,
    lesserEqualOperator,

    parentesesDireita,
    parentesesEsquerda,

    equalOperator, // =
    distinctOperator, // <>
    andOperator, // and
    orOperator, // or
    notOperador, // not
    allocation, // :=
    semicolon, // :
    comma, // ,
    dot,
    colon,
    EOF
}

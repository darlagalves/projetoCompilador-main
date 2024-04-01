package sintatico;

import lexic.Lexic;
import lexic.Token;
import lexic.Classe;


public class Sintatico {
    
    private String nomeArquivo;
    private Lexic lexico;
    private Token token;

    public boolean isReservedWord(String wordAux){
        return token.getClasse() == Classe.reservedWord &&  token.getValue().getstringValue().equals(wordAux) ;
    }

    public Sintatico (String nomeArquivo){
        this.nomeArquivo = nomeArquivo;
        lexico = new Lexic(nomeArquivo);
    }

    public void analisar(){
        System.out.println("Analisando: " + nomeArquivo);
        token = lexico.nextToken();
        programa();
    }

    //<programa> ::= program <id> {A01} ; <corpo> • {A45}
    public void programa(){
        if(isReservedWord("program")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.identifier){
                token = lexico.nextToken();
                //{A01}
                if(token.getClasse() == Classe.semicolon){
                    token = lexico.nextToken();
                    corpo();
                    if(token.getClasse() == Classe.dot){
                        token = lexico.nextToken();
                        //{A45}
                    }else{
                        System.err.println(token.getline() + "," + token.getcolumn() + " (.) Ponto final esperado no programa.");
                    }
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " (;) Ponto e vírgula esperados no programa.");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + " Nome do programa principal esperando");
            }
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + " Palavra reservada esperando");
        }
    }

    //<corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    private void corpo(){
        declara();
        //rotina
        //{A44}
        if(isReservedWord("begin") ){
            token = lexico.nextToken();
            sentencas();
            if(isReservedWord("end")){
                token = lexico.nextToken();
                //{A46}
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + " Palavra reservada (end) esperada no final");
            }
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + " Palavra reservada (begin) esperada no inicio");
        }
    }

    private void sentencas(){
        comando();
        mais_sentencas();
    }

    private void mais_sentencas(){
        if(token.getClasse() == Classe.semicolon){
            token = lexico.nextToken();
            cont_sentencas();
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + "  ; esperado na regra mais_sentencas");
        }
    }

    private void cont_sentencas(){
        if(isReservedWord("read") ||
        isReservedWord("write") ||
        isReservedWord("writeln") ||
        isReservedWord("for")||
        isReservedWord("repeat") ||
        isReservedWord("while")||
        isReservedWord("if") ||
        token.getClasse() == Classe.identifier){
            sentencas();
        }
    }

    private void comando(){
        if(isReservedWord("read")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.parentesesEsquerda){
                token = lexico.nextToken();
                var_read();
                if(token.getClasse() == Classe.parentesesDireita){
                    token = lexico.nextToken();
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando read");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando read");
            }
        }else if(isReservedWord("write")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.parentesesEsquerda){
                token = lexico.nextToken();
                exp_write();
                if(token.getClasse() == Classe.parentesesDireita){
                    token = lexico.nextToken();
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando write");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando write");
            }
        } else if(isReservedWord("writeln")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.parentesesEsquerda){
                token = lexico.nextToken();
                exp_write();
                if(token.getClasse() == Classe.parentesesDireita){
                    token = lexico.nextToken();
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando writeln");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando writeln");
            }
        } else if (isReservedWord("for")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.identifier){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.allocation ){
                    token = lexico.nextToken();
                    expressao();

                    if(isReservedWord("to")){
                        token = lexico.nextToken();
                        expressao();
                        if(isReservedWord("do")){
                            token = lexico.nextToken();
                            if(isReservedWord("begin")){
                                token = lexico.nextToken();
                                sentencas();
                                if(isReservedWord("end")){
                                    token = lexico.nextToken();
                                    
                                }else{
                                    System.err.println(token.getline() + "," + token.getcolumn() + "  (end) esperado na regra comando for");
                                }
                            }else{
                                System.err.println(token.getline() + "," + token.getcolumn() + "  (begin) esperado na regra comando for");
                            }
                            
                        }else{
                            System.err.println(token.getline() + "," + token.getcolumn() + "  (do) esperado na regra comando for");
                        }
                    }else{
                        System.err.println(token.getline() + "," + token.getcolumn() + "  (to) esperado na regra comando for");
                    }
                    
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  := esperado na regra comando for");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  identificador esperado na regra comando for");
            }
        }else if(isReservedWord("repeat")){
            token = lexico.nextToken();
            sentencas();
            if(isReservedWord("until")){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.parentesesEsquerda){
                    token = lexico.nextToken();
                    expressao_logica();
                    if(token.getClasse() == Classe.parentesesDireita){
                        token = lexico.nextToken();
                        
                    }else{
                        System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando repeat");
                    }
                    
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando repeat");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  (until) esperado na regra comando repeat");
            }
        }else if(isReservedWord("while")){
            token = lexico.nextToken();
                if(token.getClasse() == Classe.parentesesEsquerda){
                    token = lexico.nextToken();
                    expressao_logica();
                    if(token.getClasse() == Classe.parentesesDireita){
                        token = lexico.nextToken();
                        if(isReservedWord("do")){
                            token = lexico.nextToken();
                            if(isReservedWord("begin")){
                                token = lexico.nextToken();
                                sentencas();
                                if(isReservedWord("end")){
                                    token = lexico.nextToken();
                                    
                                }else{
                                    System.err.println(token.getline() + "," + token.getcolumn() + "  (end) esperado na regra comando while");
                                }
                            }else{
                                System.err.println(token.getline() + "," + token.getcolumn() + "  (begin) esperado na regra comando while");
                            }
                            
                        }else{
                            System.err.println(token.getline() + "," + token.getcolumn() + "  (do) esperado na regra comando while");
                        }
                    }else{
                        System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando while");
                    }
                    
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando while");
                }
        }else if(isReservedWord("if")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.parentesesEsquerda){
                token = lexico.nextToken();
                expressao_logica();
                if(token.getClasse() == Classe.parentesesDireita){
                    token = lexico.nextToken();
                    if(isReservedWord("then")){
                        token = lexico.nextToken();
                        if(isReservedWord("begin")){
                            token = lexico.nextToken();
                            sentencas();
                            if(isReservedWord("end")){
                                token = lexico.nextToken();
                                pfalsa();
                                
                            }else{
                                System.err.println(token.getline() + "," + token.getcolumn() + "  (end) esperado na regra comando if");
                            }
                        }else{
                            System.err.println(token.getline() + "," + token.getcolumn() + "  (begin) esperado na regra comando if");
                        }
                        
                    }else{
                        System.err.println(token.getline() + "," + token.getcolumn() + "  (do) esperado na regra comando if");
                    }
                    
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando if");
                }
                
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando if");
            }
        }else if (token.getClasse() == Classe.identifier){
            token = lexico.nextToken();
            if (token.getClasse() == Classe.allocation){
                token = lexico.nextToken();
                expressao();
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  := esperado na regra comando apos o identificador(id)");
            }
        }
    }

    private void pfalsa(){
        if (isReservedWord("else")){
            token = lexico.nextToken();
            if(isReservedWord("begin")){
                token = lexico.nextToken();
                sentencas();
                if(isReservedWord("end")){
                    token = lexico.nextToken(); 
                }
            }
        }
    }

    private void var_read(){
        if (token.getClasse() == Classe.identifier){
            token = lexico.nextToken();
            mais_var_read();
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + "  id esperado na regra var_read");
        }
    }

    private void mais_var_read(){
        if (token.getClasse() == Classe.comma){
            token = lexico.nextToken();
            var_read();
        }
    }

    private void exp_write(){
        if (token.getClasse() == Classe.identifier){
            token = lexico.nextToken();
            mais_exp_write();
        }else if (token.getClasse() == Classe.String){
            token = lexico.nextToken();
            mais_exp_write();
        } else if (token.getClasse() == Classe.integerNumber){
            token = lexico.nextToken();
            mais_exp_write();
        } else{
            System.err.println(token.getline() + "," + token.getcolumn() + "  id, string ou intnum esperado na regra exp_write");
        }
    }

    private void mais_exp_write(){
        if (token.getClasse() == Classe.comma){
            token = lexico.nextToken();
            exp_write();
        }
    }

    //<declara> ::= var <dvar> <mais_dc> | ε
    private void declara(){
        if(isReservedWord("var")){
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }

    private void mais_dc(){
        if(token.getClasse() == Classe.semicolon){
            token = lexico.nextToken();
            cont_dc();
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + "  ; esperado na regra mais_dc");
        }
    }

    private void cont_dc(){
        if(token.getClasse() == Classe.identifier){
            dvar();
            mais_dc();
        }
    }

    //<dvar> ::= <variaveis> : <tipo_var> {A02}
    private void dvar(){
        variaveis();
        if(token.getClasse() == Classe.colon){
            token = lexico.nextToken();
            tipo_var();
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + " : esperado na regra dvar");
        }
    }

    private void tipo_var(){
        if(isReservedWord("integer")){
            token = lexico.nextToken();
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + " integer esperado na regra tipo_var");
        }
    }

    private void variaveis(){
        if(token.getClasse() == Classe.identifier){
            token = lexico.nextToken();
            //{A03}
            mais_var();
        }
    }

    private void mais_var(){
        if(token.getClasse() == Classe.comma){
            token = lexico.nextToken();
            variaveis();
        } 
    }

    private void expressao_logica(){
        termo_logico();
        mais_expr_logica();
    }

    private void mais_expr_logica(){
        if(isReservedWord("or")){
            token = lexico.nextToken();
            termo_logico();
            mais_expr_logica();
        }
    }

    private void termo_logico(){
        //token = lexico.nextToken();
        fator_logico();
        mais_termo_logico();
    }

    private void mais_termo_logico(){
        if(isReservedWord("and")){
            token = lexico.nextToken();
            fator_logico();
            mais_termo_logico();
        }
    }

    private void fator_logico(){
        if(token.getClasse() == Classe.parentesesEsquerda){
            token = lexico.nextToken();
            expressao_logica();
            if(token.getClasse() == Classe.parentesesDireita){
                token = lexico.nextToken();
            } else{
                System.err.println(token.getline() + "," + token.getcolumn() + " ) esperado na regra fator_logico");
            }
        }else if (isReservedWord("not")){
            token = lexico.nextToken();
            fator_logico();
        }else if (isReservedWord("true")){
            token = lexico.nextToken();
        }else if (isReservedWord("false")){
            token = lexico.nextToken();
        }else{
            relacional();
        }
    }

    private void relacional(){
        //System.out.println(token);
        if(token.getClasse() == Classe.identifier ||
        token.getClasse() == Classe.integerNumber ||
        token.getClasse() == Classe.parentesesEsquerda){
            //token = lexico.nextToken();
            //System.out.println(token);
            expressao();
            System.out.println(token);
            if (token.getClasse() == Classe.equalOperator ||
            token.getClasse() == Classe.greaterOperator ||
            token.getClasse() == Classe.greaterEqualOperator ||
            token.getClasse() == Classe.lessesOperator ||
            token.getClasse() == Classe.lesserEqualOperator ||
            token.getClasse() == Classe.distinctOperator) {
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + " = , > , >= , < , <= , <> esperado na regra relacional");
            }
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na segunda parte da regra relacional");
        }
    }

    private void expressao(){
        termo();
        mais_expressao();
    }

    private void mais_expressao(){
        if(token.getClasse() == Classe.sumOperator ||
        token.getClasse() == Classe.subtractOperator){
            token = lexico.nextToken();
            termo();
            mais_expressao();

        }
    }

    private void termo(){
        fator();
        mais_termo();
    }

    private void mais_termo(){
        if(token.getClasse() == Classe.multiplyOperator ||
        token.getClasse() == Classe.divisionOperator){
            token = lexico.nextToken();
            fator();
            mais_termo();

        }
    }

    private void fator(){
        System.out.println(token);
        if(token.getClasse() == Classe.identifier){
            token = lexico.nextToken();
        } else if (token.getClasse() == Classe.integerNumber) {
            token = lexico.nextToken();
        } else if(token.getClasse() == Classe.parentesesEsquerda){
            token = lexico.nextToken();
            expressao();
            if(token.getClasse() == Classe.parentesesDireita){
                token = lexico.nextToken();
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + " ) esperado na regra fator");
            }
        }else{
            System.err.println(token.getline() + "," + token.getcolumn() + " id, intnum ou ( esperado na regra fator");
        }
    }

}

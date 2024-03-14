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
            //sentencas();
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
}

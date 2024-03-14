// Código fonte do analisador léxico do compilador da linguagem de programação "Pascal", criado por Vinícius Silveira Bisinoto.
import lexic.Classe;
import lexic.Lexic;
import lexic.Token;
import sintatico.Sintatico;


public class Compiler {
    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Modo de usar: java -jar NomePrograma NomeArquivoCodigo");
            return;
        }
        Sintatico sintatico = new Sintatico(args[0]);
        sintatico.analisar();
    }
}

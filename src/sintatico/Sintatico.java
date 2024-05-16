package sintatico;

import lexic.Lexic;
import lexic.Token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lexic.Classe;


public class Sintatico {
    
    private String nomeArquivo;
    private Lexic lexico;
    private Token token;

    private TabelaSimbolos tabela = new TabelaSimbolos();
    private String rotulo = "";
    private int contRotulo = 1;
    private int offsetVariavel = 0;
    private String nomeArquivoSaida;
    private String caminhoArquivoSaida;
    private BufferedWriter bw;
    private FileWriter fw;
    private static final int TAMANHO_INTEIRO = 4;
    private List<String> variaveis = new ArrayList<>();
    private List<String> sectionData = new ArrayList<>();
    private Registro registro;
    private String rotuloElse;

    public boolean isReservedWord(String wordAux){
        return token.getClasse() == Classe.reservedWord &&  token.getValue().getstringValue().equals(wordAux) ;
    }

    public Sintatico (String nomeArquivo){
        this.nomeArquivo = nomeArquivo;
        lexico = new Lexic(nomeArquivo);

        nomeArquivoSaida = "queronemver.asm";
        caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
        bw = null;
        fw = null;
        try {
            fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
            bw = new BufferedWriter(fw);
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de saída");
        }
    }

    private void escreverCodigo(String instrucoes) {
		try {
			if (rotulo.isEmpty()) {
				bw.write(instrucoes + "\n");
			} else {
				bw.write(rotulo + ": " +  instrucoes + "\n");
				rotulo = "";
			}
		} catch (IOException e) {
			System.err.println("Erro escrevendo no arquivo de saída");
		}
	}

    //funcao para criar um rotulo
    private String criarRotulo(String texto) {
		String retorno = "rotulo" + texto + contRotulo;
		contRotulo++;
		return retorno;
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
                //{A01}
                Registro registro  = tabela.add(token.getValue().getstringValue());
                offsetVariavel = 0;
                registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);
                escreverCodigo("global main");
                escreverCodigo("extern printf");
                escreverCodigo("extern scanf\n");
                escreverCodigo("section .text ");
                rotulo = "main";
                escreverCodigo("\t; Entrada do programa");
                escreverCodigo("\tpush ebp");
                escreverCodigo("\tmov ebp, esp");
                System.out.println(tabela);

                token = lexico.nextToken();

                if(token.getClasse() == Classe.semicolon){
                    token = lexico.nextToken();
                    corpo();
                    if(token.getClasse() == Classe.dot){
                        token = lexico.nextToken();
                        //{A45}
                        escreverCodigo("\tleave");
                        escreverCodigo("\tret");
                        if (!sectionData.isEmpty()) {
                            escreverCodigo("\nsection .data\n");
                            for (String mensagem : sectionData) {
                                escreverCodigo(mensagem);
                            }
                        }
                        try {
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Erro ao fechar arquivo de saída");
                        }

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
                    //A61
                    //A61
                    // Gerar um avanço de linha, ou seja, um line feed
                    //rotuloString2: db

                    String novaLinha = "rotuloStringLN: db '', 10, 0";
                    if(!sectionData.contains(novaLinha)){
                        sectionData.add(novaLinha);
                    }
                    escreverCodigo("\tpush rotuloStringLN");
                    escreverCodigo("\tcall printf");
                    escreverCodigo("\tadd esp, 4");
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + "  ) esperado na regra comando writeln");
                }
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  ( esperado na regra comando writeln");
            }
        } else if (isReservedWord("for")){
            token = lexico.nextToken();
            if(token.getClasse() == Classe.identifier){
                //A57
                String variavel = token.getValue().getstringValue();
                if (!tabela.isPresent(variavel)) {
                    System.err.println("Variável " + variavel + " não foi declarada (a57)");
                    System.exit(-1);
                } else {
                    registro = tabela.get(variavel);
                    if (registro.getCategoria() != Categoria.VARIAVEL) {
                        System.err.println("Identificador " + variavel + " não é uma variável");
                        System.exit(-1);
                    }
                }
                token = lexico.nextToken();
                
                if(token.getClasse() == Classe.allocation ){
                    token = lexico.nextToken();
                    expressao();
                    //A11
                    escreverCodigo("\tpop dword [ebp - " +registro.getOffset() + "]");

                    String rotuloEntrada = criarRotulo("FOR");
                    String rotuloSaida = criarRotulo("FIMFOR");

                    rotulo =  rotuloEntrada;

                    if(isReservedWord("to")){
                        token = lexico.nextToken();
                        expressao();
                        //A12
                        escreverCodigo("\tpush ecx\n"
								  + "\tmov ecx, dword[ebp - " + registro.getOffset() + "]\n"
								  + "\tcmp ecx, dword[esp+4]\n"  //+4 por causa do ecx
								  + "\tjg " + rotuloSaida + "\n"
								  + "\tpop ecx");
                        if(isReservedWord("do")){
                            token = lexico.nextToken();
                            if(isReservedWord("begin")){
                                token = lexico.nextToken();
                                sentencas();
                                if(isReservedWord("end")){
                                    token = lexico.nextToken();
                                    // {A13}
                                    // Gerar as instruções para incrementar a variável id.
                                    escreverCodigo("\tadd dword[ebp - " + registro.getOffset() + "], 1");
                                    // Gerar um desvio para rotuloFor.
                                    escreverCodigo("\tjmp " + rotuloEntrada);
                                    // Gerar o rótulo rotuloFim.
                                    rotulo = rotuloSaida;
                                    
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
            //A14
            String rotRepeat = criarRotulo("Repeat");
            rotulo = rotRepeat;
            token = lexico.nextToken();
            sentencas();
            if(isReservedWord("until")){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.parentesesEsquerda){
                    token = lexico.nextToken();
                    expressao_logica();
                    if(token.getClasse() == Classe.parentesesDireita){
                        token = lexico.nextToken();
                        //A15
                        escreverCodigo("\tcmp dword[esp], 0");
                        escreverCodigo("\tje  " + rotRepeat);
                        
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
            //{A16}
            String rotuloWhile = criarRotulo("While");
            String rotuloFim = criarRotulo("FimWhile");
            rotulo = rotuloWhile;
                if(token.getClasse() == Classe.parentesesEsquerda){
                    token = lexico.nextToken();
                    expressao_logica();
                    if(token.getClasse() == Classe.parentesesDireita){
                        token = lexico.nextToken();
                        //{A17}
                        escreverCodigo("\tcmp dword[esp], 0\n");
                        escreverCodigo("\tje "+ rotuloFim);
                        if(isReservedWord("do")){
                            token = lexico.nextToken();
                            if(isReservedWord("begin")){
                                token = lexico.nextToken();
                                sentencas();
                                if(isReservedWord("end")){
                                    token = lexico.nextToken();
                                    //{A18}
                                    escreverCodigo("\tjmp "+ rotuloWhile);
                                    rotulo = rotuloFim;
                                    
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
                    //{A19}
                    rotuloElse = criarRotulo("Else");
                    String rotuloFim = criarRotulo("FimIf");
                    escreverCodigo("\tcmp dword[esp], 0\n");
                    escreverCodigo("\tje "+ rotuloElse);
                    if(isReservedWord("then")){
                        token = lexico.nextToken();
                        if(isReservedWord("begin")){
                            token = lexico.nextToken();
                            sentencas();
                            if(isReservedWord("end")){
                                token = lexico.nextToken();
                                //{A20}
                                escreverCodigo("\tjmp "+ rotuloFim);
                                pfalsa();
                                //{A21}
                                rotulo = rotuloFim;
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
            String variavel = token.getValue().getstringValue();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada a49");
                System.exit(-1);
            } else {
                Registro registro = tabela.get(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                }
            }
            token = lexico.nextToken();
            if (token.getClasse() == Classe.allocation){
                token = lexico.nextToken();
                expressao();
                escreverCodigo("\tpop eax");
                escreverCodigo("\tmov dword[ebp -" + registro.getOffset() + "] , eax");
            }else{
                System.err.println(token.getline() + "," + token.getcolumn() + "  := esperado na regra comando apos o identificador(id)");
            }
        }
    }

    private void pfalsa(){
        //{A25}
        //rotulo = rotuloElse;
        escreverCodigo(rotuloElse + ":");
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
            //{A08}
            String variavel = token.getValue().getstringValue();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada a08");
                System.exit(-1);
            } else {
                Registro registro = tabela.get(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tmov edx, ebp");
                    escreverCodigo("\tlea eax, [edx - " + registro.getOffset() + "]");
                    escreverCodigo("\tpush eax");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall scanf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }

                }
            }
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
            String variavel = token.getValue().getstringValue();
            //A09
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada a09");
                System.exit(-1);
            } else {
                Registro registro = tabela.get(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall printf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }

            token = lexico.nextToken();
            mais_exp_write();
        }else if (token.getClasse() == Classe.String){

            //A59
            String string = token.getValue().getstringValue();
            String rotulo = criarRotulo("String");
            sectionData.add(rotulo + ": db '" + string + "', 0 ");
            escreverCodigo("\tpush " + rotulo);
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 4");

            token = lexico.nextToken();
            mais_exp_write();
        } else if (token.getClasse() == Classe.integerNumber){

            //A43
            int numero = token.getValue().getintegerValue();
            escreverCodigo("\tpush " + numero);
            escreverCodigo("\tpush @Integer");
            escreverCodigo("\tcall printf");
            escreverCodigo("\tadd esp, 8");
            if (!sectionData.contains("@Integer: db '%d',0")) {
                sectionData.add("@Integer: db '%d',0");
            }

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

            int tamanho = 0;
            for (String var : variaveis){
                tabela.get(var).setTipo(Tipo.INTEGER);
                tamanho += TAMANHO_INTEIRO;
            }
            escreverCodigo("\tsub esp, " + tamanho );
            variaveis.clear();


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
            //{A03}
            String variavel = token.getValue().getstringValue();
            if(tabela.isPresent(variavel)){
                System.out.println("Variável " + variavel + " já foi declarada anteriormente");
                System.exit(-1);
            } else{
                tabela.add(variavel);
                tabela.get(variavel).setCategoria(Categoria.VARIAVEL);
                tabela.get(variavel).setOffset(offsetVariavel);
                offsetVariavel += TAMANHO_INTEIRO;
                variaveis.add(variavel);
            }
            System.out.println(tabela);
            token = lexico.nextToken();
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
            // {A26}
            // Empilhar 1, caso o valor de expressao_logica ou termo_logico seja 1, e 0
            // (falso), caso seja diferente. Isto pode ser feito da seguinte forma:
            // Crie um novo rótulo, digamos rotSaida
            String rotSaida = criarRotulo("SaidaMEL");
            // Crie um novo rótulo, digamos rotVerdade
            String rotVerdade = criarRotulo("VerdadeMEL");
            // Gere a instrução: cmp dword [ESP + 4], 1
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            // Gere a instrução je para rotVerdade
            escreverCodigo("\tje " + rotVerdade);
            // Gere a instrução: cmp dword [ESP], 1
            escreverCodigo("\tcmp dword [ESP], 1");
            // Gere a instrução je para rotVerdade
            escreverCodigo("\tje " + rotVerdade);
            // Gere a instrução: mov dword [ESP + 4], 0
            escreverCodigo("\tmov dword [ESP + 4], 0");
            // Gere a instrução jmp para rotSaida
            escreverCodigo("\tjmp " + rotSaida);
            // Gere o rótulo rotVerdade
            rotulo = rotVerdade;
            // Gere a instrução: mov dword [ESP + 4], 1
            escreverCodigo("\tmov dword [ESP + 4], 1");
            // Gere o rótulo rotSaida
            rotulo = rotSaida;
            // Gere a instrução: add esp, 4
            escreverCodigo("\tadd esp, 4");
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
            // {A27}
            // Empilhar 1 (verdadeiro), caso o valor de termo_logico e fator_logico seja 1,
            // e 0 (falso), caso seja diferente. Proceda de forma semelhante a ação 26.
            // Crie um novo rótulo, digamos rotSaida
            String rotSaida = criarRotulo("SaidaMTL");
            // Crie um novo rótulo, digamos rotFalso
            String rotFalso = criarRotulo("FalsoMTL");
            // Gere a instrução: cmp dword [ESP + 4], 1
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tjne " + rotFalso);
            // Comparar os 2 valores
            // Gere a instrução: pop eax
            escreverCodigo("\tpop eax");
            // Gere a instrução: cmp dword [ESP], eax
            escreverCodigo("\tcmp dword [ESP], eax");
            // Gere a instrução je para rotVerdade
            escreverCodigo("\tjne " + rotFalso);
            // Gere a instrução: mov dword [ESP + 4], 1
            escreverCodigo("\tmov dword [ESP], 1");
            // Gere a instrução jmp para rotSaida
            escreverCodigo("\tjmp " + rotSaida);
            // Gere o rótulo rotFalso
            rotulo = rotFalso;
            // Gere a instrução: mov dword [ESP], 0
            escreverCodigo("\tmov dword [ESP], 0");
            // Gere o rótulo rotSaida
            rotulo = rotSaida;
            // Gere a instrução: add esp, 4
            // escreverCodigo("\tadd esp, 4");

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
            //{A28}
            // Empilhar 1 (verdadeiro), caso o valor de fator_logico seja 0, e 0 (falso),
            // caso seja diferente. Proceda da seguinte forma:
            // Crie um rótulo Falso e outro Saida.
            String rotFalso = criarRotulo("FalsoFL");
            String rotSaida = criarRotulo("SaidaFL");
            // Gere a instrução: cmp dword [ESP], 1
            escreverCodigo("\tcmp dword [ESP], 1");
            // Gere a instrução: jne Falso
            escreverCodigo("\tjne " + rotFalso);
            // Gere a instrução: mov dword [ESP], 0
            escreverCodigo("\tmov dword [ESP], 0");
            // Gere a instrução: jmp Fim
            escreverCodigo("\tjmp " + rotSaida);
            // Gere o rótulo Falso
            rotulo = rotFalso;
            // Gere a instrução: mov dword [ESP], 1
            escreverCodigo("\tmov dword [ESP], 1");
            // Gere o rótulo Fim
            rotulo = rotSaida;
        }else if (isReservedWord("true")){
            token = lexico.nextToken();
            //{A29}
            //Empilhar 1
            escreverCodigo("\tpush 1");
        }else if (isReservedWord("false")){
            token = lexico.nextToken();
            //{A30}
            //Empilhar 0
            escreverCodigo("\tpush 0");
        }else{
            relacional();
        }
    }

    private void relacional(){
        
        if(token.getClasse() == Classe.identifier ||
        token.getClasse() == Classe.integerNumber ||
        token.getClasse() == Classe.parentesesEsquerda){
            
            expressao();
            
            if (token.getClasse() == Classe.equalOperator) {
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                        //{A31}
                        // Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja igual a
                        // segunda, ou 0 (falso), caso contrário. Isto pode ser feito da seguinte forma:
                        // Crie um rótulo Falso e outro Saida.
                        String rotFalso = criarRotulo("FalsoREL");
                        String rotSaida = criarRotulo("SaidaREL");
                        // COMPARA 2 VALORES
                        // Gere a instrução: pop eax
                        escreverCodigo("\tpop eax");
                        // Gere a instrução: cmp dword [ESP], eax
                        escreverCodigo("\tcmp dword [ESP], eax");
                        // Gere a instrução: jne Falso
                        escreverCodigo("\tjne " + rotFalso);
                        // Gere a instrução: mov dword [ESP], 1
                        escreverCodigo("\tmov dword [ESP], 1");
                        // Gere a instrução: jmp Fim
                        escreverCodigo("\tjmp " + rotSaida);
                        // Gere o rótulo Falso
                        rotulo = rotFalso;
                        // Gere a instrução: mov dword [ESP], 0
                        escreverCodigo("\tmov dword [ESP], 0");
                        // Gere o rótulo Fim
                        rotulo = rotSaida;
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }
            } else if (token.getClasse() == Classe.greaterOperator ){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                        // {A32}
                        // Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja maior que a
                        // segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31.
                        // Crie um rótulo Falso e outro Saida.
                        String rotFalso = criarRotulo("FalsoREL");
                        String rotSaida = criarRotulo("SaidaREL");
                        // Gere a instrução: pop eax
                        escreverCodigo("\tpop eax");
                        // Gere a instrução: cmp dword [ESP], eax
                        escreverCodigo("\tcmp dword [ESP], eax");
                        // Gere a instrução: jle Falso
                        escreverCodigo("\tjle " + rotFalso);
                        // Gere a instrução: mov dword [ESP], 1
                        escreverCodigo("\tmov dword [ESP], 1");
                        // Gere a instrução: jmp Fim
                        escreverCodigo("\tjmp " + rotSaida);
                        // Gere o rótulo Falso
                        rotulo = rotFalso;
                        // Gere a instrução: mov dword [ESP], 0
                        escreverCodigo("\tmov dword [ESP], 0");
                        // Gere o rótulo Fim
                        rotulo = rotSaida;
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }

            } else if (token.getClasse() == Classe.greaterEqualOperator ){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                        // {A33}
                        // Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja maior ou
                        // igual a segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação
                        // 31.
                        // Crie um rótulo Falso e outro Saida.
                        String rotFalso = criarRotulo("FalsoREL");
                        String rotSaida = criarRotulo("SaidaREL");
                        // Gere a instrução: pop eax
                        escreverCodigo("\tpop eax");
                        // Gere a instrução: cmp dword [ESP], eax
                        escreverCodigo("\tcmp dword [ESP], eax");
                        // Gere a instrução: jl Falso
                        escreverCodigo("\tjl " + rotFalso);
                        // Gere a instrução: mov dword [ESP], 1
                        escreverCodigo("\tmov dword [ESP], 1");
                        // Gere a instrução: jmp Fim
                        escreverCodigo("\tjmp " + rotSaida);
                        // Gere o rótulo Falso
                        rotulo = rotFalso;
                        // Gere a instrução: mov dword [ESP], 0
                        escreverCodigo("\tmov dword [ESP], 0");
                        // Gere o rótulo Fim
                        rotulo = rotSaida;

                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }

            } else if (token.getClasse() == Classe.lessesOperator){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                        // {A34}
                        // Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja menor que a
                        // segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31.
                        // Crie um rótulo Falso e outro Saida.
                        String rotFalso = criarRotulo("FalsoREL");
                        String rotSaida = criarRotulo("SaidaREL");
                        // Gere a instrução: pop eax
                        escreverCodigo("\tpop eax");
                        // Gere a instrução: cmp dword [ESP], eax
                        escreverCodigo("\tcmp dword [ESP], eax");
                        // Gere a instrução: jge Falso
                        escreverCodigo("\tjge " + rotFalso);
                        // Gere a instrução: mov dword [ESP], 1
                        escreverCodigo("\tmov dword [ESP], 1");
                        // Gere a instrução: jmp Fim
                        escreverCodigo("\tjmp " + rotSaida);
                        // Gere o rótulo Falso
                        rotulo = rotFalso;
                        // Gere a instrução: mov dword [ESP], 0
                        escreverCodigo("\tmov dword [ESP], 0");
                        // Gere o rótulo Fim
                        rotulo = rotSaida;
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }

            } else if (token.getClasse() == Classe.lesserEqualOperator){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                         // {A35}
                        // Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja menor ou
                        // igual a segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação
                        // 31.
                        // Crie um rótulo Falso e outro Saida.
                        String rotFalso = criarRotulo("FalsoREL");
                        String rotSaida = criarRotulo("SaidaREL");
                        // Gere a instrução: pop eax
                        escreverCodigo("\tpop eax");
                        // Gere a instrução: cmp dword [ESP], eax
                        escreverCodigo("\tcmp dword [ESP], eax");
                        // Gere a instrução: jg Falso
                        escreverCodigo("\tjg " + rotFalso);
                        // Gere a instrução: mov dword [ESP], 1
                        escreverCodigo("\tmov dword [ESP], 1");
                        // Gere a instrução: jmp Fim
                        escreverCodigo("\tjmp " + rotSaida);
                        // Gere o rótulo Falso
                        rotulo = rotFalso;
                        // Gere a instrução: mov dword [ESP], 0
                        escreverCodigo("\tmov dword [ESP], 0");
                        // Gere o rótulo Fim
                        rotulo = rotSaida;
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }

            } else if (token.getClasse() == Classe.distinctOperator){
                token = lexico.nextToken();
                if(token.getClasse() == Classe.identifier ||
                    token.getClasse() == Classe.integerNumber ||
                    token.getClasse() == Classe.parentesesEsquerda){
                        expressao();
                         // {A36}
                        // Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja diferente
                        // da segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31.
                        // Crie um rótulo Falso e outro Saida.
                        String rotFalso = criarRotulo("FalsoREL");
                        String rotSaida = criarRotulo("SaidaREL");
                        // Gere a instrução: pop eax
                        escreverCodigo("\tpop eax");
                        // Gere a instrução: cmp dword [ESP], eax
                        escreverCodigo("\tcmp dword [ESP], eax");
                        // Gere a instrução: je Falso
                        escreverCodigo("\tje " + rotFalso);
                        // Gere a instrução: mov dword [ESP], 1
                        escreverCodigo("\tmov dword [ESP], 1");
                        // Gere a instrução: jmp Fim
                        escreverCodigo("\tjmp " + rotSaida);
                        // Gere o rótulo Falso
                        rotulo = rotFalso;
                        // Gere a instrução: mov dword [ESP], 0
                        escreverCodigo("\tmov dword [ESP], 0");
                        // Gere o rótulo Fim
                        rotulo = rotSaida;
                }else{
                    System.err.println(token.getline() + "," + token.getcolumn() + " expressao esperada na primeira parte da regra relacional");
                }

            } else{
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
        if(token.getClasse() == Classe.sumOperator){
            token = lexico.nextToken();
            termo();
            mais_expressao();
            //{A37}
            escreverCodigo("\tpop eax");
            escreverCodigo("\tadd dword[ESP], eax");
        } else if (token.getClasse() == Classe.subtractOperator){
            token = lexico.nextToken();
            termo();
            mais_expressao();
            //{A38}
            escreverCodigo("\tpop eax");
            escreverCodigo("\tsub dword[ESP], eax");
        }
    }

    private void termo(){
        fator();
        mais_termo();
    }

    private void mais_termo(){
        if(token.getClasse() == Classe.multiplyOperator ){
            token = lexico.nextToken();
            fator();
            mais_termo();
            //{A39}
            escreverCodigo("\tpop eax");
            escreverCodigo("\timul eax, dword [ESP]");
            escreverCodigo("\tmov dword[ESP], eax");

        } else if (token.getClasse() == Classe.divisionOperator){
            token = lexico.nextToken();
            fator();
            mais_termo();
            //{A40}
            escreverCodigo("\tpop ecx");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tidiv ecx");
            escreverCodigo("\tpush eax");
        }
    }

    private void fator(){
        
        if(token.getClasse() == Classe.identifier){
            // {A55}
            // Se a categoria do identificador id, reconhecido em fator, for variável ou
            // parâmetro, então empilhar o valor armazenado no endereço de memória de id.
            // Lembre-se, que o endereço de memória de id é calculado em função da base da
            // pilha (EBP) e do deslocamento contido em display.
            String variavel = token.getValue().getstringValue();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                registro = tabela.get(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A55");
                    System.exit(-1);
                }
            }
            escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");
            token = lexico.nextToken();
            
        } else if (token.getClasse() == Classe.integerNumber) {
            //{A41}
            //empliha o numero correpondente a um
            escreverCodigo("\tpush " +token.getValue().getintegerValue());
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

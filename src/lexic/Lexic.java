package lexic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Lexic {

    private String filename;
    private Token token;
    private BufferedReader br;
    private char character;
    private StringBuilder lexeme = new StringBuilder();
    private int line;
    private int column;

    public Lexic(String filename) {
        this.filename = filename;
        String filepath = Paths.get(filename).toAbsolutePath().toString();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath, StandardCharsets.UTF_8));
            this.br = br;
            line = 1;
            column = 1;
            character = nextChar();
        } catch (IOException e) {
            System.err.println("Não foi possível abrir o arquivo: " + filename);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private char nextChar() {
        try {
            return (char) br.read();
        } catch (IOException e) {
            System.err.println("Não foi possível ler do arquivo: " + filename);
            e.printStackTrace();
            System.exit(-1);
            return 0;
        }

    }

    private boolean isReservedWord(String lexeme) {
        List<String> reservedWords = new ArrayList<String>(Arrays.asList(
                "and", "array", "begin", "case", "const",
                "div", "do", "downto", "else", "end", "write",
                "file", "for", "function", "goto", "if",
                "in", "label", "mod", "nil", "not", "writeln",
                "of", "or", "packed", "procedure", "program",
                "record", "repeat", "set", "then", "to", "read",
                "type", "until", "var", "while", "with",
                "integer", "real", "boolean", "char", "string", "true", "false"));
        return reservedWords.contains(lexeme);
    }

    public Token nextToken() {
        do {
            lexeme.setLength(0);
            if (character == 65535) {
                return new Token(line, column, Classe.EOF);
            } else if (character == '{') {
                character = nextChar();
                column++;
                while (character != '}') {
                    if (character == 65535) {
                        System.err.println("Erro na linha " + line + " e coluna " + column + ": comentário não fechado");
                        System.exit(-1);
                    } else if (character == '\n') {
                        character = nextChar();
                        line++;
                        column = 1;
                    } else {
                        character = nextChar();
                        column++;
                    }
                }
                character = nextChar();
                column++;
            } else if(character == '\''){
                token = new Token(line, column);
                character = nextChar();
                column++;
                while (character != '\'') {
                    if (character == 65535) {
                        System.err.println("Erro na linha " + line + " e coluna " + column + ": string não fechada");
                        System.exit(-1);
                    } else if (character == '\n') {
                        System.err.println("Erro na linha " + line + " e coluna " + column + ": string em múltiplas linhas");
                        System.exit(-1);
                    } else {
                        lexeme.append(character);
                        character = nextChar();
                        column++;
                    }
                }
                character = nextChar();
                column++;
                token.setClasse(Classe.String);
                token.setValue(new Value(lexeme.toString()));
                return token;
            } else if (Character.isLetter(character)) {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                while (Character.isLetterOrDigit(character)) {
                    lexeme.append(character);
                    character = nextChar();
                    column++;
                }
                if (isReservedWord(lexeme.toString())) {
                    token.setClasse(Classe.reservedWord);
                    token.setValue(new Value(lexeme.toString()));
                } else {
                    token.setClasse(Classe.identifier);
                    token.setValue(new Value(lexeme.toString()));
                }
                return token;
            } else if (character == '\n') {
                character = nextChar();
                line++;
                column = 1;
            }else if (character == ' ') {
                character = nextChar();
                column++;
            } else if (Character.isDigit(character)) {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                while (Character.isDigit(character)) {
                    lexeme.append(character);
                    character = nextChar();
                    column++;
                }
                token.setClasse(Classe.integerNumber);
                token.setValue(new Value(Integer.parseInt(lexeme.toString())));
                return token;
            } else if (Character.isWhitespace(character)) {
                character = nextChar();
                column++;
            } else if (character == '\t') {
                character = nextChar();
                column = column + 4;
            } else if (character == '.') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.dot);
                return token;
            } else if (character == ';') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.semicolon);
                return token;
            } else if (character == ',') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.comma);
                return token;
            } else if (character == '+') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.sumOperator);
                return token;
            } else if (character == '-') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.subtractOperator);
                return token;
            } else if (character == '*') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.multiplyOperator);
                return token;
            } else if (character == '/') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.divisionOperator);
                return token;
            } else if (character == '>') {
                token = new Token(line, column);
                //lexeme.append(character);
                character = nextChar();
                column++;
                if (character == '=') {
                    //lexeme.append(character);
                    character = nextChar();
                    column++;
                    token.setClasse(Classe.greaterEqualOperator);
                } else {
                    token.setClasse(Classe.greaterOperator);
                }
                return token;
            } else if (character == ':') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                if (character == '=') {
                    lexeme.append(character);
                    character = nextChar();
                    column++;
                    token.setClasse(Classe.allocation);
                } else {
                    token.setClasse(Classe.colon);
                }
                return token;
            } else if (character == '<') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                if (character == '=') {
                    lexeme.append(character);
                    character = nextChar();
                    column++;
                    token.setClasse(Classe.lesserEqualOperator);
                } else if (character == '>') {
                    lexeme.append(character);
                    character = nextChar();
                    column++;
                    token.setClasse(Classe.distinctOperator);
                } else {
                    token.setClasse(Classe.lessesOperator);
                }
                return token;
            } else if (character == '=') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.equalOperator);
                return token;
            }else if (character == '(') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.parentesesEsquerda);
                return token;
            }else if (character == ')') {
                token = new Token(line, column);
                lexeme.append(character);
                character = nextChar();
                column++;
                token.setClasse(Classe.parentesesDireita);
                return token;
            }             else {
                System.err.println("Erro na linha " + line + " e coluna " + column + ": caracter inválido");
                System.exit(-1);
            }

        } while (character != 65535);
        return new Token(line, column, Classe.EOF);
    }
}

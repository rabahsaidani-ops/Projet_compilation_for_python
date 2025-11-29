import java.util.ArrayList;
import java.util.List;

public class AnalyseurLexical {

    // 1. TYPES DE TOKENS 

    public enum TokenType {
        // --- Mots-clés Structurels ---
        FOR, IN, RANGE, 
        IF, ELSE, ELIF, 
        WHILE, 
        DEF, RETURN, 
        CLASS,
        BREAK, CONTINUE,
        PRINT,

        // --- Opérateurs Logiques 
        AND, OR, NOT,

        // Valeurs Booléennes 
        TRUE, FALSE, NONE,

        // Symboles
        COLON, PARENTOUV, PARENTFERM, VIRG, 
        PLUS, SUB, MULT, DIV, ASSIGN,
        EQEQ, LT, GT, NOTEQ, 

        // Gestion de l'indentation 
        RTAB,LTAB, 

        //  Tokens dynamiques (via Matrice) 
        IDENTIFIER, NUMBER,

        //special
        NOM_PRENOM,
        UNKNOWN, EOF
    }

    public static class Token {
        public TokenType type;
        public String valeur;

        public Token(TokenType type, String valeur) {
            this.type = type;
            this.valeur = valeur;
        }

        @Override
        public String toString() {
            return "<" + type + ", \"" + valeur + "\">";
        }
    }

    // ==========================================
    // ATTRIBUTS
    // ==========================================
    private final String input;
    private final int length;
    private int pos = 0;
    private final List<Token> tokens = new ArrayList<>();
    private int blud = 0; 

    // Matrice de transition
    private final int[][] M = {
    //LETTRES   //CHIFFRES   //AUTRE
             { 1, 2, -1 }, // Etat 0
             { 1, 1, -1 }, // Etat 1 (ID)
             { -1, 2, -1 } // Etat 2 (Number)
    };

    public AnalyseurLexical(String input) {
        this.input = input;
        this.length = input.length();
    }

    // FONCTIONS UTILITAIRES MANUELLES
    private int indice(char c) {
        if ((c >= 'a' && c <= 'z') || c == '_' || (c >= 'A' && c <= 'Z')) return 0; // Lettre
        if (c >= '0' && c <= '9') return 1; // Chiffre
        return 2; // Autre
     
    }

    private boolean estAlphaNum(char c) {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               (c >= '0' && c <= '9') || 
               c == '_';
    }

    // ANALYSE VIA MATRICE 
    private TokenType analyserMatrice(String mot) {
        int Ec = 0; 
        String s = mot + "#";
        int i = 0;

        while (s.charAt(i) != '#' && M[Ec][indice(s.charAt(i))] != -1) {
            Ec = M[Ec][indice(s.charAt(i))];  
            i++;                    
        }

        if (s.charAt(i) == '#' && Ec == 1) return TokenType.IDENTIFIER;
        if (s.charAt(i) == '#' && Ec == 2) return TokenType.NUMBER;
        
        return TokenType.UNKNOWN;
    }


    // GESTION INDENTATION (VOTRE FONCTION)
 
    public void verif_s_tab() {
    int d = 0; 

    
    if (pos < input.length() && input.charAt(pos) == '\n') {
        pos++; 

        while (pos < input.length() && (input.charAt(pos) == '\t' || input.charAt(pos) == ' ')) {
           
            if (input.charAt(pos) == ' ') {
                int k = 0; 
                int i = 0;
                
                for (i = 0; i < 4; i++) {
                    if (pos + i < input.length() && input.charAt(pos + i) == ' ') {
                        k++;
                    } else {
                        break;
                    }
                }

                if (k == 4) {
                    d++;      
                    pos += 4; 
                } else {
                    break; 
                }
            } 
            // Gestion des tabulations
            else if (input.charAt(pos) == '\t') {
                d++;
                pos++;
            }
        }

        
     
        if (pos < input.length() && (input.charAt(pos) == '#' || input.charAt(pos) == '\n')) {
            return; 
        }
      

        if (d < blud) {
           
            while (d < blud) {
                tokens.add(new Token(TokenType.RTAB, "bloc fini"));
                blud--;
            }
        } 
        else if (d > blud) {
            
            tokens.add(new Token(TokenType.LTAB, "bloc commences"));
            blud = d;
        }
    }
}

    public List<Token> tokeniser() {
      while (!fin()) {
            char c = examiner();

            // CAS 1 : GESTION DES COMMENTAIRES 
            if (c == '#') {
               
                while (!fin() && examiner() != '\n') {
                    avancer();
                }
                continue; 
            }

            // CAS 2 : GESTION DES SAUTS DE LIGNE ET INDENTATION
            if (c == '\n') {
                verif_s_tab();
                if (fin()) break;
                continue;
            }

            // CAS 3 : ESPACES (au milieu d'une ligne)
            if (c == ' ' || c == '\t' || c == '\r') {
                avancer();
                continue;
            }

            // CAS 4 : LE RESTE (Tokens normaux)
            int start = pos;
            c = avancer(); 

            switch (c) {
                // Symboles simples
                case ':' -> tokens.add(new Token(TokenType.COLON, ":"));
                case '(' -> tokens.add(new Token(TokenType.PARENTOUV, "("));
                case ')' -> tokens.add(new Token(TokenType.PARENTFERM, ")"));
                case ',' -> tokens.add(new Token(TokenType.VIRG, ","));
                
                // Opérateurs arithmétiques
                case '+' -> tokens.add(new Token(TokenType.PLUS, "+"));
                case '-' -> tokens.add(new Token(TokenType.SUB, "-"));
                case '*' -> tokens.add(new Token(TokenType.MULT, "*"));
                case '/' -> tokens.add(new Token(TokenType.DIV, "/"));
                
                // Opérateurs logiques/comparaison
                case '=' -> tokens.add(estConforme('=') ? new Token(TokenType.EQEQ, "==") : new Token(TokenType.ASSIGN, "="));
                case '<' -> tokens.add(new Token(TokenType.LT, "<"));
                case '>' -> tokens.add(new Token(TokenType.GT, ">"));
                case '!' -> {
                     if (estConforme('=')) tokens.add(new Token(TokenType.NOTEQ, "!="));
                     else tokens.add(new Token(TokenType.UNKNOWN, "!"));
                }

                default -> {
                    if (estAlphaNum(c)) {
                        reculer();
                        int tempStart = pos;
                        while (!fin() && estAlphaNum(examiner())) {
                            avancer();
                        }
                        String mot = input.substring(tempStart, pos);
                        
                        // 1. Vérifier si c'est un Mot-Clé
                        TokenType type = verifierMotCle(mot);
                        
                        // 2. Si ce n'est pas un mot-clé, utiliser la Matrice
                        if (type != TokenType.IDENTIFIER) {
                            tokens.add(new Token(type, mot));
                        } else {
                            TokenType typeMatrice = analyserMatrice(mot);
                            tokens.add(new Token(typeMatrice, mot));
                        }
                    } else {
                        // Ignorer ou erreur
                    }
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, "EOF"));
        return tokens;
    }
    // VERIFICATION DES MOTS CLES (COMPLETE)

    private TokenType verifierMotCle(String mot) {
        switch(mot) {
            case "for":     return TokenType.FOR;
            case "in":      return TokenType.IN;
            case "range":   return TokenType.RANGE;
            case "if":      return TokenType.IF;
            case "elif":    return TokenType.ELIF;
            case "else":    return TokenType.ELSE;
            case "while":   return TokenType.WHILE;
            case "def":     return TokenType.DEF;    
            case "return":  return TokenType.RETURN; 
            case "class":   return TokenType.CLASS;  
            case "break":   return TokenType.BREAK;  
            case "continue":return TokenType.CONTINUE;
            
            // Logique & Booléens
            case "and":     return TokenType.AND;   
            case "or":      return TokenType.OR;   
            case "not":     return TokenType.NOT;   
            case "True":    return TokenType.TRUE;  
            case "False":   return TokenType.FALSE; 
            case "None":    return TokenType.NONE;  
            
            case "print":   return TokenType.PRINT; 

            //NOM ET PRENOM 
            case "Saidani": return TokenType.NOM_PRENOM; 
            case "Rabah":     return TokenType.NOM_PRENOM;

            default: return TokenType.IDENTIFIER;
        }
    }

    // --- AUTRES UTILITAIRES ---
    private boolean fin() { return pos >= length; }
    private char avancer() { return input.charAt(pos++); }
    private void reculer() { if(pos > 0) pos--; }
    private char examiner() { return fin() ? '\0' : input.charAt(pos); }
    
    private boolean estConforme(char attendu) {
        if (examiner() == attendu) { pos++; return true; }
        return false;
    }

    public static void main(String[] args) {
   String code = "x = 0\n" +
                      "count_1 = 10\n" +
                      "\n" +
                      "# Debut de la boucle (Theme Projet)\n" +
                      "for i in range(count_1):\n" +
                      "    x = x + 1\n" +
                      "    \n" +
                      "    if x > 5:\n" +
                      "        # Vos mots-cles speciaux\n" +
                      "        Saidani Rabah\n" +
                      "        print x";
        
        System.out.println("Code Source :\n" + code + "\n----------------");
        AnalyseurLexical lexer = new AnalyseurLexical(code);
        List<Token> resultats = lexer.tokeniser();
        for (Token t : resultats) System.out.println(t);
    }
}
    


    
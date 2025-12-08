import java.util.List;
import javax.swing.JTextArea; 

public class AnalyseurSyntaxique {

    private List<AnalyseurLexical.Token> tokens;
    private int i;
    private boolean R;
    private JTextArea console; 

    public AnalyseurSyntaxique(List<AnalyseurLexical.Token> tokens, JTextArea console) {
        this.tokens = tokens;
        this.console = console;
        this.i = 0;
        this.R = false;
    }

    //Méthode Z 
    public void Z() {
        
        console.append(" DÉBUT DE L'ANALYSE \n");
        
        Program();
        
        
        
        if (!R && i >= tokens.size() - 1) {
            console.append(" RÉSULTAT : CODE VALIDE\n");
        } else {
            console.append(" RÉSULTAT : CODE INVALIDE (Erreurs détectées)\n");
        }
    }

    private boolean match(AnalyseurLexical.TokenType type) {
        if (i < tokens.size() && tokens.get(i).type == type) {
            i++;
            return true;
        }
        return false;
    }

    public void Program() {
        if (i < tokens.size() && tokens.get(i).type != AnalyseurLexical.TokenType.EOF) {
            if (tokens.get(i).type == AnalyseurLexical.TokenType.UNKNOWN) {
                Erreur("Lexicale : Caractère non reconnu '" + tokens.get(i).valeur + "'");
                Program(); return;
            }
            if (tokens.get(i).type == AnalyseurLexical.TokenType.RTAB) return;
            
            Instruction();
            Program();
        }
    }

    public void Instruction() {
        AnalyseurLexical.TokenType type = tokens.get(i).type;

        if (type == AnalyseurLexical.TokenType.FOR) InstructionFor();
        else if (type == AnalyseurLexical.TokenType.CLASS) DeclarationClasse();
        else if (type == AnalyseurLexical.TokenType.DEF) DeclarationMethode();
        else if (type == AnalyseurLexical.TokenType.IDENTIFIER) Affectation();
        else if (type == AnalyseurLexical.TokenType.PRINT) InstructionPrint();
        else if (type == AnalyseurLexical.TokenType.NOM_PRENOM) {
            console.append("   [Signature] " + tokens.get(i).valeur + "\n");
            i++;
        } 
        else if (type == AnalyseurLexical.TokenType.IF || type == AnalyseurLexical.TokenType.WHILE ||
                 type == AnalyseurLexical.TokenType.ELSE || type == AnalyseurLexical.TokenType.ELIF) {
            IgnorerStructure();
        } 
        else {
            Erreur("Instruction non reconnue : " + tokens.get(i).valeur);
        }
    }

   

    public void InstructionFor() {
        if (match(AnalyseurLexical.TokenType.FOR)) {
         if (match(AnalyseurLexical.TokenType.IDENTIFIER)) {
            if (match(AnalyseurLexical.TokenType.IN)) {
                if (match(AnalyseurLexical.TokenType.RANGE)) {
                    if (match(AnalyseurLexical.TokenType.PARENTOUV)) {
                            Expression();
                        if (match(AnalyseurLexical.TokenType.PARENTFERM)) {
                            if (match(AnalyseurLexical.TokenType.DEUXP)) Bloc();
                                else Erreur("':' manquant à la fin du for");
                            } else Erreur("')' manquante après range");
                        } else Erreur("'(' manquante après range");
                 } else Erreur("Mot-clé 'range' manquant");
            } else Erreur("Mot-clé 'in' manquant");
         } else Erreur("Variable manquante après for");
    }
    }

    public void DeclarationClasse() {
        if (match(AnalyseurLexical.TokenType.CLASS)) {
            if (match(AnalyseurLexical.TokenType.IDENTIFIER)) {
                if (match(AnalyseurLexical.TokenType.DEUXP)) Bloc();
                else Erreur("':' manquant après la classe");
            } else Erreur("Nom de classe manquant");
        }
    }

    public void DeclarationMethode() {
        if (match(AnalyseurLexical.TokenType.DEF)) {
            if (match(AnalyseurLexical.TokenType.IDENTIFIER)) {
                if (match(AnalyseurLexical.TokenType.PARENTOUV)) {
                    while (i < tokens.size() && tokens.get(i).type != AnalyseurLexical.TokenType.PARENTFERM && tokens.get(i).type != AnalyseurLexical.TokenType.EOF) i++;
                    if (match(AnalyseurLexical.TokenType.PARENTFERM)) {
                        if (match(AnalyseurLexical.TokenType.DEUXP)) Bloc();
                        else Erreur("':' manquant après la fonction");
                    } else Erreur("')' manquante");
                } else Erreur("'(' manquante");
            } else Erreur("Nom de fonction manquant");
        }
    }

    public void Affectation() {
        if (match(AnalyseurLexical.TokenType.IDENTIFIER)) {
            if (match(AnalyseurLexical.TokenType.ASSIGN)) Expression();
            else Erreur("'=' manquant pour l'affectation");
        }
    }

    public void InstructionPrint() {
        if (match(AnalyseurLexical.TokenType.PRINT)) Expression();
    }

    public void IgnorerStructure() {
        console.append("   [Info] Ignoré : " + tokens.get(i).type + "\n");
        while (i < tokens.size() && tokens.get(i).type != AnalyseurLexical.TokenType.DEUXP && tokens.get(i).type != AnalyseurLexical.TokenType.EOF) i++;
        match(AnalyseurLexical.TokenType.DEUXP);
        if (tokens.get(i).type == AnalyseurLexical.TokenType.LTAB) {
            int niveau = 1; i++;
            while (niveau > 0 && i < tokens.size()) {
                if (tokens.get(i).type == AnalyseurLexical.TokenType.LTAB) niveau++;
                else if (tokens.get(i).type == AnalyseurLexical.TokenType.RTAB) niveau--;
                i++;
            }
        }
    }

    public void Bloc() {
        if (match(AnalyseurLexical.TokenType.LTAB)) {
            Program();
            if (!match(AnalyseurLexical.TokenType.RTAB)) Erreur("Problème indentation");
        } else Instruction();
    }

    public void Expression() {
        Terme();
        if (i < tokens.size()) {
            AnalyseurLexical.TokenType t = tokens.get(i).type;
            if (t == AnalyseurLexical.TokenType.PLUS || t == AnalyseurLexical.TokenType.SUB ||
                t == AnalyseurLexical.TokenType.GT || t == AnalyseurLexical.TokenType.LT ||
                t == AnalyseurLexical.TokenType.EQEQ) {
                i++; Expression();
            }
        }
    }

    public void Terme() {
        if (match(AnalyseurLexical.TokenType.IDENTIFIER) || match(AnalyseurLexical.TokenType.NUMBER)) {}
        else if (match(AnalyseurLexical.TokenType.PARENTOUV)) {
            Expression();
            if (!match(AnalyseurLexical.TokenType.PARENTFERM)) Erreur("')' manquante");
        } else Erreur("Valeur attendue");
    }

    private void Erreur(String msg) {
        R = true;
        String val = "EOF"; if (i < tokens.size()) val = tokens.get(i).valeur;
        
        console.append("   >> ERREUR (Token " + i + " '" + val + "') : " + msg + "\n");
        if (i < tokens.size()) i++;
    }
}

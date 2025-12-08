import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Main extends JFrame {

    private JTextArea zoneCode;   
    private JTextArea zoneResultat; 
    private JButton boutonCompiler;
    private JButton boutonEffacer;

    public Main() {
        
        setTitle("Mini-Compilateur Python (Thème: FOR)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

       
        JLabel titre = new JLabel("Éditeur de Code Python (Compilateur)", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        titre.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        
        zoneCode = new JTextArea();
        zoneCode.setFont(new Font("Monospaced", Font.PLAIN, 14));
       
        zoneCode.setText("x = 10\nfor i in range(x):\n    print i\n    Saidani Rabah\n");
        
        zoneResultat = new JTextArea();
        zoneResultat.setFont(new Font("Monospaced", Font.PLAIN, 14));
        zoneResultat.setEditable(false); 
        zoneResultat.setBackground(new Color(30, 30, 30)); 
        zoneResultat.setForeground(Color.GREEN); 

       
        JScrollPane scrollCode = new JScrollPane(zoneCode);
        scrollCode.setBorder(BorderFactory.createTitledBorder("Code Source"));
        
        JScrollPane scrollResultat = new JScrollPane(zoneResultat);
        scrollResultat.setBorder(BorderFactory.createTitledBorder("Console de Compilation"));

        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollCode, scrollResultat);
        splitPane.setDividerLocation(350); 

      
        JPanel panelBoutons = new JPanel();
        boutonCompiler = new JButton("COMPILER");
        boutonCompiler.setFont(new Font("Arial", Font.BOLD, 14));
        boutonCompiler.setBackground(new Color(0, 120, 215));
        boutonCompiler.setForeground(Color.WHITE);

        boutonEffacer = new JButton("Effacer Console");

        panelBoutons.add(boutonCompiler);
        panelBoutons.add(boutonEffacer);

     
        
        boutonCompiler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lancerCompilation();
            }
        });

        boutonEffacer.addActionListener(e -> zoneResultat.setText(""));

        
        add(titre, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(panelBoutons, BorderLayout.SOUTH);
    }

    private void lancerCompilation() {
        zoneResultat.setText(""); 
        zoneResultat.append(">>> Démarrage de l'analyse...\n\n");

        String code = zoneCode.getText();
        
        if (code.trim().isEmpty()) {
            zoneResultat.append("Le code est vide !\n");
            return;
        }

        try {
            // 1. Lexer
            AnalyseurLexical lexer = new AnalyseurLexical(code);
            List<AnalyseurLexical.Token> tokens = lexer.tokeniser();
            
            
            zoneResultat.append("--- TOKENS ---\n");
            for (AnalyseurLexical.Token t : tokens) {
                
                zoneResultat.append(t.toString() + "\n");
            }
            zoneResultat.append("\n");

            // 2. Parser 
            AnalyseurSyntaxique parser = new AnalyseurSyntaxique(tokens, zoneResultat);
            parser.Z();

        } catch (Exception ex) {
            zoneResultat.append("\n ERREUR CRITIQUE : " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}

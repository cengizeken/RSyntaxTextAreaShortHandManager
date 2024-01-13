import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShorthandManagerV2 {
    static JTextField completionParameter;
    private AutoCompletion ac;
    private DefaultCompletionProvider provider;
    // Other methods and your existing code here...
    public ShorthandManagerV2(RSyntaxTextArea textComponent) {
        provider = createCompletionProvider();
        ac = new AutoCompletion(provider);
        ac.setShowDescWindow(true);
        ac.setParameterAssistanceEnabled(true);

        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setAutoCompleteSingleChoices(true);
        ac.setAutoActivationDelay(800);

        ac.install(textComponent);

        // Set the tooltip text provider for the text area
        textComponent.setToolTipSupplier((rsyntaxTextArea, mouseEvent) -> {
            // Customize the tooltip text based on the current context or caret position
            int offset = rsyntaxTextArea.viewToModel(mouseEvent.getPoint());

            return getToolTipText(textComponent, offset);
        });
    }

    private DefaultCompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.setAutoActivationRules(true, ".");
        // Add initial shorthand completions
        addShorthandCompletion(provider, "if", "If Statement", "if description", "if summary");
        addShorthandCompletion(provider, "for", "For Loop", "for description", "for summary");
        addShorthandCompletion(provider, "while", "While Loop", "while description", "while summary");

        return provider;
    }

    public void addShorthandCompletion(DefaultCompletionProvider provider, String shorthand, String expansion, String description, String summary) {
        ShorthandCompletion completion = new ShorthandCompletion(provider, shorthand, expansion);
        completion.setSummary(summary);
        provider.addCompletion(new ShorthandCompletion(provider, shorthand, expansion, null, summary));
    }

    public void updateShorthandCompletion(JTextComponent textComponent, String oldShorthand, String newShorthand, String newExpansion, String description, String summary) {
        // Find the existing shorthand completion
        Completion existingCompletion = findCompletionByShorthand(provider, textComponent, oldShorthand);

        // Remove existing shorthand completion
        if (existingCompletion != null) {
            provider.removeCompletion(existingCompletion);
        }
        // Add updated shorthand completion
        addShorthandCompletion(provider, newShorthand, newExpansion, null, summary);
    }

    public void deleteShorthandCompletion(JTextComponent textComponent, String shorthand) {
        // Find the existing shorthand completion
        Completion existingCompletion = findCompletionByShorthand(provider, textComponent, shorthand);

        // Remove existing shorthand completion
        if (existingCompletion != null) {
            provider.removeCompletion(existingCompletion);
        }
    }

    private Completion findCompletionByShorthand(CompletionProvider provider, JTextComponent textComponent, String shorthand) {
        List<Completion> completions = provider.getCompletions(textComponent);
        for (Completion completion : completions) {
            if (completion.getInputText().equals(shorthand)) {
                return completion;
            }
        }
        return null;
    }

    // Updated method to find specific keywords, shorthands, and words between # characters
    private String getToolTipText(RSyntaxTextArea textArea, int offset) {
        try {
            RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();

            // Get the current line text using Segment
            int line = textArea.getLineOfOffset(offset);
            int start = textArea.getLineStartOffset(line);
            int end = textArea.getLineEndOffset(line);
            Segment segment = new Segment();
            doc.getText(start, end - start, segment);

            // Use a regular expression to find words between # characters
            Pattern pattern = Pattern.compile("#(.*?)#");
            Matcher matcher = pattern.matcher(segment.toString());

            while (matcher.find()) {
                String foundText = matcher.group(1);

                // Check if the mouse position is within the bounds of this replacement text
                int replacementStart = start + matcher.start();
                int replacementEnd = start + matcher.end();
                if (offset >= replacementStart && offset <= replacementEnd) {
                    return "Replacement Text: " + foundText;
                }
            }

            // Check for specific keywords, custom tokens, and shorthands
            TokenMakerFactory tokenMakerFactory = TokenMakerFactory.getDefaultInstance();
            Token currentToken = tokenMakerFactory.getTokenMaker("text/java").getTokenList(segment, TokenTypes.NULL, start);

            // Continue with the rest of the method...

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // Check if the token is a shorthand replacement
    private boolean isShorthandReplacement(Token token) {
        // Add your logic to check if the token is a replacement in shorthand completion
        // You may need to adapt this based on the actual implementation of shorthand completions
        return token.getType() == TokenTypes.IDENTIFIER && token.getLexeme().startsWith("#") && token.getLexeme().endsWith("#");
    }

    // Extract shorthand from the replacement text (expansion)
    private String getShorthandFromReplacement(String replacement) {
        // Add your logic to extract shorthand from the replacement text
        // You may need to adapt this based on the actual implementation of shorthand completions
        return replacement.substring(1, replacement.length() - 1);
    }

    // Get the summary for a shorthand
    private String getSummaryForShorthand(String shorthand) {
        // Add your logic to get the summary for the shorthand
        // You may need to adapt this based on the actual implementation of shorthand completions
        return "Summary for " + shorthand;
    }

    // Check if the token is a custom token
    private boolean isCustomToken(Token token) {
        // Add your custom token checks here
        return "Sine".equals(token.getLexeme()) ||
                "Cosine".equals(token.getLexeme()) ||
                "rugged".equals(token.getLexeme());
    }

    // Other methods and your existing code here...
    public static void main(String[] args) {
        // Example usage
        JFrame jframe = new JFrame("RSyntaxArea Shorthand Completion Example");
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        ShorthandManagerV2 shorthandManager = new ShorthandManagerV2(textArea);

        GridBagLayout gbl = new GridBagLayout();
        jframe.setLayout(gbl);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagConstraints textAreaGbc = new GridBagConstraints();
        textAreaGbc.gridx = 0;
        textAreaGbc.gridy = 0;
        textAreaGbc.fill = GridBagConstraints.BOTH;
        textAreaGbc.weightx = 1;
        textAreaGbc.weighty = 1;

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridy = 1;
        buttonGbc.gridx = 0;
        buttonGbc.fill = GridBagConstraints.BOTH;
        buttonGbc.weightx = 1;
        buttonGbc.weighty = 0.1;
        JButton button = new JButton("ekle");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update an existing shorthand completion
                shorthandManager.updateShorthandCompletion(textArea, "Corriolis Etkisi", "Corriolis Etkisi" + completionParameter.getText(), "#Corriolis etkisideğeri#", "ife description", "Corriolis Etkisi : 0.246");
            }
        });

        GridBagConstraints completionSil = new GridBagConstraints();
        completionSil.gridx = 0;
        completionSil.gridy = 2;
        completionSil.fill = GridBagConstraints.BOTH;
        completionSil.weighty = 0.1;
        completionSil.weightx = 1;
        JButton btnCompletionSil = new JButton("Sil");
        btnCompletionSil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Delete an existing shorthand completion
                shorthandManager.deleteShorthandCompletion(textArea, "for");
            }
        });

        GridBagConstraints completionEkleText = new GridBagConstraints();
        completionEkleText.gridx = 0;
        completionEkleText.gridy = 3;
        completionEkleText.fill = GridBagConstraints.BOTH;
        completionEkleText.weighty = 0.1;
        completionEkleText.weightx = 1;
        completionParameter = new JFormattedTextField();

        //textArea.setToolTipSupplier((ToolTipSupplier)shorthandManager.provider);
        ToolTipManager.sharedInstance().registerComponent(textArea);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);

        // Add a new shorthand completion
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "sysout", "#System.out.println()#", "descrition-sysout", "summary-sysout");
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "Corriolis Etkisi", "#Corriolis etkisideğeri#", "0.245", "Corriolis Etkisi : 0.245");
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "Etkisi", "Etkisi", "0.245", "Etkisi : 0.245");

        jframe.setSize(new Dimension(300, 400));
        jframe.getContentPane().add(new RTextScrollPane(textArea, true), textAreaGbc);
        jframe.getContentPane().add(button, buttonGbc);
        jframe.getContentPane().add(btnCompletionSil, completionSil);
        jframe.getContentPane().add(completionParameter, completionEkleText);
        //jframe.getContentPane().add(new RTextScrollPane(textArea, true));
        jframe.setVisible(true);
    }
}

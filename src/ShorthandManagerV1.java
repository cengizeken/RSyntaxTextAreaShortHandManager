import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ShorthandManagerV1 {
    static JTextField completionParameter;
    private AutoCompletion ac;
    public DefaultCompletionProvider provider;

    public ShorthandManagerV1(RSyntaxTextArea textComponent) {
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

    // Method to provide tooltip text based on the current context
    private String getToolTipText(RSyntaxTextArea textArea, int offset) {
        // You can implement logic to determine the tooltip text based on the caret position or other factors
        // For example, you might want to provide information about the current code element at the caret position

        try {
            // Get the word under the mouse cursor
            int start = RSyntaxUtilities.getWordStart(textArea, offset);
            int end = RSyntaxUtilities.getWordEnd(textArea, offset);
            String wordUnderCursor = textArea.getText(start, end - start);
            System.out.println(wordUnderCursor);
            // Customize the tooltip text based on the word under the cursor
            if ("if".equals(wordUnderCursor)) {
                return "This is an 'if' statement.";
            } else if ("for".equals(wordUnderCursor)) {
                return "This is a 'for' loop.";
            } else if ("while".equals(wordUnderCursor)) {
                return "This is a 'while' loop.";
            }
            // Add more conditions based on your specific words

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public static void main(String[] args) {
        // Example usage
        JFrame jframe = new JFrame("RSyntaxArea Shorthand Completion Example");
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        ShorthandManagerV1 shorthandManager = new ShorthandManagerV1(textArea);

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
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "sysout", "System.out.println()", "descrition-sysout", "summary-sysout");
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "Sistem Etkisi", "#Sistem etkisi değeri#", "0.245", "Sistem Etkisi : 0.245");

        jframe.setSize(new Dimension(300, 400));
        jframe.getContentPane().add(new RTextScrollPane(textArea, true), textAreaGbc);
        jframe.getContentPane().add(button, buttonGbc);
        jframe.getContentPane().add(btnCompletionSil, completionSil);
        jframe.getContentPane().add(completionParameter, completionEkleText);
        //jframe.getContentPane().add(new RTextScrollPane(textArea, true));
        jframe.setVisible(true);
    }
}
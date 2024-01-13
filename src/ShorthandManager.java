import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.annotation.processing.Completions;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShorthandManager {
    List<Completion> shorthandCompletions;
    static JTextField completionParameter;
    private AutoCompletion ac;
    private DefaultCompletionProvider provider;
    // Other methods and your existing code here...
    public ShorthandManager(RSyntaxTextArea textComponent) {
        provider = createCompletionProvider();
        shorthandCompletions = provider.getCompletions(textComponent);
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
            //int offset = rsyntaxTextArea.viewToModel(mouseEvent.getPoint());

            //return getToolTipText(textComponent, offset);
            return getToolTipText(textComponent, mouseEvent.getPoint().x, mouseEvent.getPoint().y);
        });
    }

    private DefaultCompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.setAutoActivationRules(true, ".");
        // Add initial shorthand completions
        addShorthandCompletion(provider, "if", "if ", "if description", "if summary");
        addShorthandCompletion(provider, "for", "For Loop ", "for description", "for summary");
        addShorthandCompletion(provider, "while", "while ", "while description", "while summary");

        return provider;
    }

    public void addShorthandCompletion(DefaultCompletionProvider provider, String shorthand, String expansion, String description, String summary) {
        ShorthandCompletion completion = new ShorthandCompletion(provider, shorthand, expansion);
        completion.setSummary(summary);
        provider.addCompletion(new ShorthandCompletion(provider, shorthand, expansion, null, summary));
    }

    public void updateShorthandCompletion(JTextComponent textComponent, String oldShorthand, String newShorthand, String newExpansion, String description, String summary) {

        try {
            // Find the existing shorthand completion
            Completion existingCompletion = findCompletionByShorthand(provider, textComponent, oldShorthand);

            // Remove existing shorthand completion
            if (existingCompletion != null) {
                provider.removeCompletion(existingCompletion);
                System.out.println("Removed existing completion for shorthand: " + oldShorthand);
            } else
                System.out.println("No existing completion found for shorthand: " + oldShorthand);
            // Add updated shorthand completion
            addShorthandCompletion(provider, newShorthand, newExpansion, null, summary);
            System.out.println("Added updated completion for shorthand: " + newShorthand);
        } catch (Exception e) {
        }
    }

    /*public void updateShorthandCompletion(JTextComponent textComponent, String oldShorthand, String newShorthand, String newExpansion, String description, String summary) {
        try {
            // Find the existing shorthand completion
            Completion existingCompletion = findCompletionByShorthand(provider, textComponent, oldShorthand);

            // Remove existing shorthand completion
            if (existingCompletion != null) {
                provider.removeCompletion(existingCompletion);
                System.out.println("Removed existing completion for shorthand: " + oldShorthand);
            } else {
                System.out.println("No existing completion found for shorthand: " + oldShorthand);
            }

            // Check if the new shorthand already exists before adding
            if (findCompletionByShorthand(provider, textComponent, newShorthand) == null) {
                // Add updated shorthand completion
                addShorthandCompletion(provider, newShorthand, newExpansion, null, summary);
                System.out.println("Added updated completion for shorthand: " + newShorthand);
            } else {
                System.out.println("New shorthand already exists: " + newShorthand);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }*/
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

    /*private Completion findCompletionByShorthand(DefaultCompletionProvider provider, JTextComponent textComponent, String shorthand) {
        // Check if any completion contains the shorthand
        for (Completion completion : provider.getCompletions(textComponent)) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                if (shorthandCompletion.getInputText().contains(shorthand)) {
                    return shorthandCompletion;
                }
            }
        }
        return null;
    }*/

    private ShorthandCompletion findCompletionByShorthand(DefaultCompletionProvider provider, JTextComponent textComponent, String shorthand) {
        // Check if any completion exactly matches the shorthand
        //for (Completion completion : provider.getCompletions(textComponent)) { bunun yerine aşağıdaki
        //getCompletionByInputText i kullanmak, text alana sonunda boşluk olmadan bir harf/kelime
        //yazdıktan sonra getCompletions yapınca null dönmesi durumunu çözdü
        //ihtiyacım olan sadece completion varsa summary yi güncelle, yoksa yeni completion u ve summary yi ekle idi
            for (Completion completion : provider.getCompletionByInputText(shorthand)) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                if (shorthandCompletion.getInputText().equals(shorthand)) {
                    return shorthandCompletion;
                }
            }
        }
        return null;
    }
    // Updated method to find specific keywords, shorthands, and words between # characters
    private String getToolTipTextv1(RSyntaxTextArea textArea, int mouseX, int mouseY) {
        try {
            RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
            shorthandCompletions = provider.getCompletions(textArea);
            System.out.println("shorthandCompletions size" + shorthandCompletions.size());
            // Get the offset based on the mouse coordinates
            int offset = textArea.viewToModel(new Point(mouseX, mouseY));
            System.out.println("Mouse X: " + mouseX + ", Mouse Y: " + mouseY + ", Offset: " + offset);

            // Get the current line text using Segment
            int line = textArea.getLineOfOffset(offset);
            int start = textArea.getLineStartOffset(line);
            int end = textArea.getLineEndOffset(line);
            Segment segment = new Segment();
            doc.getText(start, end - start, segment);
            System.out.println("Current Line Text: " + segment.toString());


            // Use a regular expression to find words between # characters
            Pattern pattern = Pattern.compile("#(.*?)#");
            Matcher matcher = pattern.matcher(segment.toString());

            while (matcher.find()) {
                String foundText = matcher.group(1);

                // Check if the mouse position is within the bounds of this replacement text
                int replacementStart = start + matcher.start();
                int replacementEnd = start + matcher.end();
                if (offset >= replacementStart && offset <= replacementEnd) {
                    // Check if the found text matches a replacement in the ShorthandCompletion list
                    ShorthandCompletion matchingCompletion = findMatchingCompletion(foundText);
                    if (matchingCompletion != null) {
                        return "Input Text: " + matchingCompletion.getInputText();
                    } else {
                        return "Replacement Text: " + foundText;
                    }
                }
            }

            // Check for specific keywords, custom tokens, and shorthands
            TokenMakerFactory tokenMakerFactory = TokenMakerFactory.getDefaultInstance();
            Token currentToken = tokenMakerFactory.getTokenMaker("text/java").getTokenList(segment, TokenTypes.NULL, start);

            while (currentToken != null && currentToken.isPaintable()) {
                int currentTokenStart = currentToken.getOffset();
                int currentTokenEnd = currentTokenStart + currentToken.length();
                if (offset >= currentTokenStart && offset <= currentTokenEnd) {
                    return "Token: " + currentToken.getLexeme();
                }
                currentToken = currentToken.getNextToken();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getToolTipText(RSyntaxTextArea textArea, int mouseX, int mouseY) {
        try {
            RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
            shorthandCompletions = provider.getCompletions(textArea);

            // Get the offset based on the mouse coordinates
            int offset = textArea.viewToModel(new Point(mouseX, mouseY));
            System.out.println("Mouse X: " + mouseX + ", Mouse Y: " + mouseY + ", Offset: " + offset);

            // Get the current line text using Segment
            int line = textArea.getLineOfOffset(offset);
            int start = textArea.getLineStartOffset(line);
            int end = textArea.getLineEndOffset(line);
            Segment segment = new Segment();
            doc.getText(start, end - start, segment);
            System.out.println("Current Line Text: " + segment.toString());

            // Use a regular expression to find words between # characters
            Pattern pattern = Pattern.compile("#(.*?)#");
            Matcher matcher = pattern.matcher(segment.toString());

            while (matcher.find()) {
                String foundText = matcher.group(1);
                System.out.println("Found Text->" + foundText);
                // Check if the mouse position is within the bounds of this replacement text
                int replacementStart = start + matcher.start();
                int replacementEnd = start + matcher.end();
                if (offset >= replacementStart && offset <= replacementEnd) {
                    // Check if the found text matches a replacement in the ShorthandCompletion list
                    ShorthandCompletion matchingCompletion = findMatchingCompletion(foundText);
                    if (matchingCompletion != null) {
                        return "Input Text: " + matchingCompletion.getInputText();
                    } else {
                        return "Replacement Text: " + foundText;
                    }
                }
            }

            // ... (remaining code)

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to find a matching ShorthandCompletion for the given replacement text
    private ShorthandCompletion findMatchingCompletionV1(String replacementText) {
        for (Completion completion : shorthandCompletions) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                // Handle ShorthandCompletion
                if (completion.getReplacementText().equals("#" + replacementText + "#")) {
                    return shorthandCompletion;
                }
            } else {
                System.out.println("Başka Bir completion türü");
            }

        }
        return null;
    }

    // Method to find a matching ShorthandCompletion for the given replacement text
    private ShorthandCompletion findMatchingCompletionV3(String replacementText) {
        for (Completion completion : shorthandCompletions) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                String shorthand = shorthandCompletion.getInputText();
                String replacementText1 = shorthandCompletion.getReplacementText();
                String summary = shorthandCompletion.getSummary();

                // Check if the replacement text exactly matches or is within the # symbols
                if (replacementText1.equals(replacementText) || replacementText1.equals("#" + replacementText + "#")) {
                    return (ShorthandCompletion) completion;
                }
            }
        }
        return null;
    }
    // Method to find a matching ShorthandCompletion for the given replacement text
    private ShorthandCompletion findMatchingCompletionv4(String replacementText) {
        for (Completion completion : shorthandCompletions) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                String shorthand = shorthandCompletion.getInputText();
                String replacementText1 = shorthandCompletion.getReplacementText();
                String summary = shorthandCompletion.getSummary();

                // Check if the replacement text exactly matches or is within the # symbols
                if (replacementText1.equals(replacementText) || replacementText1.equals("#" + replacementText + "#")) {
                    return shorthandCompletion;
                }
            }
        }
        return null;
    }
    private ShorthandCompletion findMatchingCompletionv5(String replacementText) {
        for (Completion completion : shorthandCompletions) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                String shorthand = shorthandCompletion.getInputText();
                String replacementText1 = shorthandCompletion.getReplacementText();
                String summary = shorthandCompletion.getSummary();

                // Check if the replacement text exactly matches or is within the # symbols
                if (replacementText1.equals(replacementText) || replacementText1.matches("#.*\\b" + Pattern.quote(replacementText) + "\\b.*#")) {
                    return shorthandCompletion;
                }

                // Check if the replacement text with spaces after words matches
                if (replacementText1.equals("#" + replacementText + "#") || replacementText1.matches("#" + Pattern.quote(replacementText) + "\\s.*#")) {
                    return shorthandCompletion;
                }
            }
        }
        return null;
    }
    private ShorthandCompletion findMatchingCompletion(String replacementText) {
        for (Completion completion : shorthandCompletions) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                String replacementText1 = shorthandCompletion.getReplacementText();

                // Check if the replacement text is within the # symbols
                if (replacementText1.matches("#.*" + Pattern.quote(replacementText) + ".*#")) {
                    return shorthandCompletion;
                }
            }
        }
        return null;
    }
    // Method to find a matching ShorthandCompletion for the given replacement text
    private ShorthandCompletion findMatchingCompletionv2(String replacementText) {
        for (Completion completion : shorthandCompletions) {
            if (completion instanceof ShorthandCompletion) {
                ShorthandCompletion shorthandCompletion = (ShorthandCompletion) completion;
                // Now you can use shorthandCompletion as a ShorthandCompletion
                String shorthand = shorthandCompletion.getInputText();
                String replacementText1 = shorthandCompletion.getReplacementText();
                String summary = shorthandCompletion.getSummary();

                if (completion.getReplacementText().equals(replacementText)) {
                    return (ShorthandCompletion)completion;
                }
            }
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

        //textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        ShorthandManager shorthandManager = new ShorthandManager(textArea);

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
                shorthandManager.updateShorthandCompletion(textArea, "Corona Etkisi", "Corona Etkisi" + completionParameter.getText(), "#Corona etkisi değeri#", "ife description", "Corona Etkisi : 0.246");

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
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "Corona Etkisi", "#Corona etkisi değeri#", "0.245", "Corona Etkisi : 0.245");
        shorthandManager.addShorthandCompletion(shorthandManager.provider, "Corona Etkisi Sonucu", "#Corona Etkisi Sonucu#", "0.245", "Corona Etkisi Sonucu: 0.245");


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
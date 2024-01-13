import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomAutoCompletion extends AutoCompletion {

    private CustomDescWindow customDescWindow;

    public CustomAutoCompletion(CompletionProvider provider) {
        super(provider);
        setShowDescWindow(true);
        customDescWindow = new CustomDescWindow(this);
        customDescWindow.setDescription("deneme");
    }

    @Override
    public void install(JTextComponent textComponent) {
        super.install(textComponent);
        addMouseListenerToEditorPane(textComponent);
    }

    private void addMouseListenerToEditorPane(JTextComponent textComponent) {
        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                customDescWindow.hideWindow();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // You may need to adjust these coordinates based on your specific needs
                int x = e.getXOnScreen() + 20;
                int y = e.getYOnScreen() + 20;
                customDescWindow.showWindow(x, y);
            }
        });
    }

    private static class CustomDescWindow {

        private JWindow window;
        private JLabel descriptionLabel;

        public CustomDescWindow(CustomAutoCompletion ac) {
            window = new JWindow();
            descriptionLabel = new JLabel();
            window.getContentPane().setLayout(new BorderLayout());
            window.getContentPane().add(descriptionLabel, BorderLayout.CENTER);
        }

        public void showWindow(int x, int y) {
            SwingUtilities.invokeLater(() -> {
                window.setLocation(x, y);
                window.pack();
                window.setVisible(true);
            });
        }

        public void hideWindow() {
            SwingUtilities.invokeLater(() -> window.setVisible(false));
        }

        public void setDescription(String description) {
            descriptionLabel.setText("Custom Description: " + description);
        }
    }
    private static DefaultCompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        provider.addCompletion(new ShorthandCompletion(provider, "sysout", "System.out.println()", "Prints to console"));
        return provider;
    }
}
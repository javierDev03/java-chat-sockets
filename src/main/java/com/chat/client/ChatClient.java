package com.chat.client;

import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatClient extends JFrame {

    private JButton sendButton;
    private JTextField chatInputField;
    private JTextPane chatDisplayPane;  // Usamos JTextPane en lugar de JTextArea para aplicar estilos
    private JTextField nameField;
    private PrintWriter writer;
    private Socket socket;
    private String localUserName;
    private StyledDocument doc;  // Para controlar el formato del texto

    public ChatClient() {
        initComponents();
        connectToServer();
    }

    private void initComponents() {
        // Configuración del JFrame
        setTitle("PetMotrix Messenger");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de visualización del chat con JTextPane
        chatDisplayPane = new JTextPane();
        chatDisplayPane.setEditable(false);
        chatDisplayPane.setBackground(new Color(245, 245, 245)); // Fondo gris claro
        chatDisplayPane.setFont(new Font("Arial", Font.PLAIN, 14));

        // Usamos StyledDocument para controlar la alineación de los mensajes
        doc = chatDisplayPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatDisplayPane);
        add(scrollPane, BorderLayout.CENTER);

        // Panel para el campo de nombre con su etiqueta
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());

        // Etiqueta para el nombre
        JLabel nameLabel = new JLabel("Ingresa tu nombre:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        namePanel.add(nameLabel, BorderLayout.NORTH);

        // Campo para el nombre
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(150, 30)); // Tamaño mínimo ajustado
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        namePanel.add(nameField, BorderLayout.CENTER);

        // Panel de entrada de mensaje y botón enviar
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        // Campo de entrada de mensaje
        chatInputField = new JTextField();
        chatInputField.setBorder(BorderFactory.createTitledBorder("Escribe tu mensaje"));
        chatInputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(chatInputField, BorderLayout.CENTER);

        // Botón de enviar
        sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(30, 144, 255)); // Azul
        sendButton.setForeground(Color.WHITE); // Texto blanco
        sendButton.setFocusPainted(false); // Eliminar borde por defecto
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Añadir el panel de nombre y panel de mensaje a la parte inferior del JFrame
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(namePanel, BorderLayout.NORTH); // Agregamos el panel de nombre arriba
        bottomPanel.add(inputPanel, BorderLayout.SOUTH); // Panel de mensaje y botón abajo

        add(bottomPanel, BorderLayout.SOUTH);

        // Hacer visible el JFrame
        setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket("192.168.100.19", 12345);  // Dirección IP y puerto del servidor
            writer = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new IncomingReader()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendButtonActionPerformed(ActionEvent evt) {
        String name = nameField.getText();
        String message = chatInputField.getText();

        if (!name.isEmpty() && !message.isEmpty()) {
            localUserName = name;  // Guardar el nombre del usuario local

            // Enviar el mensaje al servidor
            writer.println(name + ": " + message);

            // Mostrar el mensaje propio alineado a la derecha
            appendMessage(name + ": " + message, true);
            
            chatInputField.setText(""); // Limpiar el campo del mensaje después de enviarlo
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, introduce tu nombre y mensaje antes de enviar.");
        }
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    // Verificar si el mensaje no es del usuario local
                    if (!message.startsWith(localUserName + ":")) {
                        appendMessage(message, false);  // Mensaje de otros usuarios, alineado a la izquierda
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para agregar un mensaje al chat
    private void appendMessage(String message, boolean isRightAligned) {
        try {
            // Crear estilo para el mensaje
            SimpleAttributeSet style = new SimpleAttributeSet();

            if (isRightAligned) {
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT); // Alineado a la derecha
                StyleConstants.setForeground(style, Color.BLACK);   // Color azul para el usuario local
            } else {
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);  // Alineado a la izquierda
                
                StyleConstants.setForeground(style, new Color(30, 144, 255));
                // Color negro para otros usuarios
            }

            // Aplicar el estilo y añadir el mensaje
            doc.setParagraphAttributes(doc.getLength(), 1, style, false);
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient().setVisible(true);
            }
        });
    }
}

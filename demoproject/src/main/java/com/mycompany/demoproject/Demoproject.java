/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.demoproject;

import View.SignIn.SignInView;

/**
 *
 * @author DELL
 */
public class Demoproject {

    public static void main(String[] args) {
        // Initialize FlatLaf globally here so all views inherit the theme
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo FlatLaf");
        }

        java.awt.EventQueue.invokeLater(() -> {
            String role = Controller.SignIn.AuthProcess.validateToken();
            if ("ADMIN".equals(role)) {
                new View.Admin.Main().setVisible(true);
            } else if ("EMPLOYEE".equals(role)) { // Optional check if there is an employee view
                // new View.Employees.Main().setVisible(true);
                new View.Customers.Main().setVisible(true); // Fallback
            } else {
                new View.Customers.Main().setVisible(true); // Default entry point
            }
        });
    }
}

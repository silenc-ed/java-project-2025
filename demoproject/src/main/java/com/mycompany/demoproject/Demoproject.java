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
            String role = Common.TokenManager.validateLocalToken();
            if ("CUSTOMER".equals(role)) {
                new View.Customers.Main().setVisible(true);
            } else if ("ADMIN".equals(role)) {
                new View.Admin.Main().setVisible(true);
            } else {
                new SignInView().setVisible(true);
            }
        });
    }
}

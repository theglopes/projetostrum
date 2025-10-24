package com.loja.utils;

import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner SC = new Scanner(System.in);

    public static int lerInt(String prompt) {
        System.out.print(prompt);
        while (!SC.hasNextInt()) {
            SC.nextLine();
            System.out.print(prompt);
        }
        int v = SC.nextInt();
        SC.nextLine();
        return v;
    }

    public static String lerLinha(String prompt) {
        System.out.print(prompt);
        return SC.nextLine();
    }

    public static double lerDouble(String prompt) {
        System.out.print(prompt);
        while (!SC.hasNextDouble()) {
            SC.nextLine();
            System.out.print(prompt);
        }
        double v = SC.nextDouble();
        SC.nextLine();
        return v;
    }

    public static void pause() {
        System.out.println("\nPressione Enter para continuar...");
        SC.nextLine();
    }
}
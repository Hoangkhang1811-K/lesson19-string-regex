package baitap1;

import java.util.Scanner;

public class ClassNameValidateApp {
    private static final String REGEX = "^[CAP]\\d{4}[GHIK]$";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Nhap ten lop (enter de thoat): ");
            String name = sc.nextLine();
            if (name.trim().isEmpty()) break;

            if (name.matches(REGEX)) {
                System.out.println("Hop le");
            } else {
                System.out.println("Khong hop le");
            }
        }
    }
}

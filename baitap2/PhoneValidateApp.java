package baitap2;

import java.util.Scanner;

public class PhoneValidateApp {
    private static final String REGEX = "^\\(\\d{2}\\)-\\(0\\d{9}\\)$";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Nhap so dien thoai (enter de thoat): ");
            String phone = sc.nextLine();
            if (phone.trim().isEmpty()) break;

            if (phone.matches(REGEX)) {
                System.out.println("Hop le");
            } else {
                System.out.println("Khong hop le");
            }
        }
    }
}

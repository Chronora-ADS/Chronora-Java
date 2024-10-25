package br.com.senai;

import java.util.Scanner;

public class ProgramaPrincipal {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int opcao;

        do {

            System.out.println("""
                    Selecione a opção desejada:
                    
                    1 - Cadastrar usuário;
                    2- Editar Usuário;
                    3- Listar Usuário;
                    4- Remover Usuário;
                    0- Sair do programa;
                    """);
            System.out.print("Opção: ");
            opcao = sc.nextInt();


            switch (opcao) {
                case 1 -> {
                }
                case 2 -> {
                }
                case 3 -> {
                }
                case 4 -> {
                }
                case 0 -> System.out.println("\nEncerrando programa...");

                default -> System.out.println("\sOpção inválida, tente novamente.");
            }
        } while (opcao != 0);
        System.out.println("\n--------------------------");
        System.out.println("\nPrograma encerrado!");
    }
}

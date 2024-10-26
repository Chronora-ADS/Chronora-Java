package br.com.senai;

import br.com.senai.controller.UsuarioController;
import br.com.senai.entity.UsuarioEntity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Scanner;

public class ProgramaPrincipal {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        DecimalFormat fone = new DecimalFormat("+00 (00) 0000-0000");

        UsuarioController usuarioController = new UsuarioController();


        int opcao;

        do {

            System.out.println("""
                    Selecione a opção desejada:
                    
                    1- Cadastrar usuário
                    2- Editar Usuário
                    3- Listar Usuário
                    4- Remover Usuário
                    0- Sair do programa
                    """);
            System.out.print("Opção: ");
            opcao = sc.nextInt();


            switch (opcao) {
                case 1 -> {
                    System.out.println("Qual o nome do usuário?");
                    sc.nextLine();
                    String nome = sc.nextLine();
                    System.out.println("Qual a idade?");
                    int idade = sc.nextInt();
                    System.out.println("Qual a email?");
                    sc.nextLine();
                    String email = sc.nextLine();
                    System.out.println("Qual o telefone?");
                    long telefone = sc.nextLong();

                    String telefoneStr = Long.toString(telefone);


                    String telefoneFormatado = String.format("+%s (%s) %s-%s",
                            telefoneStr.substring(0, 2),
                            telefoneStr.substring(2, 4),
                            telefoneStr.substring(4, 8),
                            telefoneStr.substring(8, 12)
                    );

                    UsuarioEntity usuarioEntity2 = usuarioController.cadastrarUsuario(new UsuarioEntity(nome, idade, email, telefoneFormatado));
                    System.out.println(usuarioEntity2.toString());
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

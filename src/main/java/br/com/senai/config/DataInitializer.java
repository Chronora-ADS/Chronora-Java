package br.com.senai.config;

import br.com.senai.model.DTO.user.DocumentDTO;
import br.com.senai.model.DTO.user.UserDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.auth.AuthService;
import br.com.senai.service.auth.SupabaseAuthService;
import br.com.senai.service.service.SupabaseStorageService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;
    private final SupabaseStorageService storageService;

    public DataInitializer(UserRepository userRepository, ServiceRepository serviceRepository,
            SupabaseAuthService supabaseAuthService, AuthService authService,
            SupabaseStorageService storageService) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.supabaseAuthService = supabaseAuthService;
        this.authService = authService;
        this.storageService = storageService;
    }

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() == 0) {
                UserEntity user = createDefaultUser();
                createDefaultServices(user);
            } else {
                System.out.println("Supabase já possui dados. Inicialização ignorada.");
            }
        };
    }

    private UserEntity createDefaultUser() throws Exception {
        String plainPassword = "123123";

        UserDTO defaultUser = new UserDTO();
        defaultUser.setName("Bertrania Dude");
        defaultUser.setEmail("email@email.com");
        defaultUser.setPhoneNumber(5547912345678L);
        defaultUser.setPassword(plainPassword);

        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName("documento_padrao.png");
        documentDTO.setType("image/png");

        byte[] imageBytes = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("imagem.png")
        ).readAllBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        documentDTO.setData(base64Image);
        defaultUser.setDocument(documentDTO);

        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("name", defaultUser.getName());
        userMetadata.put("phone", defaultUser.getPhoneNumber());

        var supabaseUserDTO = supabaseAuthService.signUp(
                defaultUser.getEmail(),
                defaultUser.getPassword(),
                userMetadata
        );

        UserEntity createdUser = authService.register(defaultUser, supabaseUserDTO.getId());
        System.out.println("Usuário criado: " + createdUser.getEmail());
        return createdUser;
    }

    private void createDefaultServices(UserEntity userCreator) throws Exception {
        UserEntity managedUser = userRepository.findById(userCreator.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após criação"));

        byte[] serviceImageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("imagem.png")).readAllBytes();
        String base64ServiceImage = "data:image/png;base64," + Base64.getEncoder().encodeToString(serviceImageBytes);
        String serviceImageUrl = storageService.uploadBase64Image(base64ServiceImage, "services", null);

        List<ServiceEntity> servicesToCreate = Arrays.asList(
                // 1. Serviço original corrigido
                createService(
                        "Manutenção Preventiva de Eletrodomésticos",
                        "Procuro um técnico qualificado para realizar manutenção preventiva em geladeira, "
                                + "lavadora e micro-ondas em casa. Preciso de limpeza profunda, verificação "
                                + "de componentes e ajustes para evitar falhas futuras.",
                        6,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(15),
                        createCategoryList("Manutenção", "Técnico", "Eletrodomésticos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 2. Serviço original corrigido
                createService(
                        "Desentupimento de Pia e Vaso Sanitário",
                        "Preciso de um encanador para desentupir a pia da cozinha e o vaso sanitário do "
                                + "banheiro. A situação está urgente e preciso de um profissional confiável "
                                + "que use equipamentos adequados sem danificar a tubulação.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(7),
                        createCategoryList("Encanamento", "Urgente", "Com equipamentos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 3. Serviço original corrigido
                createService(
                        "Instalação de Tomadas e Interruptores",
                        "Estou buscando um eletricista para instalar novas tomadas e interruptores no "
                                + "apartamento, além de atualizar o quadro de luz conforme a norma ABNT "
                                + "NBR 5410. Preciso de segurança e qualidade no serviço.",
                        8,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Elétrica", "Eletricista", "Tomadas", "Interruptores", "Quadro de Luz"),
                        serviceImageUrl,
                        managedUser
                ),
                // 4. Serviço original corrigido
                createService(
                        "Terapia Cognitivo-Comportamental Online",
                        "Procuro um psicólogo registrado e especializado em Terapia Cognitivo-Comportamental "
                                + "(TCC) para atendimento online. Estou enfrentando ansiedade e estresse "
                                + "crônico e busco um profissional com experiência comprovada.",
                        5,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Saúde Mental", "Terapia", "TCC", "Ansiedade"),
                        serviceImageUrl,
                        managedUser
                ),
                // 5. Serviço original corrigido
                createService(
                        "Desenvolvimento de APIs com Spring Boot",
                        "Preciso de um desenvolvedor backend especialista em Spring Boot para criar uma API "
                                + "RESTful robusta, com autenticação JWT, integração com PostgreSQL e "
                                + "tratamento personalizado de exceções.",
                        9,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(12),
                        createCategoryList(
                                "Desenvolvimento de Software",
                                "Backend",
                                "Spring Boot",
                                "Java",
                                "JWT",
                                "PostgreSQL",
                                "Documentação"
                        ),
                        serviceImageUrl,
                        managedUser
                ),
                // 6. Novo serviço
                createService(
                        "Jardinagem e Paisagismo Residencial",
                        "Preciso de um jardineiro para poda de árvores, plantio de flores e manutenção do gramado. "
                                + "O serviço inclui limpeza do quintal e aplicação de adubo orgânico.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(20),
                        createCategoryList("Jardinagem", "Paisagismo", "Poda", "Limpeza"),
                        serviceImageUrl,
                        managedUser
                ),
                // 7. Novo serviço
                createService(
                        "Pintura de Apartamento Completo",
                        "Procuro um pintor profissional para pintar um apartamento de 3 quartos, sala, cozinha e corredor. "
                                + "Inclui preparação de paredes, aplicação de tinta látex e acabamento.",
                        12,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(25),
                        createCategoryList("Pintura", "Reforma", "Acabamento"),
                        serviceImageUrl,
                        managedUser
                ),
                // 8. Novo serviço
                createService(
                        "Reparo de Computador e Remoção de Vírus",
                        "Meu computador está lento e com suspeita de vírus. Preciso de um técnico para diagnóstico, "
                                + "remoção de malwares, formatação e instalação de sistema operacional.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(4),
                        createCategoryList("Informática", "Manutenção de Computadores", "Segurança Digital"),
                        serviceImageUrl,
                        managedUser
                ),
                // 9. Novo serviço
                createService(
                        "Aulas Particulares de Matemática para Ensino Médio",
                        "Estou com dificuldades em matemática, principalmente em funções e geometria. "
                                + "Busco um professor particular para aulas presenciais duas vezes por semana.",
                        2,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(30),
                        createCategoryList("Educação", "Matemática", "Reforço Escolar"),
                        serviceImageUrl,
                        managedUser
                ),
                // 10. Novo serviço
                createService(
                        "Consultoria Financeira para Pessoa Física",
                        "Preciso de um consultor financeiro para me ajudar a organizar minhas dívidas, "
                                + "planejar investimentos e criar um orçamento mensal. Atendimento remoto.",
                        4,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(14),
                        createCategoryList("Finanças", "Consultoria", "Investimentos", "Educação Financeira"),
                        serviceImageUrl,
                        managedUser
                ),
                // 11. Novo serviço
                createService(
                        "Tradução de Documentos Inglês-Português",
                        "Preciso de um tradutor profissional para traduzir um contrato de 10 páginas "
                                + "do inglês para o português. Prazo de 5 dias.",
                        8,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(5),
                        createCategoryList("Tradução", "Idiomas", "Documentos Jurídicos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 12. Novo serviço
                createService(
                        "Design de Logotipo e Identidade Visual",
                        "Estou criando uma marca e preciso de um designer para desenvolver logotipo, "
                                + "paleta de cores e manual de identidade visual. Entregas em alta resolução.",
                        10,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(18),
                        createCategoryList("Design Gráfico", "Identidade Visual", "Logotipo", "Branding"),
                        serviceImageUrl,
                        managedUser
                ),
                // 13. Novo serviço
                createService(
                        "Limpeza Profunda de Estofados e Tapetes",
                        "Preciso de um serviço de limpeza especializado para sofá, poltronas e tapetes. "
                                + "Remoção de manchas, ácaros e odores com equipamento adequado.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(6),
                        createCategoryList("Limpeza", "Estofados", "Tapetes", "Higienização"),
                        serviceImageUrl,
                        managedUser
                ),
                // 14. Novo serviço
                createService(
                        "Montagem e Instalação de Móveis Planejados",
                        "Comprei móveis planejados e preciso de um marceneiro para montagem e fixação na parede. "
                                + "Inclui prateleiras, armários e nichos.",
                        7,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(12),
                        createCategoryList("Marcenaria", "Montagem de Móveis", "Instalação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 15. Novo serviço
                createService(
                        "Treinamento Funcional Online (Personal Trainer)",
                        "Busco um personal trainer para orientar treinos funcionais remotos, com acompanhamento "
                                + "ao vivo e plano individualizado para emagrecimento e condicionamento.",
                        1,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(30),
                        createCategoryList("Educação Física", "Personal Trainer", "Saúde", "Treinamento Funcional"),
                        serviceImageUrl,
                        managedUser
                ),
                // 16
                createService(
                        "Revisão de Carro e Troca de Óleo",
                        "Preciso de um mecânico de confiança para realizar revisão completa no meu carro: "
                                + "troca de óleo, filtros, verificação de freios, suspensão e alinhamento.",
                        15,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(8),
                        createCategoryList("Mecânica", "Automotivo", "Revisão", "Troca de Óleo"),
                        serviceImageUrl,
                        managedUser
                ),
                // 17
                createService(
                        "Aulas de Violão para Iniciantes",
                        "Sempre quis aprender violão. Busco um professor paciente para aulas presenciais "
                                + "uma vez por semana, ensinando acordes básicos e ritmos.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(20),
                        createCategoryList("Música", "Violão", "Aulas Particulares"),
                        serviceImageUrl,
                        managedUser
                ),
                // 18
                createService(
                        "Limpeza de Ar-Condicionado Split",
                        "Preciso de um técnico especializado para limpeza de 3 aparelhos de ar-condicionado split. "
                                + "Inclui desmontagem, limpeza das bobinas, bandejas e aplicação de produto bactericida.",
                        9,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(14),
                        createCategoryList("Ar-Condicionado", "Limpeza", "Manutenção Preventiva"),
                        serviceImageUrl,
                        managedUser
                ),
                // 19
                createService(
                        "Desenvolvimento de E-commerce com Shopify",
                        "Preciso de um desenvolvedor para criar uma loja virtual no Shopify com integração "
                                + "de pagamentos, frete automático e catálogo de 50 produtos.",
                        18,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(25),
                        createCategoryList("E-commerce", "Shopify", "Desenvolvimento Web", "Integrações"),
                        serviceImageUrl,
                        managedUser
                ),
                // 20
                createService(
                        "Massoterapia Relaxante Domiciliar",
                        "Procuro um massoterapeuta para atendimento domiciliar com foco em relaxamento profundo. "
                                + "Preferência por técnicas como quick massage ou shantala para adultos.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(3),
                        createCategoryList("Bem-Estar", "Massagem", "Relaxamento", "Saúde"),
                        serviceImageUrl,
                        managedUser
                ),
                // 21
                createService(
                        "Tradução Juramentada de Certidão de Nascimento",
                        "Preciso de um tradutor juramentado para traduzir minha certidão de nascimento do português "
                                + "para o inglês, com validade para uso no exterior.",
                        7,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Tradução Juramentada", "Documentos Oficiais", "Inglês"),
                        serviceImageUrl,
                        managedUser
                ),
                // 22
                createService(
                        "Instalação de Câmeras de Segurança",
                        "Busco um técnico em segurança eletrônica para instalar 4 câmeras de vigilância "
                                + "Wi-Fi na parte externa da minha casa, com configuração para acesso remoto.",
                        11,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(18),
                        createCategoryList("Segurança Eletrônica", "Câmeras", "Instalação", "Monitoramento"),
                        serviceImageUrl,
                        managedUser
                ),
                // 23
                createService(
                        "Revisão de Texto e Normalização ABNT",
                        "Preciso de um revisor para corrigir gramática, ortografia e formatar meu TCC "
                                + "segundo as normas da ABNT (citações, referências, margens).",
                        4,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(9),
                        createCategoryList("Revisão de Texto", "Normalização ABNT", "TCC", "Acadêmico"),
                        serviceImageUrl,
                        managedUser
                ),
                // 24
                createService(
                        "Podcast de Árvores Frutíferas",
                        "Preciso de um jardineiro especializado em poda de árvores frutíferas (laranjeira, "
                                + "limoeiro e jabuticabeira) para aumentar a produção e saúde das plantas.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(22),
                        createCategoryList("Jardinagem", "Poda", "Árvores Frutíferas"),
                        serviceImageUrl,
                        managedUser
                ),
                // 25
                createService(
                        "Aulas de Inglês para Viagem",
                        "Busco um professor de inglês para aulas online focadas em conversação para viagem. "
                                + "Preciso aprender frases úteis, vocabulário de aeroporto e restaurantes.",
                        2,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(35),
                        createCategoryList("Idiomas", "Inglês", "Conversação", "Viagem"),
                        serviceImageUrl,
                        managedUser
                ),
                // 26
                createService(
                        "Manutenção de Portão Eletrônico",
                        "Meu portão eletrônico está com falha no motor e não abre completamente. "
                                + "Procuro um técnico especializado em automação para reparo e lubrificação.",
                        6,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(5),
                        createCategoryList("Automação", "Portão Eletrônico", "Manutenção Elétrica"),
                        serviceImageUrl,
                        managedUser
                ),
                // 27
                createService(
                        "Produção de Conteúdo para Redes Sociais",
                        "Preciso de um social media ou redator para criar posts semanais para Instagram e Facebook. "
                                + "Inclui legendas, hashtags e sugestões de imagens para um negócio de moda.",
                        8,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(15),
                        createCategoryList("Marketing Digital", "Redes Sociais", "Conteúdo", "Copywriting"),
                        serviceImageUrl,
                        managedUser
                ),
                // 28
                createService(
                        "Desentupimento de Ralo e Caixa de Gordura",
                        "Preciso de um desentupidor profissional para limpeza de ralo da cozinha e caixa de gordura "
                                + "do prédio. Cheiro forte e água parada.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(2),
                        createCategoryList("Desentupimento", "Encanamento", "Caixa de Gordura"),
                        serviceImageUrl,
                        managedUser
                ),
                // 29
                createService(
                        "Orientação para Declaração de Imposto de Renda",
                        "Procuro um contador para me ajudar a declarar meu Imposto de Renda 2025, com recibos "
                                + "de serviços autônomos e investimentos. Atendimento online.",
                        7,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(18),
                        createCategoryList("Contabilidade", "Imposto de Renda", "Declaração", "Finanças"),
                        serviceImageUrl,
                        managedUser
                ),
                // 30
                createService(
                        "Instalação de Piso Vinílico",
                        "Quero instalar piso vinílico em um quarto de 12m². Preciso de um profissional experiente "
                                + "que faça o assentamento com encaixe ou cola, incluindo rodapés.",
                        10,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(16),
                        createCategoryList("Pisos", "Reforma", "Vinílico", "Instalação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 31
                createService(
                        "Desenvolvimento de Aplicativo Flutter",
                        "Preciso de um desenvolvedor mobile para criar um app de tarefas simples com login "
                                + "e notificações push. Backend em Firebase. Entregar código e documentação.",
                        14,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(28),
                        createCategoryList("Desenvolvimento Mobile", "Flutter", "Firebase", "Android/iOS"),
                        serviceImageUrl,
                        managedUser
                ),
                // 32
                createService(
                        "Lavagem de Sofá e Colchão",
                        "Preciso de higienização profissional do meu sofá de tecido e colchão de casal. "
                                + "Remoção de manchas, ácaros e odores com equipamento de vapor.",
                        6,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(9),
                        createCategoryList("Limpeza", "Sofá", "Colchão", "Higienização"),
                        serviceImageUrl,
                        managedUser
                ),
                // 33
                createService(
                        "Aulas de Programação Python para Iniciantes",
                        "Busco um instrutor para aulas online de Python. Quero aprender lógica de programação, "
                                + "estruturas de dados e loops para migrar de carreira.",
                        3,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(25),
                        createCategoryList("Programação", "Python", "Educação", "Iniciantes"),
                        serviceImageUrl,
                        managedUser
                ),
                // 34
                createService(
                        "Reparo de Máquina de Lavar Roupa",
                        "Minha máquina de lavar está vazando água e não centrifuga. Procuro um técnico "
                                + "especializado em eletrodomésticos para diagnóstico e conserto rápido.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(3),
                        createCategoryList("Eletrodomésticos", "Máquina de Lavar", "Conserto"),
                        serviceImageUrl,
                        managedUser
                ),
                // 35
                createService(
                        "Edição de Vídeo para YouTube",
                        "Preciso de um editor de vídeo para cortar, adicionar transições, legendas e thumbnail "
                                + "para um canal de jogos. Vídeos com até 15 minutos.",
                        8,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(12),
                        createCategoryList("Edição de Vídeo", "YouTube", "Thumbnail", "Adobe Premiere"),
                        serviceImageUrl,
                        managedUser
                ),
                // 36
                createService(
                        "Vistoria Técnica para Compra de Imóvel",
                        "Estou comprando um apartamento e preciso de um engenheiro civil para vistoria "
                                + "completa: estrutura, hidráulica, elétrica, infiltrações e laudo final.",
                        12,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(20),
                        createCategoryList("Engenharia Civil", "Vistoria", "Laudo Técnico", "Imóvel"),
                        serviceImageUrl,
                        managedUser
                ),
                // 37
                createService(
                        "Ensinar Espanhol para Crianças",
                        "Busco uma professora de espanhol para meu filho de 8 anos. Aulas lúdicas e presenciais "
                                + "2 vezes por semana, com foco em vocabulário básico e pronúncia.",
                        2,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(30),
                        createCategoryList("Idiomas", "Espanhol", "Educação Infantil", "Aulas Particulares"),
                        serviceImageUrl,
                        managedUser
                ),
                // 38
                createService(
                        "Manutenção de Impressora Jato de Tinta",
                        "Minha impressora está com impressão riscada e falha de alimentação de papel. "
                                + "Preciso de um técnico de informática para limpeza de cabeçotes e reparo.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(6),
                        createCategoryList("Informática", "Impressora", "Manutenção", "Hardware"),
                        serviceImageUrl,
                        managedUser
                ),
                // 39
                createService(
                        "Criação de Planilha de Controle Financeiro",
                        "Preciso de um especialista em Excel/Google Sheets para criar uma planilha automatizada "
                                + "de controle de gastos, com gráficos e categorias personalizadas.",
                        5,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(7),
                        createCategoryList("Excel", "Planilhas", "Automação", "Finanças Pessoais"),
                        serviceImageUrl,
                        managedUser
                ),
                // 40
                createService(
                        "Poda de Árvore de Grande Porte",
                        "Tenho uma árvore de mais de 8 metros no quintal com galhos secos sobre o telhado. "
                                + "Preciso de um jardineiro com equipamento de segurança e descarte dos galhos.",
                        8,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(14),
                        createCategoryList("Jardinagem", "Poda de Árvores", "Árvore Grande", "Descarte"),
                        serviceImageUrl,
                        managedUser
                ),
                // 41
                createService(
                        "Apoio Psicológico para Adolescentes (Online)",
                        "Procuro um psicólogo com experiência em adolescentes para atendimento online. "
                                + "Meu filho está ansioso e com baixa autoestima.",
                        4,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(5),
                        createCategoryList("Psicologia", "Adolescentes", "Ansiedade", "Saúde Mental"),
                        serviceImageUrl,
                        managedUser
                ),
                // 42
                createService(
                        "Troca de Fechadura e Cilindro",
                        "Preciso de um chaveiro para trocar a fechadura da porta de entrada e instalar "
                                + "cilindro de segurança com 3 chaves reserva.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(4),
                        createCategoryList("Chaveiro", "Fechaduras", "Segurança Residencial"),
                        serviceImageUrl,
                        managedUser
                ),
                // 43
                createService(
                        "Migração de Site para WordPress",
                        "Tenho um site estático em HTML e quero migrar para WordPress com mesmo design. "
                                + "Inclui instalação de temas, plugins e redirecionamento de URLs.",
                        11,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(22),
                        createCategoryList("WordPress", "Migração de Site", "Desenvolvimento Web"),
                        serviceImageUrl,
                        managedUser
                ),
                // 44
                createService(
                        "Limpeza de Calhas e Telhado",
                        "Preciso de um serviço de limpeza de calhas do telhado, remoção de folhas e sujeira, "
                                + "e verificação de vazamentos. Acesso por escada.",
                        6,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Limpeza", "Calhas", "Telhado", "Manutenção Predial"),
                        serviceImageUrl,
                        managedUser
                ),
                // 45
                createService(
                        "Tradução de Currículo para Alemão",
                        "Preciso de um tradutor profissional para traduzir meu currículo e carta de apresentação "
                                + "do português para o alemão, com foco em termos corporativos.",
                        5,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(8),
                        createCategoryList("Tradução", "Alemão", "Currículo", "Recursos Humanos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 46
                createService(
                        "Instalação de Ventilador de Teto",
                        "Comprei um ventilador de teto com 4 pás e preciso de um eletricista para instalação "
                                + "no lugar do lustre antigo, incluindo balanceamento.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(6),
                        createCategoryList("Elétrica", "Ventilador de Teto", "Instalação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 47
                createService(
                        "Coaching de Carreira e Recolocação",
                        "Procuro um coach de carreira para me ajudar a redefinir objetivos profissionais, "
                                + "revisar LinkedIn e simular entrevistas. Atendimento remoto.",
                        7,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(19),
                        createCategoryList("Coaching", "Carreira", "LinkedIn", "Recolocação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 48
                createService(
                        "Dedetização de Cupins",
                        "Identifiquei cupins em móveis de madeira e rodapés. Preciso de uma empresa especializada "
                                + "em dedetização residencial, com tratamento localizado.",
                        9,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(12),
                        createCategoryList("Dedetização", "Cupins", "Pragas", "Madeira"),
                        serviceImageUrl,
                        managedUser
                ),
                // 49
                createService(
                        "Desenvolvimento de Chatbot para WhatsApp",
                        "Preciso de um desenvolvedor para criar um chatbot simples para WhatsApp Business "
                                + "que responda perguntas frequentes e agende horários.",
                        13,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(27),
                        createCategoryList("Chatbot", "WhatsApp", "Automação", "Atendimento"),
                        serviceImageUrl,
                        managedUser
                ),
                // 50
                createService(
                        "Reforma de Banheiro Pequeno",
                        "Quero reformar meu banheiro de 3m²: trocar piso, azulejos, vaso sanitário e instalar "
                                + "box de vidro. Preciso de um pedreiro com referências.",
                        18,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(35),
                        createCategoryList("Reforma", "Banheiro", "Pedreiro", "Azulejo", "Hidráulica"),
                        serviceImageUrl,
                        managedUser
                ),
                // 51
                createService(
                        "Aulas de Fotografia com Smartphone",
                        "Gostaria de aprender técnicas de fotografia usando apenas o celular. Conteúdo: "
                                + "composição, luz, edição básica e dicas para redes sociais.",
                        2,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(14),
                        createCategoryList("Fotografia", "Smartphone", "Edição", "Redes Sociais"),
                        serviceImageUrl,
                        managedUser
                ),
                // 52
                createService(
                        "Remoção de Vírus e Otimização de PC",
                        "Computador com pop-ups, lento e programas suspeitos. Preciso de técnico para remoção "
                                + "de malwares, limpeza de registro e instalação de antivírus.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(2),
                        createCategoryList("Informática", "Remoção de Vírus", "Otimização", "Windows"),
                        serviceImageUrl,
                        managedUser
                ),
                // 53
                createService(
                        "Elaboração de Plano de Negócios",
                        "Estou abrindo uma cafeteria e preciso de um consultor para elaborar plano de negócios "
                                + "com análise de mercado, fluxo de caixa e projeções.",
                        10,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(21),
                        createCategoryList("Plano de Negócios", "Empreendedorismo", "Finanças", "Marketing"),
                        serviceImageUrl,
                        managedUser
                ),
                // 54
                createService(
                        "Montagem de Estante e Prateleiras",
                        "Preciso de um profissional para montar uma estante de 2 metros e fixar 3 prateleiras "
                                + "na parede da sala. Inclui furação e nivelamento.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(7),
                        createCategoryList("Montagem de Móveis", "Prateleiras", "Estante", "Instalação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 55
                createService(
                        "Revisão de Contratos (Advogado)",
                        "Preciso de um advogado para revisar um contrato de prestação de serviços freelance. "
                                + "Verificar cláusulas de confidencialidade e multas.",
                        9,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(11),
                        createCategoryList("Direito", "Contratos", "Advogado", "Revisão Jurídica"),
                        serviceImageUrl,
                        managedUser
                ),
                // 56
                createService(
                        "Instalação de Purificador de Água",
                        "Comprei um purificador de água de bancada com conexão direta. Preciso de um encanador "
                                + "para instalar na pia, com vedação adequada.",
                        2,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(5),
                        createCategoryList("Encanamento", "Purificador de Água", "Instalação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 57
                createService(
                        "Aulas de Reforço em Química (Ensino Médio)",
                        "Estou com dificuldades em química orgânica e estequiometria. Procuro professor "
                                + "particular para aulas online 2x por semana.",
                        2,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(17),
                        createCategoryList("Educação", "Química", "Reforço Escolar", "Ensino Médio"),
                        serviceImageUrl,
                        managedUser
                ),
                // 58
                createService(
                        "Conserto de Fechadura de Cofre",
                        "O cofre doméstico está com a fechadura travada e não abre com a chave. Preciso "
                                + "de um chaveiro especializado em cofres.",
                        8,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(9),
                        createCategoryList("Chaveiro", "Cofre", "Fechadura", "Segurança"),
                        serviceImageUrl,
                        managedUser
                ),
                // 59
                createService(
                        "Otimização de SEO para Site WordPress",
                        "Meu site não aparece no Google. Preciso de um especialista em SEO para análise "
                                + "de palavras-chave, meta tags e backlinks.",
                        12,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(19),
                        createCategoryList("SEO", "WordPress", "Marketing Digital", "Google"),
                        serviceImageUrl,
                        managedUser
                ),
                // 60
                createService(
                        "Limpeza de Sofá e Poltrona",
                        "Preciso de higienização de um sofá de 3 lugares e duas poltronas de tecido. "
                                + "Manchas de alimentos e odores.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(8),
                        createCategoryList("Limpeza", "Sofá", "Poltrona", "Higienização"),
                        serviceImageUrl,
                        managedUser
                ),
                // 61
                createService(
                        "Desenvolvimento de Dashboard em Power BI",
                        "Preciso de um analista de dados para criar um dashboard interativo no Power BI "
                                + "a partir de planilhas Excel de vendas.",
                        11,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(16),
                        createCategoryList("Power BI", "Business Intelligence", "Dashboards", "Análise de Dados"),
                        serviceImageUrl,
                        managedUser
                ),
                // 62
                createService(
                        "Troca de Resistência de Chuveiro",
                        "Chuveiro elétrico está saindo água fria mesmo no modo inverno. Troca de resistência. "
                                + "Preciso de um eletricista.",
                        2,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(3),
                        createCategoryList("Elétrica", "Chuveiro", "Resistência", "Reparo"),
                        serviceImageUrl,
                        managedUser
                ),
                // 63
                createService(
                        "Aulas de Desenho Artístico para Crianças",
                        "Busco professora de desenho para minha filha de 7 anos. Aulas presenciais uma vez "
                                + "por semana, com técnicas de lápis de cor e aquarela.",
                        2,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(28),
                        createCategoryList("Arte", "Desenho", "Educação Infantil", "Criatividade"),
                        serviceImageUrl,
                        managedUser
                ),
                // 64
                createService(
                        "Manutenção de Notebook (Limpeza e Troca de Pasta Térmica)",
                        "Meu notebook está superaquecendo e desligando sozinho. Preciso de limpeza interna, "
                                + "troca de pasta térmica e verificação do cooler.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Informática", "Notebook", "Manutenção", "Hardware"),
                        serviceImageUrl,
                        managedUser
                ),
                // 65
                createService(
                        "Roteiro e Produção de Podcast",
                        "Estou lançando um podcast e preciso de ajuda com roteiro, edição de áudio, "
                                + "identidade sonora e distribuição nas plataformas.",
                        9,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(26),
                        createCategoryList("Podcast", "Edição de Áudio", "Roteiro", "Produção"),
                        serviceImageUrl,
                        managedUser
                ),
                // 66
                createService(
                        "Aplicação de Insulfilm em Janelas",
                        "Quero aplicar insulfilm escuro nas janelas do quarto para bloquear luz e calor. "
                                + "Preciso de profissional com experiência em películas.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(13),
                        createCategoryList("Insulfilm", "Janelas", "Controle Solar", "Automação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 67
                createService(
                        "Estratégia de Marketing para Instagram",
                        "Preciso de um consultor de marketing digital para criar uma estratégia de conteúdo "
                                + "e crescimento orgânico no Instagram para minha loja de artesanato.",
                        8,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(20),
                        createCategoryList("Marketing Digital", "Instagram", "Estratégia", "Conteúdo"),
                        serviceImageUrl,
                        managedUser
                ),
                // 68
                createService(
                        "Desentupimento de Esgoto com Hidrojato",
                        "Esgoto da casa está entupido e com refluxo. Necessário desentupimento com "
                                + "equipamento de hidrojato e inspeção por câmera.",
                        10,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(4),
                        createCategoryList("Desentupimento", "Esgoto", "Hidrojato", "Encanamento"),
                        serviceImageUrl,
                        managedUser
                ),
                // 69
                createService(
                        "Elaboração de Currículo e LinkedIn",
                        "Preciso de um especialista em RH para reescrever meu currículo e perfil do LinkedIn "
                                + "destacando minhas competências em vendas.",
                        4,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(6),
                        createCategoryList("Currículo", "LinkedIn", "Recursos Humanos", "Carreira"),
                        serviceImageUrl,
                        managedUser
                ),
                // 70
                createService(
                        "Reparo de Geladeira Frost Free",
                        "Geladeira está formando gelo no congelador e não resfria a parte de baixo. "
                                + "Problema no termostato ou degelo.",
                        6,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(7),
                        createCategoryList("Eletrodomésticos", "Geladeira", "Reparo", "Refrigeração"),
                        serviceImageUrl,
                        managedUser
                ),
                // 71
                createService(
                        "Transcrição de Áudio para Texto",
                        "Preciso transcrever 3 horas de entrevistas em áudio para texto editado. "
                                + "Prazo 5 dias. Formato Word.",
                        5,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(5),
                        createCategoryList("Transcrição", "Áudio para Texto", "Entrevistas", "Revisão"),
                        serviceImageUrl,
                        managedUser
                ),
                // 72
                createService(
                        "Instalação de Tela de Proteção para Gatos",
                        "Preciso de um profissional para instalar tela de proteção nas janelas do apartamento "
                                + "(6 janelas) para meus gatos não caírem.",
                        7,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(12),
                        createCategoryList("Tela de Proteção", "Segurança para Animais", "Instalação", "Janelas"),
                        serviceImageUrl,
                        managedUser
                ),
                // 73
                createService(
                        "Acompanhamento Nutricional Online",
                        "Busco uma nutricionista para consultas online, com plano alimentar personalizado "
                                + "para emagrecimento e dieta sem glúten.",
                        4,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(9),
                        createCategoryList("Nutrição", "Emagrecimento", "Sem Glúten", "Saúde"),
                        serviceImageUrl,
                        managedUser
                ),
                // 74
                createService(
                        "Limpeza de Caixa D'Água",
                        "Preciso de limpeza e desinfecção da caixa d'água de 1000 litros da minha casa. "
                                + "Inclui esvaziamento, escovação e aplicação de cloro.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(14),
                        createCategoryList("Limpeza", "Caixa D'Água", "Hidráulica", "Saneamento"),
                        serviceImageUrl,
                        managedUser
                ),
                // 75
                createService(
                        "Desenvolvimento de Jogo 2D com Unity",
                        "Preciso de um desenvolvedor de jogos para criar um jogo 2D educativo para crianças. "
                                + "Plataforma WebGL. Entregar código e assets.",
                        16,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(30),
                        createCategoryList("Game Development", "Unity", "2D", "Educativo"),
                        serviceImageUrl,
                        managedUser
                ),
                // 76
                createService(
                        "Conserto de Furadeira e Parafusadeira",
                        "Minha furadeira de impacto está com mau contato e a parafusadeira não carrega "
                                + "mais a bateria. Reparo de ferramentas elétricas.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(8),
                        createCategoryList("Ferramentas Elétricas", "Conserto", "Manutenção", "Furadeira"),
                        serviceImageUrl,
                        managedUser
                ),
                // 77
                createService(
                        "Revisão de TCC e Formatação ABNT",
                        "Preciso de um revisor para corrigir meu TCC de administração (60 páginas), "
                                + "formatar citações e referências conforme ABNT.",
                        6,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(18),
                        createCategoryList("Revisão de Texto", "ABNT", "TCC", "Normalização"),
                        serviceImageUrl,
                        managedUser
                ),
                // 78
                createService(
                        "Troca de Vidro de Celular (Tela)",
                        "Quebrei a tela do meu iPhone 12. Preciso de um técnico para troca do vidro "
                                + "e verificação do display. Entrega rápida.",
                        7,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(3),
                        createCategoryList("Celular", "iPhone", "Troca de Tela", "Assistência Técnica"),
                        serviceImageUrl,
                        managedUser
                ),
                // 79
                createService(
                        "Aulas de Meditação e Mindfulness",
                        "Procuro um instrutor de meditação para aulas online, com foco em redução de "
                                + "estresse e ansiedade. Técnicas de respiração e mindfulness.",
                        3,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Meditação", "Mindfulness", "Saúde Mental", "Bem-Estar"),
                        serviceImageUrl,
                        managedUser
                ),
                // 80
                createService(
                        "Instalação de Sistema de Irrigação Automática",
                        "Preciso de um jardineiro para instalar sistema de irrigação por gotejamento "
                                + "no jardim de 30m², com temporizador.",
                        8,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(22),
                        createCategoryList("Jardinagem", "Irrigação", "Automação", "Gotejamento"),
                        serviceImageUrl,
                        managedUser
                ),
                // 81
                createService(
                        "Análise de Dados com Python (Pandas)",
                        "Preciso de um cientista de dados para analisar um dataset de vendas e gerar "
                                + "insights, gráficos e relatório em Jupyter Notebook.",
                        9,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(14),
                        createCategoryList("Python", "Pandas", "Análise de Dados", "Data Science"),
                        serviceImageUrl,
                        managedUser
                ),
                // 82
                createService(
                        "Limpeza de Fachada com Água Pressurizada",
                        "Fachada do prédio está com manchas de poluição. Preciso de empresa especializada "
                                + "em limpeza com água pressurizada e andaimes.",
                        14,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(25),
                        createCategoryList("Limpeza de Fachada", "Água Pressurizada", "Manutenção Predial"),
                        serviceImageUrl,
                        managedUser
                ),
                // 83
                createService(
                        "Configuração de Servidor Linux (Ubuntu)",
                        "Preciso de um administrador de sistemas para configurar servidor Ubuntu com "
                                + "Apache, banco de dados e firewall. Acesso remoto.",
                        12,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(17),
                        createCategoryList("Linux", "Servidor", "Administração de Sistemas", "Ubuntu"),
                        serviceImageUrl,
                        managedUser
                ),
                // 84
                createService(
                        "Reparo de Porta de Correr (Deslizamento)",
                        "Porta de correr da sacada está saindo do trilho e difícil de mover. "
                                + "Preciso de um marceneiro ou serralheiro.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(6),
                        createCategoryList("Marcenaria", "Porta de Correr", "Reparo", "Trilhos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 85
                createService(
                        "Ghostwriting de E-book",
                        "Preciso de um redator para escrever um e-book de 50 páginas sobre produtividade. "
                                + "Pesquisa e conteúdo original, com direitos autorais cedidos.",
                        10,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(28),
                        createCategoryList("Ghostwriting", "E-book", "Redação", "Produtividade"),
                        serviceImageUrl,
                        managedUser
                ),
                // 86
                createService(
                        "Instalação de Forro de Gesso",
                        "Quero instalar forro de gesso (rebaixado) na sala com sanca de LED. "
                                + "Preciso de um gesseiro experiente.",
                        15,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(32),
                        createCategoryList("Gesso", "Forro", "Reforma", "Sanca de LED"),
                        serviceImageUrl,
                        managedUser
                ),
                // 87
                createService(
                        "Aulas de Programação Web (HTML, CSS, JS)",
                        "Iniciante querendo aprender front-end. Professor online para 5 sessões "
                                + "de 1 hora, com projeto prático de site pessoal.",
                        3,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(15),
                        createCategoryList("Programação Web", "HTML", "CSS", "JavaScript"),
                        serviceImageUrl,
                        managedUser
                ),
                // 88
                createService(
                        "Desentupimento de Vaso Sanitário com Objeto",
                        "Vaso sanitário entupiu com brinquedo de criança. Preciso de desentupimento "
                                + "sem quebrar a louça.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(2),
                        createCategoryList("Desentupimento", "Vaso Sanitário", "Encanamento", "Urgente"),
                        serviceImageUrl,
                        managedUser
                ),
                // 89
                createService(
                        "Tradução de Site para Inglês (5 páginas)",
                        "Preciso de um tradutor para traduzir as 5 páginas principais do meu site "
                                + "do português para o inglês, mantendo palavras-chave de SEO.",
                        6,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(12),
                        createCategoryList("Tradução", "Inglês", "SEO", "Site"),
                        serviceImageUrl,
                        managedUser
                ),
                // 90
                createService(
                        "Revisão de Freio de Bicicleta e Câmbio",
                        "Minha bicicleta está com freios frouxos e câmbio desregulado. "
                                + "Preciso de um mecânico de bicicletas para ajuste.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Bicicleta", "Mecânica", "Freios", "Câmbio"),
                        serviceImageUrl,
                        managedUser
                ),
                // 91
                createService(
                        "Criação de Landing Page para Captura de Leads",
                        "Preciso de um desenvolvedor front-end para criar uma landing page responsiva "
                                + "com formulário de captura integrado ao Mailchimp.",
                        8,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(13),
                        createCategoryList("Landing Page", "Front-end", "Mailchimp", "Leads"),
                        serviceImageUrl,
                        managedUser
                ),
                // 92
                createService(
                        "Aplicação de Resina em Piso de Concreto",
                        "Piso da garagem está com manchas e rachaduras. Quero aplicar resina epóxi "
                                + "para proteger e dar acabamento.",
                        11,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(24),
                        createCategoryList("Piso", "Resina", "Concreto", "Reforma"),
                        serviceImageUrl,
                        managedUser
                ),
                // 93
                createService(
                        "Consultoria em SEO Local para Pequeno Negócio",
                        "Tenho uma padaria e quero aparecer no Google Maps. Preciso de SEO local, "
                                + "Google Meu Negócio e avaliações.",
                        7,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(15),
                        createCategoryList("SEO Local", "Google Meu Negócio", "Marketing", "Pequenos Negócios"),
                        serviceImageUrl,
                        managedUser
                ),
                // 94
                createService(
                        "Manutenção de Bomba Submersa de Poço",
                        "Bomba do poço artesiano não está ligando. Preciso de um técnico hidráulico "
                                + "para diagnóstico e reparo da bomba.",
                        9,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(18),
                        createCategoryList("Hidráulica", "Bomba Submersa", "Poço", "Manutenção"),
                        serviceImageUrl,
                        managedUser
                ),
                // 95
                createService(
                        "Edição de Fotos para Produtos (E-commerce)",
                        "Preciso de um editor de fotos para remover fundo, ajustar cor e brilho "
                                + "de 100 fotos de produtos para loja virtual.",
                        7,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(7),
                        createCategoryList("Edição de Fotos", "E-commerce", "Photoshop", "Produtos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 96
                createService(
                        "Instalação de Antena de TV Digital",
                        "Minha TV não pega canais digitais. Preciso de um eletricista para instalar "
                                + "antena externa e configurar os canais.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(9),
                        createCategoryList("Antena", "TV Digital", "Elétrica", "Instalação"),
                        serviceImageUrl,
                        managedUser
                ),
                // 97
                createService(
                        "Preparação de Declaração Anual de MEI",
                        "Sou MEI e preciso de um contador para preparar e enviar minha declaração anual "
                                + "de faturamento (DASN-SIMEI).",
                        3,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(20),
                        createCategoryList("Contabilidade", "MEI", "Declaração Anual", "Impostos"),
                        serviceImageUrl,
                        managedUser
                ),
                // 98
                createService(
                        "Conserto de Interfone (Porteiro Eletrônico)",
                        "Interfone do prédio não está funcionando: não consigo abrir o portão "
                                + "e o áudio está falhando.",
                        5,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(11),
                        createCategoryList("Interfone", "Porteiro Eletrônico", "Reparo", "Elétrica"),
                        serviceImageUrl,
                        managedUser
                ),
                // 99
                createService(
                        "Legendagem de Vídeo para YouTube (Inglês)",
                        "Preciso de legendas em inglês para um vídeo de 20 minutos do meu canal. "
                                + "Entrega em arquivo SRT.",
                        4,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(6),
                        createCategoryList("Legendagem", "YouTube", "Inglês", "SRT"),
                        serviceImageUrl,
                        managedUser
                ),
                // 100
                createService(
                        "Troca de Vedação de Geladeira (Borracha)",
                        "A borracha da porta da geladeira não veda mais, vaza ar frio. Preciso de "
                                + "um técnico para trocar a vedação.",
                        3,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(5),
                        createCategoryList("Eletrodomésticos", "Geladeira", "Vedação", "Manutenção"),
                        serviceImageUrl,
                        managedUser
                )
        );

        serviceRepository.saveAll(servicesToCreate);
        System.out.println(servicesToCreate.size() + " servicos criados com sucesso.");
    }

    private ServiceEntity createService(String title, String description,
            int timeChronos, ServiceModality modality, LocalDate deadline,
            List<CategoryEntity> categories, String imageUrl, UserEntity userCreator) {
        ServiceEntity service = new ServiceEntity();
        service.setTitle(title);
        service.setDescription(description);
        service.setTimeChronos(timeChronos);
        service.setModality(modality);
        service.setDeadline(deadline);
        service.setPostedAt(LocalDateTime.now());
        service.setStatus(ServiceStatus.CRIADO);
        service.setCategoryEntities(categories);
        service.setUserCreator(userCreator);
        service.setServiceImageUrl(imageUrl);
        return service;
    }

    private List<CategoryEntity> createCategoryList(String... categoryNames) {
        List<CategoryEntity> categories = new ArrayList<>();
        for (String name : categoryNames) {
            CategoryEntity category = new CategoryEntity();
            category.setName(name);
            categories.add(category);
        }
        return categories;
    }
}

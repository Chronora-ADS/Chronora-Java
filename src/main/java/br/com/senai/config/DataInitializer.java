package br.com.senai.config;

import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.service.SupabaseStorageService;
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

    public DataInitializer(
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            SupabaseAuthService supabaseAuthService,
            AuthService authService,
            SupabaseStorageService storageService
    ) {
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
                System.out.println("Supabase ja possui dados. Inicializacao ignorada.");
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
        System.out.println("Usuario criado: " + createdUser.getEmail());
        return createdUser;
    }

    private void createDefaultServices(UserEntity userCreator) throws Exception {
        UserEntity managedUser = userRepository.findById(userCreator.getId())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado apos criacao"));

        byte[] serviceImageBytes = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("imagem.png")
        ).readAllBytes();
        String base64ServiceImage = "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(serviceImageBytes);
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
                )
        );

        serviceRepository.saveAll(servicesToCreate);
        System.out.println(servicesToCreate.size() + " servicos criados com sucesso.");
    }

    private ServiceEntity createService(
            String title,
            String description,
            int timeChronos,
            ServiceModality modality,
            LocalDate deadline,
            List<CategoryEntity> categories,
            String imageUrl,
            UserEntity userCreator
    ) {
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

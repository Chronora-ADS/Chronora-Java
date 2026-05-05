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
                createService(
                        "Manutencao Preventiva de Eletrodomesticos",
                        "Procuro um tecnico qualificado para realizar manutencao preventiva em geladeira, "
                                + "lavadora e micro-ondas em casa. Preciso de limpeza profunda, verificacao "
                                + "de componentes e ajustes para evitar falhas futuras.",
                        6,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(15),
                        createCategoryList("Manutencao", "Tecnico", "Eletrodomesticos"),
                        serviceImageUrl,
                        managedUser
                ),
                createService(
                        "Desentupimento de Pia e Vaso Sanitario",
                        "Preciso de um encanador para desentupir a pia da cozinha e o vaso sanitario do "
                                + "banheiro. A situacao esta urgente e preciso de um profissional confiavel "
                                + "que use equipamentos adequados sem danificar a tubulacao.",
                        4,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(7),
                        createCategoryList("Encanamento", "Urgente", "Com equipamentos"),
                        serviceImageUrl,
                        managedUser
                ),
                createService(
                        "Instalacao de Tomadas e Interruptores",
                        "Estou buscando um eletricista para instalar novas tomadas e interruptores no "
                                + "apartamento, alem de atualizar o quadro de luz conforme a norma ABNT "
                                + "NBR 5410. Preciso de seguranca e qualidade no servico.",
                        8,
                        ServiceModality.PRESENCIAL,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Eletrica", "Eletricista", "Tomadas", "Interruptores", "Quadro de Luz"),
                        serviceImageUrl,
                        managedUser
                ),
                createService(
                        "Terapia Cognitivo-Comportamental Online",
                        "Procuro um psicologo registrado e especializado em Terapia Cognitivo-Comportamental "
                                + "(TCC) para atendimento online. Estou enfrentando ansiedade e estresse "
                                + "cronico e busco um profissional com experiencia comprovada.",
                        5,
                        ServiceModality.REMOTO,
                        LocalDate.now().plusDays(10),
                        createCategoryList("Saude Mental", "Terapia", "TCC", "Ansiedade"),
                        serviceImageUrl,
                        managedUser
                ),
                createService(
                        "Desenvolvimento de APIs com Spring Boot",
                        "Preciso de um desenvolvedor backend especialista em Spring Boot para criar uma API "
                                + "RESTful robusta, com autenticacao JWT, integracao com PostgreSQL e "
                                + "tratamento personalizado de excecoes.",
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
                                "Documentacao"
                        ),
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

package br.com.senai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import lombok.RequiredArgsConstructor;

import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;

import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.UserDTO;

import br.com.senai.service.SupabaseAuthService;
import br.com.senai.service.AuthService;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    @Bean
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

        // Documento
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName("documento_padrao.png");
        documentDTO.setType("image/png");

        byte[] imageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images.png")).readAllBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        documentDTO.setData(base64Image);
        defaultUser.setDocument(documentDTO);

        // Registrar no Supabase
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("name", defaultUser.getName());
        userMetadata.put("phone", defaultUser.getPhoneNumber());
        var supabaseUserDTO = supabaseAuthService.signUp(
                defaultUser.getEmail(),
                defaultUser.getPassword(),
                userMetadata
        );

        // Registrar no banco local
        UserEntity createdUser = authService.register(defaultUser, supabaseUserDTO.getId());
        System.out.println("Usuário criado: " + createdUser.getEmail());
        return createdUser;
    }

    private void createDefaultServices(UserEntity userCreator) throws Exception {
        // Garantir que temos a entidade mais recente do banco
        UserEntity managedUser = userRepository.findById(userCreator.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após criação"));

        // Carregar imagem uma única vez
        byte[] serviceImageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images.png")).readAllBytes();
        String base64ServiceImage = "data:image/png;base64," + Base64.getEncoder().encodeToString(serviceImageBytes);

        // Lista de serviços para criar
        List<ServiceEntity> servicesToCreate = Arrays.asList(
                createService("Manutenção Preventiva de Eletrodomésticos",
                        "Procuro um técnico qualificado para realizar manutenção preventiva em geladeira, lavadora e micro-ondas em casa. " +
                                "Preciso de limpeza profunda, verificação de componentes e ajustes para evitar falhas futuras.",
                        6, "PRESENCIAL", LocalDate.now().plusDays(15),
                        createCategoryList("Manutenção", "Técnico", "Eletrodomésticos"), base64ServiceImage, managedUser),
                createService("Desentupimento de Pia e Vaso Sanitário",
                        "Preciso de um encanador para desentupir a pia da cozinha e o vaso sanitário do banheiro. A situação está urgente e " +
                                "preciso de um profissional confiável que use equipamentos adequados sem danificar a tubulação.",
                        4, "PRESENCIAL", LocalDate.now().plusDays(7),
                        createCategoryList("Encanamento", "Urgente", "Com equipamentos"), base64ServiceImage, managedUser),
                createService("Instalação de Tomadas e Interruptores",
                        "Estou buscando um eletricista para instalar novas tomadas e interruptores no apartamento, além de atualizar o " +
                                "quadro de luz conforme a norma ABNT NBR 5410. Preciso de segurança e qualidade no serviço.",
                        8, "PRESENCIAL", LocalDate.now().plusDays(10),
                        createCategoryList("Elétrica", "Eletricista", "Tomadas", "Interruptores", "Quadro de Luz"), base64ServiceImage, managedUser),
                createService("Terapia Cognitivo-Comportamental Online",
                        "Procuro um psicólogo registrado e especializado em Terapia Cognitivo-Comportamental (TCC) para atendimento online. " +
                                "Estou enfrentando ansiedade e estresse crônico e busco um profissional com experiência comprovada, que ofereça sessões " +
                                "seguras e confidenciais via plataforma de videochamada.",
                        5, "REMOTO", LocalDate.now().plusDays(10),
                        createCategoryList("Saúde Mental", "Terapia", "TCC", "Ansiedade"), base64ServiceImage, managedUser),
                createService("Desenvolvimento de APIs com Spring Boot",
                        "Preciso de um desenvolvedor backend especialista em Spring Boot para criar uma API RESTful robusta, com autenticação JWT," +
                                " integração com banco de dados PostgreSQL e tratamento personalizado de exceções. Busco boas práticas de código, documentação " +
                                "clara e entrega em prazo definido.",
                        9, "REMOTO", LocalDate.now().plusDays(12),
                        createCategoryList("Desenvolvimento de Software", "Backend", "Spring Boot", "Java", "JWT", "PostgreSQL", "Documentação"),
                        base64ServiceImage, managedUser)
        );
        serviceRepository.saveAll(servicesToCreate);
        System.out.println(servicesToCreate.size() + " serviços criados com sucesso!");
    }

    private ServiceEntity createService(String title, String description, int timeChronos, String modality, LocalDate deadline, List<CategoryEntity> categories,
                                        String base64Image, UserEntity userCreator) {
        ServiceEntity service = new ServiceEntity();
        service.setTitle(title);
        service.setDescription(description);
        service.setTimeChronos(timeChronos);
        service.setModality(modality);
        service.setDeadline(deadline);
        service.setPostedAt(LocalDateTime.now());
        service.setCategoryEntities(categories);
        service.setUserCreator(userCreator);

        // Processar imagem
        String imageData;
        if (base64Image.contains(",")) {
            imageData = base64Image.substring(base64Image.indexOf(",") + 1);
        } else {
            imageData = base64Image;
        }
        service.setServiceImage(Base64.getDecoder().decode(imageData));
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